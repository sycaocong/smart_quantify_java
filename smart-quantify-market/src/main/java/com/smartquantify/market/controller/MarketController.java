package com.smartquantify.market.controller;

import com.smartquantify.common.model.Instrument;
import com.smartquantify.common.model.Kline;
import com.smartquantify.common.model.OrderBook;
import com.smartquantify.common.model.Ticker;
import com.smartquantify.market.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
public class MarketController {
    private final MarketService marketService;

    @GetMapping("/klines")
    public ResponseEntity<Map<String, Object>> getKlines(
            @RequestParam String exchange,
            @RequestParam String symbol,
            @RequestParam String interval,
            @RequestParam(required = false) Integer limit) {
        List<Kline> klines = marketService.getKlines(exchange, symbol, interval, limit);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("klines", klines));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/orderbook")
    public ResponseEntity<Map<String, Object>> getOrderBook(
            @RequestParam String exchange,
            @RequestParam String symbol,
            @RequestParam(required = false) Integer limit) {
        OrderBook orderBook = marketService.getOrderBook(exchange, symbol, limit);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("orderBook", orderBook));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ticker")
    public ResponseEntity<Map<String, Object>> getTicker(
            @RequestParam String exchange,
            @RequestParam(required = false) String symbol) {
        Map<String, Object> result = new HashMap<>();
        if (symbol != null) {
            Ticker ticker = marketService.getTicker(exchange, symbol);
            result.put("data", Map.of("ticker", ticker));
        } else {
            List<Ticker> tickers = marketService.getAllTickers(exchange);
            result.put("data", Map.of("tickers", tickers));
        }
        result.put("code", 200);
        result.put("message", "success");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/instruments")
    public ResponseEntity<Map<String, Object>> getInstruments(
            @RequestParam String exchange,
            @RequestParam(required = false) String type) {
        List<Instrument> instruments = marketService.getInstruments(exchange, type);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("instruments", instruments));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/subscribe/klines")
    public ResponseEntity<Map<String, Object>> subscribeKlines(
            @RequestParam String exchange,
            @RequestParam String symbol,
            @RequestParam String interval) {
        marketService.subscribeKlines(exchange, symbol, interval);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/subscribe/orderbook")
    public ResponseEntity<Map<String, Object>> subscribeOrderBook(
            @RequestParam String exchange,
            @RequestParam String symbol) {
        marketService.subscribeOrderBook(exchange, symbol);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, Object>> unsubscribe(
            @RequestParam String exchange,
            @RequestParam String symbol) {
        marketService.unsubscribe(exchange, symbol);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        return ResponseEntity.ok(result);
    }
}