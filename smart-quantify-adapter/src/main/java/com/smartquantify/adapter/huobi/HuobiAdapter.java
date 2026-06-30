package com.smartquantify.adapter.huobi;

import com.smartquantify.adapter.ExchangeAdapter;
import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
public class HuobiAdapter implements ExchangeAdapter {

    @Override
    public String getName() {
        return "Huobi";
    }

    @Override
    public Exchange getExchange() {
        return Exchange.HUOBI;
    }

    @Override
    public List<Kline> getKlines(String symbol, String interval, int limit) {
        log.info("Getting klines from Huobi: {} {} {}", symbol, interval, limit);
        return new ArrayList<>();
    }

    @Override
    public OrderBook getOrderBook(String symbol, int limit) {
        log.info("Getting order book from Huobi: {} {}", symbol, limit);
        return OrderBook.builder().symbol(symbol).build();
    }

    @Override
    public Ticker getTicker(String symbol) {
        log.info("Getting ticker from Huobi: {}", symbol);
        return Ticker.builder().symbol(symbol).build();
    }

    @Override
    public List<Instrument> getInstruments() {
        log.info("Getting instruments from Huobi");
        return new ArrayList<>();
    }

    @Override
    public com.smartquantify.common.model.Order placeOrder(OrderRequest request) {
        log.info("Placing order to Huobi: {}", request);
        return com.smartquantify.common.model.Order.builder()
                .id("HUOBI-" + System.currentTimeMillis())
                .symbol(request.getSymbol())
                .status("NEW")
                .build();
    }

    @Override
    public void cancelOrder(CancelOrderRequest request) {
        log.info("Cancelling order on Huobi: {}", request);
    }

    @Override
    public com.smartquantify.common.model.Order getOrder(String symbol, String orderId) {
        log.info("Getting order from Huobi: {} {}", symbol, orderId);
        return com.smartquantify.common.model.Order.builder().id(orderId).build();
    }

    @Override
    public List<com.smartquantify.common.model.Order> getOpenOrders(String symbol) {
        log.info("Getting open orders from Huobi: {}", symbol);
        return new ArrayList<>();
    }

    @Override
    public void subscribeKlines(String symbol, String interval, Consumer<Kline> handler) {
        log.info("Subscribing to klines on Huobi: {} {}", symbol, interval);
    }

    @Override
    public void subscribeOrderBook(String symbol, Consumer<OrderBook> handler) {
        log.info("Subscribing to order book on Huobi: {}", symbol);
    }

    @Override
    public void unsubscribe(String symbol) {
        log.info("Unsubscribing from Huobi: {}", symbol);
    }
}