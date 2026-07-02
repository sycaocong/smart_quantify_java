package com.smartquantify.adapter.okx;

import com.smartquantify.adapter.ExchangeAdapter;
import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.OrderStatus;
import com.smartquantify.common.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * OKX交易所适配器
 * 实现与OKX交易所的API交互
 */
@Slf4j
@Component
public class OkxAdapter implements ExchangeAdapter {

    @Override
    public String getName() {
        return "OKX";
    }

    @Override
    public Exchange getExchange() {
        return Exchange.OKX;
    }

    @Override
    public List<Kline> getKlines(String symbol, String interval, int limit) {
        log.info("Getting klines from OKX: {} {} {}", symbol, interval, limit);
        return new ArrayList<>();
    }

    @Override
    public OrderBook getOrderBook(String symbol, int limit) {
        log.info("Getting order book from OKX: {} {}", symbol, limit);
        return OrderBook.builder().symbol(symbol).build();
    }

    @Override
    public Ticker getTicker(String symbol) {
        log.info("Getting ticker from OKX: {}", symbol);
        return Ticker.builder().symbol(symbol).build();
    }

    @Override
    public List<Instrument> getInstruments() {
        log.info("Getting instruments from OKX");
        return new ArrayList<>();
    }

    @Override
    public com.smartquantify.common.model.Order placeOrder(OrderRequest request) {
        log.info("Placing order to OKX: {}", request);
        return com.smartquantify.common.model.Order.builder()
                .id("OKX-" + System.currentTimeMillis())
                .symbol(request.getSymbol())
                .status(OrderStatus.NEW)
                .build();
    }

    @Override
    public void cancelOrder(CancelOrderRequest request) {
        log.info("Cancelling order on OKX: {}", request);
    }

    @Override
    public com.smartquantify.common.model.Order getOrder(String symbol, String orderId) {
        log.info("Getting order from OKX: {} {}", symbol, orderId);
        return com.smartquantify.common.model.Order.builder().id(orderId).build();
    }

    @Override
    public List<com.smartquantify.common.model.Order> getOpenOrders(String symbol) {
        log.info("Getting open orders from OKX: {}", symbol);
        return new ArrayList<>();
    }

    @Override
    public void subscribeKlines(String symbol, String interval, Consumer<Kline> handler) {
        log.info("Subscribing to klines on OKX: {} {}", symbol, interval);
    }

    @Override
    public void subscribeOrderBook(String symbol, Consumer<OrderBook> handler) {
        log.info("Subscribing to order book on OKX: {}", symbol);
    }

    @Override
    public void unsubscribe(String symbol) {
        log.info("Unsubscribing from OKX: {}", symbol);
    }
}