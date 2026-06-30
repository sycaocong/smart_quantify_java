package com.smartquantify.market.service;

import com.smartquantify.adapter.AdapterFactory;
import com.smartquantify.adapter.ExchangeAdapter;
import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.InstrumentType;
import com.smartquantify.common.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketService {

    private final AdapterFactory adapterFactory;

    @Cacheable(value = "klines", key = "#exchange + ':' + #symbol + ':' + #interval + ':' + #limit")
    public List<Kline> getKlines(String exchange, String symbol, String interval, Integer limit) {
        try {
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));
            return adapter.getKlines(symbol, interval, limit != null ? limit : 100);
        } catch (Exception e) {
            log.error("Failed to get klines: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Cacheable(value = "orderBook", key = "#exchange + ':' + #symbol + ':' + #limit")
    public OrderBook getOrderBook(String exchange, String symbol, Integer limit) {
        try {
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));
            return adapter.getOrderBook(symbol, limit != null ? limit : 100);
        } catch (Exception e) {
            log.error("Failed to get order book: {}", e.getMessage());
            return null;
        }
    }

    @Cacheable(value = "ticker", key = "#exchange + ':' + #symbol")
    public Ticker getTicker(String exchange, String symbol) {
        try {
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));
            return adapter.getTicker(symbol);
        } catch (Exception e) {
            log.error("Failed to get ticker: {}", e.getMessage());
            return null;
        }
    }

    public List<Ticker> getAllTickers(String exchange) {
        try {
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));
            List<Instrument> instruments = getInstruments(exchange, null);
            List<Ticker> tickers = new ArrayList<>();
            for (Instrument instrument : instruments) {
                Ticker ticker = getTicker(exchange, instrument.getSymbol());
                if (ticker != null) {
                    tickers.add(ticker);
                }
            }
            return tickers;
        } catch (Exception e) {
            log.error("Failed to get all tickers: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Cacheable(value = "instruments", key = "#exchange + ':' + #type")
    public List<Instrument> getInstruments(String exchange, String type) {
        try {
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));
            List<Instrument> instruments = adapter.getInstruments();
            if (type != null) {
                InstrumentType instrumentType = InstrumentType.valueOf(type.toUpperCase());
                return instruments.stream()
                        .filter(i -> i.getType() == instrumentType)
                        .collect(Collectors.toList());
            }
            return instruments;
        } catch (Exception e) {
            log.error("Failed to get instruments: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public void subscribeKlines(String exchange, String symbol, String interval) {
        try {
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));
            adapter.subscribeKlines(symbol, interval, kline -> {
                log.info("Received kline: symbol={}, interval={}, close={}",
                        kline.getSymbol(), kline.getInterval(), kline.getClose());
            });
        } catch (Exception e) {
            log.error("Failed to subscribe klines: {}", e.getMessage());
        }
    }

    public void subscribeOrderBook(String exchange, String symbol) {
        try {
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));
            adapter.subscribeOrderBook(symbol, orderBook -> {
                log.info("Received order book: symbol={}, asks={}, bids={}",
                        orderBook.getSymbol(), orderBook.getAsks().size(), orderBook.getBids().size());
            });
        } catch (Exception e) {
            log.error("Failed to subscribe order book: {}", e.getMessage());
        }
    }

    public void unsubscribe(String exchange, String symbol) {
        try {
            ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.valueOf(exchange));
            adapter.unsubscribe(symbol);
        } catch (Exception e) {
            log.error("Failed to unsubscribe: {}", e.getMessage());
        }
    }
}