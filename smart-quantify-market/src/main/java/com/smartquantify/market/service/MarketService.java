package com.smartquantify.market.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.smartquantify.adapter.AdapterFactory;
import com.smartquantify.adapter.ExchangeAdapter;
import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.InstrumentType;
import com.smartquantify.common.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 市场数据服务
 * 负责获取和缓存交易所的市场行情数据
 * <p>
 * 二级缓存策略：本地Caffeine → Redis → 交易所远程接口
 * <p>
 * 缓存层级说明：
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                     一级缓存：本地Caffeine                        │
 * │  特点：内存访问，速度最快，容量有限，进程内共享                     │
 * │  用途：热点数据快速访问，减轻Redis压力                            │
 * └─────────────────────────────────────────────────────────────────┘
 *                              │ 未命中
 *                              ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                     二级缓存：Redis分布式缓存                      │
 * │  特点：网络访问，速度较快，容量大，多进程共享                       │
 * │  用途：跨服务共享缓存，支持分布式部署                              │
 * └─────────────────────────────────────────────────────────────────┘
 *                              │ 未命中
 *                              ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    三级：交易所远程API                            │
 * │  特点：网络访问，速度最慢，有调用限制                             │
 * │  用途：获取最新数据，作为缓存数据源                              │
 * └─────────────────────────────────────────────────────────────────┘
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheManager = "redisCacheManager")
public class MarketService {

    /**
     * 交易所适配器工厂
     * 用于获取不同交易所的适配器实例
     */
    private final AdapterFactory adapterFactory;

    /**
     * Redis缓存管理器
     * 用于手动操作Redis缓存（更新、删除等）
     */
    private final CacheManager cacheManager;

