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

@RestController
@RequestMapping("/api/v1/strategies")
@RequiredArgsConstructor
public class StrategyController {
    private final StrategyService strategyService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createStrategy(@RequestBody StrategyRequest request) {
        StrategyResponse response = strategyService.createStrategy(request);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("strategy", response));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStrategy(@PathVariable String id) {
        StrategyResponse response = strategyService.getStrategy(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("strategy", response));
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listStrategies() {
        List<StrategyResponse> response = strategyService.listStrategies();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("strategies", response));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startStrategy(@PathVariable String id) {
        StrategyResponse response = strategyService.startStrategy(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("strategy", response));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopStrategy(@PathVariable String id) {
        StrategyResponse response = strategyService.stopStrategy(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("strategy", response));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<Map<String, Object>> pauseStrategy(@PathVariable String id) {
        StrategyResponse response = strategyService.pauseStrategy(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("strategy", response));
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateStrategy(@PathVariable String id, @RequestBody StrategyRequest request) {
        StrategyResponse response = strategyService.updateStrategy(id, request);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("strategy", response));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteStrategy(@PathVariable String id) {
        strategyService.deleteStrategy(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        return ResponseEntity.ok(result);
    }
}