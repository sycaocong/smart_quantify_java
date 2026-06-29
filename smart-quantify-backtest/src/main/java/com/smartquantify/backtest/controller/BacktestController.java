package com.smartquantify.backtest.controller;

import com.smartquantify.backtest.dto.BacktestRequest;
import com.smartquantify.backtest.dto.BacktestResult;
import com.smartquantify.backtest.entity.BacktestTask;
import com.smartquantify.backtest.service.BacktestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/backtests")
@RequiredArgsConstructor
public class BacktestController {
    private final BacktestService backtestService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBacktest(@RequestBody BacktestRequest request) {
        BacktestTask task = backtestService.createBacktest(request);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("backtest", task));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBacktest(@PathVariable String id) {
        BacktestTask task = backtestService.getBacktest(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("backtest", task));
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listBacktests() {
        List<BacktestTask> tasks = backtestService.listBacktests();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("backtests", tasks));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<Map<String, Object>> runBacktest(@PathVariable String id) {
        BacktestTask task = backtestService.runBacktest(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("backtest", task));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBacktest(@PathVariable String id) {
        BacktestTask task = backtestService.cancelBacktest(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("backtest", task));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<Map<String, Object>> getBacktestResult(@PathVariable String id) {
        BacktestResult result = backtestService.getBacktestResult(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", Map.of("result", result));
        return ResponseEntity.ok(response);
    }
}