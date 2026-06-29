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

/**
 * 回测服务控制器
 * 设计文档: [DESIGN.md](../DESIGN.md#45-回测服务)
 */
@RestController
@RequestMapping("/api/v1/backtests")
@RequiredArgsConstructor
public class BacktestController {
    private final BacktestService backtestService;

    /**
     * 创建回测任务
     * 设计文档: [DESIGN.md](../DESIGN.md#45-回测服务)
     * API文档: [API.md](../API.md#61-创建回测任务)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBacktest(@RequestBody BacktestRequest request) {
        BacktestTask task = backtestService.createBacktest(request);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("backtest", task));
        return ResponseEntity.ok(result);
    }

    /**
     * 获取回测任务详情
     * 设计文档: [DESIGN.md](../DESIGN.md#45-回测服务)
     * API文档: [API.md](../API.md#62-获取回测任务详情)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBacktest(@PathVariable String id) {
        BacktestTask task = backtestService.getBacktest(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("backtest", task));
        return ResponseEntity.ok(result);
    }

    /**
     * 获取回测任务列表
     * 设计文档: [DESIGN.md](../DESIGN.md#45-回测服务)
     * API文档: [API.md](../API.md#63-获取回测任务列表)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listBacktests() {
        List<BacktestTask> tasks = backtestService.listBacktests();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("backtests", tasks));
        return ResponseEntity.ok(result);
    }

    /**
     * 运行回测
     * 设计文档: [DESIGN.md](../DESIGN.md#45-回测服务)
     * API文档: [API.md](../API.md#64-运行回测)
     */
    @PostMapping("/{id}/run")
    public ResponseEntity<Map<String, Object>> runBacktest(@PathVariable String id) {
        BacktestTask task = backtestService.runBacktest(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("backtest", task));
        return ResponseEntity.ok(result);
    }

    /**
     * 取消回测
     * 设计文档: [DESIGN.md](../DESIGN.md#45-回测服务)
     * API文档: [API.md](../API.md#65-取消回测)
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBacktest(@PathVariable String id) {
        BacktestTask task = backtestService.cancelBacktest(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("backtest", task));
        return ResponseEntity.ok(result);
    }

    /**
     * 获取回测结果
     * 设计文档: [DESIGN.md](../DESIGN.md#45-回测服务)
     * API文档: [API.md](../API.md#66-获取回测结果)
     */
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