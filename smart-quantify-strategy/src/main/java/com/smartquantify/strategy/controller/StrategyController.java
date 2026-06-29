package com.smartquantify.strategy.controller;

import com.smartquantify.strategy.dto.StrategyRequest;
import com.smartquantify.strategy.dto.StrategyResponse;
import com.smartquantify.strategy.service.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 策略管理控制器
 * 设计文档: [DESIGN.md](../DESIGN.md#42-策略引擎)
 */
@RestController
@RequestMapping("/api/v1/strategies")
@RequiredArgsConstructor
public class StrategyController {
    private final StrategyService strategyService;

    /**
     * 创建策略
     * 设计文档: [DESIGN.md](../DESIGN.md#42-策略引擎)
     * API文档: [API.md](../API.md#31-创建策略)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createStrategy(@RequestBody StrategyRequest request) {
        StrategyResponse response = strategyService.createStrategy(request);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("strategy", response));
        return ResponseEntity.ok(result);
    }

    /**
     * 获取策略详情
     * 设计文档: [DESIGN.md](../DESIGN.md#42-策略引擎)
     * API文档: [API.md](../API.md#32-获取策略详情)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStrategy(@PathVariable String id) {
        StrategyResponse response = strategyService.getStrategy(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("strategy", response));
        return ResponseEntity.ok(result);
    }

    /**
     * 获取策略列表
     * 设计文档: [DESIGN.md](../DESIGN.md#42-策略引擎)
     * API文档: [API.md](../API.md#33-获取策略列表)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listStrategies() {
        List<StrategyResponse> response = strategyService.listStrategies();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("strategies", response));
        return ResponseEntity.ok(result);
    }

    /**
     * 启动策略
     * 设计文档: [DESIGN.md](../DESIGN.md#42-策略引擎)
     * API文档: [API.md](../API.md#34-启动策略)
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startStrategy(@PathVariable String id) {
        StrategyResponse response = strategyService.startStrategy(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("strategy", response));
        return ResponseEntity.ok(result);
    }

    /**
     * 停止策略
     * 设计文档: [DESIGN.md](../DESIGN.md#42-策略引擎)
     * API文档: [API.md](../API.md#35-停止策略)
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopStrategy(@PathVariable String id) {
        StrategyResponse response = strategyService.stopStrategy(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("strategy", response));
        return ResponseEntity.ok(result);
    }

    /**
     * 暂停策略
     * 设计文档: [DESIGN.md](../DESIGN.md#42-策略引擎)
     * API文档: [API.md](../API.md#36-暂停策略)
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<Map<String, Object>> pauseStrategy(@PathVariable String id) {
        StrategyResponse response = strategyService.pauseStrategy(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("strategy", response));
        return ResponseEntity.ok(result);
    }

    /**
     * 更新策略
     * 设计文档: [DESIGN.md](../DESIGN.md#42-策略引擎)
     * API文档: [API.md](../API.md#37-更新策略)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateStrategy(@PathVariable String id, @RequestBody StrategyRequest request) {
        StrategyResponse response = strategyService.updateStrategy(id, request);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("strategy", response));
        return ResponseEntity.ok(result);
    }

    /**
     * 删除策略
     * 设计文档: [DESIGN.md](../DESIGN.md#42-策略引擎)
     * API文档: [API.md](../API.md#38-删除策略)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteStrategy(@PathVariable String id) {
        strategyService.deleteStrategy(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        return ResponseEntity.ok(result);
    }
}