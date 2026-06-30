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

@Slf4j
@Component
public class AdapterFactory {
    private final Map<Exchange, ExchangeAdapter> ADAPTERS = new ConcurrentHashMap<>();

    private final BinanceAdapter binanceAdapter;
    private final OkxAdapter okxAdapter;
    private final BybitAdapter bybitAdapter;
    private final HuobiAdapter huobiAdapter;

    public AdapterFactory(BinanceAdapter binanceAdapter, OkxAdapter okxAdapter,
                          BybitAdapter bybitAdapter, HuobiAdapter huobiAdapter) {
        this.binanceAdapter = binanceAdapter;
        this.okxAdapter = okxAdapter;
        this.bybitAdapter = bybitAdapter;
        this.huobiAdapter = huobiAdapter;
    }

    @PostConstruct
    public void init() {
        ADAPTERS.put(Exchange.BINANCE, binanceAdapter);
        ADAPTERS.put(Exchange.OKX, okxAdapter);
        ADAPTERS.put(Exchange.BYBIT, bybitAdapter);
        ADAPTERS.put(Exchange.HUOBI, huobiAdapter);
        log.info("Exchange adapters initialized: {}", ADAPTERS.keySet());
    }

    public ExchangeAdapter getAdapter(Exchange exchange) {
        return ADAPTERS.get(exchange);
    }

    public void registerAdapter(Exchange exchange, ExchangeAdapter adapter) {
        ADAPTERS.put(exchange, adapter);
        log.info("Registered adapter for exchange: {}", exchange);
    }
}