    /**
     * 静态缓存交易所名称集合
     * 预加载所有交易所枚举名称，避免每次调用时流式遍历枚举
     * 使用不可变集合保证线程安全
     */
    private static final Set<String> EXCHANGE_NAMES = Arrays.stream(Exchange.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    /**
     * K线本地缓存
     * 容量：10,000条
     * 过期时间：写入后5秒
     * 加载策略：本地缓存未命中时，调用loadKlineFromRedis加载
     */
    private final LoadingCache<String, List<Kline>> localKlineCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build(this::loadKlineFromRedis);

    /**
     * 订单簿本地缓存
     * 容量：5,000条（订单簿数据较大，容量较小）
     * 过期时间：写入后3秒（订单簿变化最频繁）
     * 加载策略：本地缓存未命中时，调用loadOrderBookFromRedis加载
     */
    private final LoadingCache<String, OrderBook> localOrderBookCache = Caffeine.newBuilder()
            .maximumSize(5_000)
            .expireAfterWrite(3, TimeUnit.SECONDS)
            .build(this::loadOrderBookFromRedis);

    /**
     * Ticker本地缓存
     * 容量：10,000条
     * 过期时间：写入后2秒（实时行情数据，更新最频繁）
     * 加载策略：本地缓存未命中时，调用loadTickerFromRedis加载
     */
    private final LoadingCache<String, Ticker> localTickerCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .build(this::loadTickerFromRedis);

    /**
     * 合约列表本地缓存
     * 容量：1,000条（合约列表变化较少）
     * 过期时间：写入后30秒
     * 加载策略：本地缓存未命中时，调用loadInstrumentFromRedis加载
     */
    private final LoadingCache<String, List<Instrument>> localInstrumentCache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(this::loadInstrumentFromRedis);

    /**
     * 获取K线数据
     * <p>
     * 二级缓存流程：
     * 1. 快速参数校验（null检查、非法交易所、limit边界限制）
     * 2. 组装缓存键：exchange:symbol:interval:limit
     * 3. 查询本地Caffeine缓存
     * 4. 本地缓存未命中 → 查询Redis（@Cacheable）
     * 5. Redis未命中 → 调用交易所API
     * <p>
     * @param exchange 交易所名称（如BINANCE、OKX、BYBIT、HUOBI）
     * @param symbol 交易对（如BTC-USDT）
     * @param interval K线周期（如1m、5m、15m、1h、4h、1d）
     * @param limit 返回条数，默认100，范围1-500
     * @return K线数据列表，参数无效时返回空列表
     */
    public List<Kline> getKlines(String exchange, String symbol, String interval, Integer limit) {
        // 1. 快速参数拦截：null检查
        if (exchange == null || symbol == null || interval == null) {
            log.warn("K线查询参数为空 exchange={},symbol={},interval={}", exchange, symbol, interval);
            return Collections.emptyList();
        }

        // 2. 非法交易所校验
        if (!EXCHANGE_NAMES.contains(exchange)) {
            log.warn("非法交易所 exchange={}", exchange);
            return Collections.emptyList();
        }

        // 3. 边界限制：limit范围1-500
        int queryLimit = Optional.ofNullable(limit).orElse(100);
        queryLimit = clamp(queryLimit, 1, 500);

        // 4. 组装缓存键
        String cacheKey = String.format("%s:%s:%s:%d", exchange, symbol, interval, queryLimit);

        // 5. 查询本地缓存（自动触发loadKlineFromRedis）
        return localKlineCache.get(cacheKey);
    }

    /**
     * 从Redis加载K线数据
     * 当本地Caffeine缓存未命中时调用此方法
     * 使用@Cacheable注解实现Redis缓存
     * <p>
     * @param cacheKey 缓存键，格式：exchange:symbol:interval:limit
     * @return K线数据列表
     */
    @Cacheable(value = "klines", key = "#cacheKey")
    public List<Kline> loadKlineFromRedis(String cacheKey) {
        // 解析缓存键
        String[] arr = cacheKey.split(":");
        String exchange = arr[0];
        String symbol = arr[1];
        String interval = arr[2];
        int limit = Integer.parseInt(arr[3]);

        try {
            // 获取交易所适配器并调用API
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));
            return adapter.getKlines(symbol, interval, limit);
        } catch (Exception e) {
            log.error("拉取K线远程异常 exchange={},symbol={},interval={},limit={}",
                    exchange, symbol, interval, limit, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取订单簿数据
     * <p>
     * 二级缓存流程：
     * 1. 快速参数校验（null检查、非法交易所、limit边界限制）
     * 2. 组装缓存键：exchange:symbol:limit
     * 3. 查询本地Caffeine缓存
     * 4. 本地缓存未命中 → 查询Redis（@Cacheable）
     * 5. Redis未命中 → 调用交易所API
     * <p>
     * @param exchange 交易所名称
     * @param symbol 交易对
     * @param limit 返回深度，默认100，范围1-500
     * @return 订单簿数据，参数无效时返回null
     */
    public OrderBook getOrderBook(String exchange, String symbol, Integer limit) {
        // 1. 快速参数拦截：null检查
        if (exchange == null || symbol == null) {
            log.warn("订单簿查询参数为空 exchange={},symbol={}", exchange, symbol);
            return null;
        }

        // 2. 非法交易所校验
        if (!EXCHANGE_NAMES.contains(exchange)) {
            log.warn("非法交易所 exchange={}", exchange);
            return null;
        }

        // 3. 边界限制：limit范围1-500
        int queryLimit = Optional.ofNullable(limit).orElse(100);
        queryLimit = clamp(queryLimit, 1, 500);

        // 4. 组装缓存键
        String cacheKey = String.format("%s:%s:%d", exchange, symbol, queryLimit);

        // 5. 查询本地缓存
        return localOrderBookCache.get(cacheKey);
    }

    /**
     * 从Redis加载订单簿数据
     * 当本地Caffeine缓存未命中时调用此方法
     * <p>
     * @param cacheKey 缓存键，格式：exchange:symbol:limit
     * @return 订单簿数据
     */
    @Cacheable(value = "orderBook", key = "#cacheKey")
    public OrderBook loadOrderBookFromRedis(String cacheKey) {
        String[] arr = cacheKey.split(":");
        String exchange = arr[0];
        String symbol = arr[1];
        int limit = Integer.parseInt(arr[2]);

        try {
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));
            return adapter.getOrderBook(symbol, limit);
        } catch (Exception e) {
            log.error("拉取订单簿远程异常 exchange={},symbol={},limit={}",
                    exchange, symbol, limit, e);
            return null;
        }
    }

