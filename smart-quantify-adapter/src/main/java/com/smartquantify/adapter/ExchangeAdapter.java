package com.smartquantify.adapter;

import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.model.*;

import java.util.List;
import java.util.function.Consumer;

/**
 * 交易所适配器接口
 * 设计文档: [DESIGN.md](../DESIGN.md#47-交易所适配器)
 */
public interface ExchangeAdapter {
    String getName();

    Exchange getExchange();

    List<Kline> getKlines(String symbol, String interval, int limit);

    OrderBook getOrderBook(String symbol, int limit);

    Ticker getTicker(String symbol);

    List<Instrument> getInstruments();

    com.smartquantify.common.model.Order placeOrder(OrderRequest request);

    void cancelOrder(CancelOrderRequest request);

    com.smartquantify.common.model.Order getOrder(String symbol, String orderId);

    List<com.smartquantify.common.model.Order> getOpenOrders(String symbol);

    void subscribeKlines(String symbol, String interval, Consumer<Kline> handler);

    void subscribeOrderBook(String symbol, Consumer<OrderBook> handler);

    void unsubscribe(String symbol);
}