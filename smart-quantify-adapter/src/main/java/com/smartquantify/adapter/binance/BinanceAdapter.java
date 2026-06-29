package com.smartquantify.adapter.binance;

import com.smartquantify.adapter.ExchangeAdapter;
import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BinanceAdapter implements ExchangeAdapter {
    @Override
    public String getName() {
        return "Binance";
    }

    @Override
    public Exchange getExchange() {
        return Exchange.BINANCE;
    }

    @Override
    public List<Kline> getKlines(String symbol, String interval, int limit) {
        List<Kline> klines = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            klines.add(Kline.builder()
                    .symbol(symbol)
                    .interval(interval)
                    .openTime(LocalDateTime.now().minusMinutes(i))
                    .open(BigDecimal.valueOf(45000 + i * 10))
                    .high(BigDecimal.valueOf(45100 + i * 10))
                    .low(BigDecimal.valueOf(44900 + i * 10))
                    .close(BigDecimal.valueOf(45050 + i * 10))
                    .volume(BigDecimal.valueOf(100 + i))
                    .quoteVolume(BigDecimal.valueOf(4505000 + i * 45000))
                    .closeTime(LocalDateTime.now().minusMinutes(i - 1))
                    .build());
        }
        return klines;
    }

    @Override
    public OrderBook getOrderBook(String symbol, int limit) {
        List<OrderBook.OrderBookLevel> asks = new ArrayList<>();
        List<OrderBook.OrderBookLevel> bids = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            asks.add(OrderBook.OrderBookLevel.builder()
                    .price(BigDecimal.valueOf(45100 + i * 10))
                    .quantity(BigDecimal.valueOf(10 - i * 0.5))
                    .build());
            bids.add(OrderBook.OrderBookLevel.builder()
                    .price(BigDecimal.valueOf(44900 + i * 10))
                    .quantity(BigDecimal.valueOf(10 - i * 0.5))
                    .build());
        }
        return OrderBook.builder()
                .symbol(symbol)
                .timestamp(System.currentTimeMillis())
                .asks(asks)
                .bids(bids)
                .build();
    }

    @Override
    public Ticker getTicker(String symbol) {
        return Ticker.builder()
                .symbol(symbol)
                .lastPrice(BigDecimal.valueOf(45050))
                .openPrice(BigDecimal.valueOf(44800))
                .highPrice(BigDecimal.valueOf(45200))
                .lowPrice(BigDecimal.valueOf(44700))
                .volume24h(BigDecimal.valueOf(10000))
                .quoteVolume24h(BigDecimal.valueOf(450500000))
                .priceChange24h(BigDecimal.valueOf(250))
                .priceChangePercent24h(BigDecimal.valueOf(0.56))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public List<Instrument> getInstruments() {
        List<Instrument> instruments = new ArrayList<>();
        instruments.add(Instrument.builder()
                .symbol("BTCUSDT")
                .type(com.smartquantify.common.enums.InstrumentType.SPOT)
                .exchange(Exchange.BINANCE)
                .tickSize(BigDecimal.valueOf(0.01))
                .lotSize(BigDecimal.valueOf(0.00001))
                .minQuantity(BigDecimal.valueOf(0.00001))
                .maxQuantity(BigDecimal.valueOf(100))
                .minNotional(BigDecimal.valueOf(10))
                .baseAsset("BTC")
                .quoteAsset("USDT")
                .enabled(true)
                .build());
        return instruments;
    }

    @Override
    public com.smartquantify.common.model.Order placeOrder(OrderRequest request) {
        return com.smartquantify.common.model.Order.builder()
                .id(java.util.UUID.randomUUID().toString())
                .symbol(request.getSymbol())
                .side(request.getSide())
                .type(request.getType())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .status(com.smartquantify.common.enums.OrderStatus.FILLED)
                .filledQuantity(request.getQuantity())
                .remainingQuantity(BigDecimal.ZERO)
                .avgPrice(request.getPrice())
                .exchange(request.getExchange())
                .clientOrderId(request.getClientOrderId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public void cancelOrder(CancelOrderRequest request) {
    }

    @Override
    public com.smartquantify.common.model.Order getOrder(String symbol, String orderId) {
        return com.smartquantify.common.model.Order.builder()
                .id(orderId)
                .symbol(symbol)
                .side(com.smartquantify.common.enums.Side.BUY)
                .type(com.smartquantify.common.enums.OrderType.MARKET)
                .quantity(BigDecimal.valueOf(0.1))
                .price(BigDecimal.valueOf(45000))
                .status(com.smartquantify.common.enums.OrderStatus.FILLED)
                .filledQuantity(BigDecimal.valueOf(0.1))
                .remainingQuantity(BigDecimal.ZERO)
                .avgPrice(BigDecimal.valueOf(45000))
                .exchange(Exchange.BINANCE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public List<com.smartquantify.common.model.Order> getOpenOrders(String symbol) {
        return new ArrayList<>();
    }

    @Override
    public void subscribeKlines(String symbol, String interval, Consumer<Kline> handler) {
    }

    @Override
    public void subscribeOrderBook(String symbol, Consumer<OrderBook> handler) {
    }

    @Override
    public void unsubscribe(String symbol) {
    }
}