    /**
     * 获取单个Ticker数据
     * <p>
     * @param exchange 交易所名称
     * @param symbol 交易对
     * @return Ticker数据，参数无效时返回null
     */
    public Ticker getTicker(String exchange, String symbol) {
        // 1. 快速参数拦截
        if (exchange == null || symbol == null) {
            log.warn("Ticker查询参数为空 exchange={},symbol={}", exchange, symbol);
            return null;
        }

        // 2. 非法交易所校验
        if (!EXCHANGE_NAMES.contains(exchange)) {
            log.warn("非法交易所 exchange={}", exchange);
            return null;
        }

        // 3. 组装缓存键
        String cacheKey = String.format("%s:%s", exchange, symbol);

        // 4. 查询本地缓存
        return localTickerCache.get(cacheKey);
    }

    /**
     * 从Redis加载Ticker数据
     * 当本地Caffeine缓存未命中时调用此方法
     * <p>
     * @param cacheKey 缓存键，格式：exchange:symbol
     * @return Ticker数据
     */
    @Cacheable(value = "ticker", key = "#cacheKey")
    public Ticker loadTickerFromRedis(String cacheKey) {
        String[] arr = cacheKey.split(":");
        String exchange = arr[0];
        String symbol = arr[1];

        try {
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));
            return adapter.getTicker(symbol);
        } catch (Exception e) {
            log.error("拉取Ticker远程异常 exchange={},symbol={}", exchange, symbol, e);
            return null;
        }
    }

    /**
     * 获取所有交易对的Ticker数据
     * 通过遍历合约列表，逐个获取Ticker
     * <p>
     * @param exchange 交易所名称
     * @return Ticker列表，参数无效时返回空列表
     */
    public List<Ticker> getAllTickers(String exchange) {
        // 参数校验
        if (exchange == null || !EXCHANGE_NAMES.contains(exchange)) {
            log.warn("非法交易所 exchange={}", exchange);
            return Collections.emptyList();
        }

        try {
            // 获取所有合约
            List<Instrument> instruments = getInstruments(exchange, null);
            List<Ticker> tickers = new ArrayList<>();

            // 逐个获取Ticker
            for (Instrument instrument : instruments) {
                Ticker ticker = getTicker(exchange, instrument.getSymbol());
                if (ticker != null) {
                    tickers.add(ticker);
                }
            }

            log.debug("获取所有Ticker成功 exchange={},count={}", exchange, tickers.size());
            return tickers;
        } catch (Exception e) {
            log.error("获取所有Ticker异常 exchange={}", exchange, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取合约列表
     * <p>
     * @param exchange 交易所名称
     * @param type 合约类型（可选），如SPOT、FUTURES、OPTIONS
     * @return 合约列表，参数无效时返回空列表
     */
    public List<Instrument> getInstruments(String exchange, String type) {
        // 参数校验
        if (exchange == null) {
            log.warn("合约列表查询参数为空 exchange={}", exchange);
            return Collections.emptyList();
        }
        if (!EXCHANGE_NAMES.contains(exchange)) {
            log.warn("非法交易所 exchange={}", exchange);
            return Collections.emptyList();
        }

        // 组装缓存键
        String cacheKey = String.format("%s:%s", exchange, type);

        // 查询本地缓存
        List<Instrument> instruments = localInstrumentCache.get(cacheKey);

        // 如果指定了类型，进行过滤
        if (type != null) {
            InstrumentType instrumentType = InstrumentType.valueOf(type.toUpperCase());
            return instruments.stream()
                    .filter(i -> i.getType() == instrumentType)
                    .collect(Collectors.toList());
        }

        return instruments;
    }

    /**
     * 从Redis加载合约列表数据
     * <p>
     * @param cacheKey 缓存键，格式：exchange:type
     * @return 合约列表
     */
    @Cacheable(value = "instruments", key = "#cacheKey")
    public List<Instrument> loadInstrumentFromRedis(String cacheKey) {
        String[] arr = cacheKey.split(":");
        String exchange = arr[0];

        try {
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));
            return adapter.getInstruments();
        } catch (Exception e) {
            log.error("拉取合约列表远程异常 exchange={}", exchange, e);
            return Collections.emptyList();
        }
    }

    /**
     * 订阅K线实时数据
     * 通过WebSocket订阅指定交易对的K线数据，收到数据后自动更新缓存
     * <p>
     * @param exchange 交易所名称
     * @param symbol 交易对
     * @param interval K线周期
     */
    public void subscribeKlines(String exchange, String symbol, String interval) {
        // 参数校验
        if (!validateExchange(exchange)) {
            return;
        }

        try {
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));

            // 订阅K线，收到数据时更新Ticker缓存
            adapter.subscribeKlines(symbol, interval, kline -> {
                log.info("收到K线数据: symbol={}, interval={}, close={}",
                        kline.getSymbol(), kline.getInterval(), kline.getClose());

                // 更新Ticker缓存（使用最新收盘价）
                updateTickerCache(exchange, symbol, Ticker.builder()
                        .symbol(symbol)
                        .lastPrice(kline.getClose())
                        .build());
            });

            log.info("订阅K线成功 exchange={},symbol={},interval={}", exchange, symbol, interval);
        } catch (Exception e) {
            log.error("订阅K线异常 exchange={},symbol={},interval={}",
                    exchange, symbol, interval, e);
        }
    }

    /**
     * 订阅订单簿实时数据
     * 通过WebSocket订阅指定交易对的订单簿数据，收到数据后自动更新缓存
     * <p>
     * @param exchange 交易所名称
     * @param symbol 交易对
     */
    public void subscribeOrderBook(String exchange, String symbol) {
        // 参数校验
        if (!validateExchange(exchange)) {
            return;
        }

        try {
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));

            // 订阅订单簿，收到数据时更新缓存
            adapter.subscribeOrderBook(symbol, orderBook -> {
                log.info("收到订单簿数据: symbol={}, asks={}, bids={}",
                        orderBook.getSymbol(), orderBook.getAsks().size(), orderBook.getBids().size());

                // 更新订单簿缓存
                updateOrderBookCache(exchange, symbol, orderBook);

                // 如果卖盘非空，更新Ticker缓存（使用卖一价）
                if (!orderBook.getAsks().isEmpty()) {
                    updateTickerCache(exchange, symbol, Ticker.builder()
                            .symbol(symbol)
                            .lastPrice(orderBook.getAsks().get(0).getPrice())
                            .build());
                }
            });

            log.info("订阅订单簿成功 exchange={},symbol={}", exchange, symbol);
        } catch (Exception e) {
            log.error("订阅订单簿异常 exchange={},symbol={}", exchange, symbol, e);
        }
    }

    /**
     * 取消订阅
     * 取消指定交易对的所有订阅（K线、订单簿等）
     * <p>
     * @param exchange 交易所名称
     * @param symbol 交易对
     */
    public void unsubscribe(String exchange, String symbol) {
        // 参数校验
        if (!validateExchange(exchange)) {
            return;
        }

        try {
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));
            adapter.unsubscribe(symbol);

            log.info("取消订阅成功 exchange={},symbol={}", exchange, symbol);
        } catch (Exception e) {
            log.error("取消订阅异常 exchange={},symbol={}", exchange, symbol, e);
        }
    }

    /**
     * 更新K线缓存
     * 当WebSocket收到新K线数据时调用，主动更新Redis缓存并失效本地缓存
     * <p>
     * @param exchange 交易所名称
     * @param symbol 交易对
     * @param interval K线周期
     * @param klines K线数据列表
     */
    public void updateKlinesCache(String exchange, String symbol, String interval, List<Kline> klines) {
        try {
            Cache cache = cacheManager.getCache("klines");
            if (cache != null) {
                // 默认更新limit=100的缓存
                String cacheKey = String.format("%s:%s:%s:%d", exchange, symbol, interval, 100);
                cache.put(cacheKey, klines);

                // 失效本地缓存，下次查询时重新加载
                localKlineCache.invalidate(cacheKey);

                log.debug("更新K线缓存成功 key={}, size={}", cacheKey, klines.size());
            }
        } catch (Exception e) {
            log.error("更新K线缓存失败 exchange={},symbol={}", exchange, symbol, e);
        }
    }

    /**
     * 更新订单簿缓存
     * 当WebSocket收到新订单簿数据时调用，主动更新Redis缓存并失效本地缓存
     * <p>
     * @param exchange 交易所名称
     * @param symbol 交易对
     * @param orderBook 订单簿数据
     */
    public void updateOrderBookCache(String exchange, String symbol, OrderBook orderBook) {
        try {
            Cache cache = cacheManager.getCache("orderBook");
            if (cache != null) {
                String cacheKey = String.format("%s:%s:%d", exchange, symbol, 100);
                cache.put(cacheKey, orderBook);
                localOrderBookCache.invalidate(cacheKey);

                log.debug("更新订单簿缓存成功 key={}", cacheKey);
            }
        } catch (Exception e) {
            log.error("更新订单簿缓存失败 exchange={},symbol={}", exchange, symbol, e);
        }
    }

    /**
     * 更新Ticker缓存
     * 当WebSocket收到新行情数据时调用，主动更新Redis缓存并失效本地缓存
     * <p>
     * @param exchange 交易所名称
     * @param symbol 交易对
     * @param ticker Ticker数据
     */
    public void updateTickerCache(String exchange, String symbol, Ticker ticker) {
        try {
            Cache cache = cacheManager.getCache("ticker");
            if (cache != null) {
                String cacheKey = String.format("%s:%s", exchange, symbol);
                cache.put(cacheKey, ticker);
                localTickerCache.invalidate(cacheKey);

                log.debug("更新Ticker缓存成功 key={}, price={}", cacheKey, ticker.getLastPrice());
            }
        } catch (Exception e) {
            log.error("更新Ticker缓存失败 exchange={},symbol={}", exchange, symbol, e);
        }
    }

    /**
     * 清除指定交易对的所有缓存
     * 同时清除本地缓存和Redis缓存
     * <p>
     * @param exchange 交易所名称
     * @param symbol 交易对
     */
    public void evictSymbolCache(String exchange, String symbol) {
        try {
            // 清除Redis缓存
            evictCache("klines", exchange + ":" + symbol);
            evictCache("orderBook", exchange + ":" + symbol);
            evictCache("ticker", exchange + ":" + symbol);

            // 清除本地缓存中匹配前缀的键
            String keyPrefix = exchange + ":" + symbol;
            localKlineCache.invalidateAll(
                    localKlineCache.asMap().keySet().stream()
                            .filter(k -> k.startsWith(keyPrefix))
                            .collect(Collectors.toList())
            );
            localOrderBookCache.invalidateAll(
                    localOrderBookCache.asMap().keySet().stream()
                            .filter(k -> k.startsWith(keyPrefix))
                            .collect(Collectors.toList())
            );
            localTickerCache.invalidate(keyPrefix);

            log.info("清除交易对缓存成功 exchange={},symbol={}", exchange, symbol);
        } catch (Exception e) {
            log.error("清除交易对缓存失败 exchange={},symbol={}", exchange, symbol, e);
        }
    }

    /**
     * 清除指定缓存名的所有缓存
     * <p>
     * @param cacheName 缓存名
     * @param keyPrefix 键前缀（预留参数，当前实现清除整个缓存）
     */
    private void evictCache(String cacheName, String keyPrefix) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.debug("清除缓存成功 cacheName={}", cacheName);
        }
    }

    /**
     * 清除所有市场数据缓存
     * 同时清除本地缓存和Redis缓存
     */
    public void evictAllMarketCache() {
        try {
            // 清除Redis缓存
            evictCache("klines", "");
            evictCache("orderBook", "");
            evictCache("ticker", "");
            evictCache("instruments", "");

            // 清除本地缓存
            localKlineCache.invalidateAll();
            localOrderBookCache.invalidateAll();
            localTickerCache.invalidateAll();
            localInstrumentCache.invalidateAll();

            log.info("清除所有市场缓存成功");
        } catch (Exception e) {
            log.error("清除所有市场缓存失败", e);
        }
    }

    /**
     * 校验交易所名称是否合法
     * <p>
     * @param exchange 交易所名称
     * @return 合法返回true，否则返回false
     */
    private boolean validateExchange(String exchange) {
        if (exchange == null || !EXCHANGE_NAMES.contains(exchange)) {
            log.warn("非法交易所 exchange={}", exchange);
            return false;
        }
        return true;
    }

    /**
     * 将值限制在指定范围内
     * <p>
     * @param value 要限制的值
     * @param min 最小值
     * @param max 最大值
     * @return 限制后的值（在min和max之间）
     */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}