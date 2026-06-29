package com.smartquantify.risk.controller;

import com.smartquantify.risk.dto.RiskCheckRequest;
import com.smartquantify.risk.dto.RiskCheckResponse;
import com.smartquantify.risk.dto.RiskRuleRequest;
import com.smartquantify.risk.entity.RiskLimit;
import com.smartquantify.risk.entity.RiskRule;
import com.smartquantify.risk.entity.RiskState;
import com.smartquantify.risk.service.RiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/risk")
@RequiredArgsConstructor
public class RiskController {
    private final RiskService riskService;

    @PostMapping("/rules")
    public ResponseEntity<Map<String, Object>> createRule(@RequestBody RiskRuleRequest request) {
        RiskRule rule = riskService.createRule(request);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("rule", rule));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/rules/{id}")
    public ResponseEntity<Map<String, Object>> getRule(@PathVariable String id) {
        RiskRule rule = riskService.getRule(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("rule", rule));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/rules")
    public ResponseEntity<Map<String, Object>> listRules() {
        List<RiskRule> rules = riskService.listRules();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("rules", rules));
        return ResponseEntity.ok(result);
    }

    @PutMapping("/rules/{id}")
    public ResponseEntity<Map<String, Object>> updateRule(@PathVariable String id, @RequestBody RiskRuleRequest request) {
        RiskRule rule = riskService.updateRule(id, request);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("rule", rule));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Map<String, Object>> deleteRule(@PathVariable String id) {
        riskService.deleteRule(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkRisk(@RequestBody RiskCheckRequest request) {
        RiskCheckResponse response = riskService.checkRisk(request);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("riskCheck", response));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/limits")
    public ResponseEntity<Map<String, Object>> getLimits(
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String strategyId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String exchange) {
        List<RiskLimit> limits = riskService.getLimits(scope, strategyId, symbol, exchange);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("limits", limits));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> getState(
            @RequestParam String exchange,
            @RequestParam String strategyId) {
        RiskState state = riskService.getState(exchange, strategyId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("state", state));
        return ResponseEntity.ok(result);
    }
}