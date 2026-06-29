package com.smartquantify.execution.controller;

import com.smartquantify.execution.dto.OrderRequest;
import com.smartquantify.execution.entity.Order;
import com.smartquantify.execution.service.ExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单执行控制器
 * 设计文档: [DESIGN.md](../DESIGN.md#44-订单执行服务)
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class ExecutionController {
    private final ExecutionService executionService;

    /**
     * 提交订单
     * 设计文档: [DESIGN.md](../DESIGN.md#44-订单执行服务)
     * API文档: [API.md](../API.md#51-提交订单)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> submitOrder(@RequestBody OrderRequest request) {
        Order order = executionService.submitOrder(request);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("order", order));
        return ResponseEntity.ok(result);
    }

    /**
     * 获取订单详情
     * 设计文档: [DESIGN.md](../DESIGN.md#44-订单执行服务)
     * API文档: [API.md](../API.md#52-获取订单详情)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable String id) {
        Order order = executionService.getOrder(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("order", order));
        return ResponseEntity.ok(result);
    }

    /**
     * 获取订单列表
     * 设计文档: [DESIGN.md](../DESIGN.md#44-订单执行服务)
     * API文档: [API.md](../API.md#53-获取订单列表)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String exchange) {
        List<Order> orders = executionService.listOrders(status, symbol, exchange);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("orders", orders));
        return ResponseEntity.ok(result);
    }

    /**
     * 取消订单
     * 设计文档: [DESIGN.md](../DESIGN.md#44-订单执行服务)
     * API文档: [API.md](../API.md#54-取消订单)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable String id) {
        Order order = executionService.cancelOrder(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of("order", order));
        return ResponseEntity.ok(result);
    }

    /**
     * 同步订单
     * 设计文档: [DESIGN.md](../DESIGN.md#44-订单执行服务)
     * API文档: [API.md](../API.md#55-同步订单)
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncOrders() {
        executionService.syncOrders();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        return ResponseEntity.ok(result);
    }
}