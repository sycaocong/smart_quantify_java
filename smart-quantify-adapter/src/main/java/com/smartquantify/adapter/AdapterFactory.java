package com.smartquantify.adapter;

import com.smartquantify.common.enums.Exchange;
import com.smartquantify.adapter.binance.BinanceAdapter;
import com.smartquantify.adapter.okx.OkxAdapter;
import com.smartquantify.adapter.bybit.BybitAdapter;
import com.smartquantify.adapter.huobi.HuobiAdapter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 交易所适配器工厂
 * 负责管理和提供各交易所的适配器实例
 */
@Slf4j
@Component
public class AdapterFactory {

    /**
     * 适配器映射表，线程安全
     */
    private final Map<Exchange, ExchangeAdapter> ADAPTERS = new ConcurrentHashMap<>();

    /**
     * Binance交易所适配器
     */
    private final BinanceAdapter binanceAdapter;

    /**
     * OKX交易所适配器
     */
    private final OkxAdapter okxAdapter;

    /**
     * Bybit交易所适配器
     */
    private final BybitAdapter bybitAdapter;

    /**
     * Huobi交易所适配器
     */
    private final HuobiAdapter huobiAdapter;

    /**
     * 构造函数，注入所有交易所适配器
     */
    public AdapterFactory(BinanceAdapter binanceAdapter, OkxAdapter okxAdapter,
                          BybitAdapter bybitAdapter, HuobiAdapter huobiAdapter) {
        this.binanceAdapter = binanceAdapter;
        this.okxAdapter = okxAdapter;
        this.bybitAdapter = bybitAdapter;
        this.huobiAdapter = huobiAdapter;
    }

    /**
     * 初始化方法，注册所有交易所适配器
     */
    @PostConstruct
    public void init() {
        ADAPTERS.put(Exchange.BINANCE, binanceAdapter);
        ADAPTERS.put(Exchange.OKX, okxAdapter);
        ADAPTERS.put(Exchange.BYBIT, bybitAdapter);
        ADAPTERS.put(Exchange.HUOBI, huobiAdapter);
        log.info("Exchange adapters initialized: {}", ADAPTERS.keySet());
    }

    /**
     * 获取指定交易所的适配器
     * @param exchange 交易所枚举
     * @return 交易所适配器
     */
    public ExchangeAdapter getAdapter(Exchange exchange) {
        return ADAPTERS.get(exchange);
    }

    /**
     * 注册新的交易所适配器
     * @param exchange 交易所枚举
     * @param adapter 交易所适配器
     */
    public void registerAdapter(Exchange exchange, ExchangeAdapter adapter) {
        ADAPTERS.put(exchange, adapter);
        log.info("Registered adapter for exchange: {}", exchange);
    }
}