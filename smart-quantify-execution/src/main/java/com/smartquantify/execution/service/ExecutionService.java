package com.smartquantify.execution.service;

import com.smartquantify.adapter.AdapterFactory;
import com.smartquantify.adapter.ExchangeAdapter;
import com.smartquantify.execution.dto.OrderRequest;
import com.smartquantify.execution.entity.Order;
import com.smartquantify.execution.entity.Trade;
import com.smartquantify.execution.repository.OrderRepository;
import com.smartquantify.execution.repository.TradeRepository;
import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.OrderStatus;
import com.smartquantify.common.enums.OrderType;
import com.smartquantify.common.enums.Side;
import com.smartquantify.common.model.CancelOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionService {
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;

    @Transactional
    public Order submitOrder(OrderRequest request) {
        Exchange exchange = Exchange.valueOf(request.getExchange());
        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .symbol(request.getSymbol())
                .side(Side.valueOf(request.getSide()))
                .type(OrderType.valueOf(request.getType()))
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .status(OrderStatus.NEW)
                .filledQuantity(BigDecimal.ZERO)
                .remainingQuantity(request.getQuantity())
                .exchange(exchange)
                .clientOrderId(request.getClientOrderId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .strategyId(request.getStrategyId())
                .build();

        order = orderRepository.save(order);
        log.info("Order created: id={}, symbol={}, side={}", order.getId(), order.getSymbol(), order.getSide());

        try {
            ExchangeAdapter adapter = AdapterFactory.getAdapter(exchange);
            com.smartquantify.common.model.OrderRequest adapterRequest = com.smartquantify.common.model.OrderRequest.builder()
                    .exchange(exchange)
                    .symbol(request.getSymbol())
                    .side(Side.valueOf(request.getSide()))
                    .type(OrderType.valueOf(request.getType()))
                    .price(request.getPrice())
                    .quantity(request.getQuantity())
                    .clientOrderId(request.getClientOrderId())
                    .build();

            com.smartquantify.common.model.Order adapterOrder = adapter.placeOrder(adapterRequest);

            order.setStatus(adapterOrder.getStatus());
            order.setFilledQuantity(adapterOrder.getFilledQuantity());
            order.setRemainingQuantity(adapterOrder.getRemainingQuantity());
            order.setAvgPrice(adapterOrder.getAvgPrice());
            order.setUpdatedAt(LocalDateTime.now());
            order = orderRepository.save(order);

            if (order.getStatus() == OrderStatus.FILLED) {
                Trade trade = Trade.builder()
                        .id(UUID.randomUUID().toString())
                        .orderId(order.getId())
                        .symbol(order.getSymbol())
                        .side(order.getSide())
                        .price(order.getAvgPrice())
                        .quantity(order.getFilledQuantity())
                        .quoteQuantity(order.getAvgPrice().multiply(order.getFilledQuantity()))
                        .exchange(order.getExchange())
                        .createdAt(LocalDateTime.now())
                        .build();
                tradeRepository.save(trade);
                log.info("Trade created: id={}, orderId={}", trade.getId(), trade.getOrderId());
            }
        } catch (Exception e) {
            order.setStatus(OrderStatus.REJECTED);
            order.setUpdatedAt(LocalDateTime.now());
            order = orderRepository.save(order);
            log.error("Order execution failed: {}", e.getMessage());
        }

        return order;
    }

    public Order getOrder(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public List<Order> listOrders(String status, String symbol, String exchange) {
        List<Order> orders = new ArrayList<>();
        if (status != null && symbol != null) {
            orders = orderRepository.findByStatusAndSymbol(OrderStatus.valueOf(status), symbol);
        } else if (status != null) {
            orders = orderRepository.findByStatus(OrderStatus.valueOf(status));
        } else if (symbol != null) {
            orders = orderRepository.findBySymbol(symbol);
        } else if (exchange != null) {
            orders = orderRepository.findByExchange(Exchange.valueOf(exchange));
        } else {
            orders = orderRepository.findAll();
        }
        return orders;
    }

    @Transactional
    public Order cancelOrder(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.PARTIALLY_FILLED) {
            return order;
        }

        try {
            ExchangeAdapter adapter = AdapterFactory.getAdapter(order.getExchange());
            com.smartquantify.common.model.CancelOrderRequest cancelRequest =
                    com.smartquantify.common.model.CancelOrderRequest.builder()
                            .exchange(order.getExchange())
                            .symbol(order.getSymbol())
                            .orderId(order.getId())
                            .build();
            adapter.cancelOrder(cancelRequest);

            order.setStatus(OrderStatus.CANCELED);
            order.setUpdatedAt(LocalDateTime.now());
            order = orderRepository.save(order);
            log.info("Order canceled: id={}", order.getId());
        } catch (Exception e) {
            log.error("Order cancellation failed: {}", e.getMessage());
        }

        return order;
    }

    @Transactional
    public void syncOrders() {
        log.info("Syncing orders from exchanges...");
    }
}