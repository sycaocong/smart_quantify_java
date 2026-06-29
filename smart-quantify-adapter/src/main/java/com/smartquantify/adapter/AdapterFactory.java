package com.smartquantify.adapter;

import com.smartquantify.common.enums.Exchange;
import com.smartquantify.adapter.binance.BinanceAdapter;
import com.smartquantify.adapter.okx.OkxAdapter;
import com.smartquantify.adapter.bybit.BybitAdapter;
import com.smartquantify.adapter.huobi.HuobiAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdapterFactory {
    private static final Map<Exchange, ExchangeAdapter> ADAPTERS = new ConcurrentHashMap<>();

    static {
        ADAPTERS.put(Exchange.BINANCE, new BinanceAdapter());
        ADAPTERS.put(Exchange.OKX, new OkxAdapter());
        ADAPTERS.put(Exchange.BYBIT, new BybitAdapter());
        ADAPTERS.put(Exchange.HUOBI, new HuobiAdapter());
    }

    private AdapterFactory() {
    }

    public static ExchangeAdapter getAdapter(Exchange exchange) {
        return ADAPTERS.get(exchange);
    }

    public static void registerAdapter(Exchange exchange, ExchangeAdapter adapter) {
        ADAPTERS.put(exchange, adapter);
    }
}