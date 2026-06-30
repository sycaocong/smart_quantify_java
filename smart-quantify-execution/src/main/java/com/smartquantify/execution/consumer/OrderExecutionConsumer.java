package com.smartquantify.execution.consumer;

import com.smartquantify.adapter.AdapterFactory;
import com.smartquantify.adapter.ExchangeAdapter;
import com.smartquantify.common.event.OrderCreatedEvent;
import com.smartquantify.common.event.OrderExecutedEvent;
import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.OrderStatus;
import com.smartquantify.common.enums.OrderType;
import com.smartquantify.common.enums.Side;
import com.smartquantify.common.model.OrderRequest;
import com.smartquantify.execution.entity.Order;
import com.smartquantify.execution.entity.Trade;
import com.smartquantify.execution.repository.OrderRepository;
import com.smartquantify.execution.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExecutionConsumer {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final AdapterFactory adapterFactory;
    private final KafkaTemplate<String, OrderExecutedEvent> orderExecutedKafkaTemplate;

    @KafkaListener(topics = "order-created", groupId = "execution-consumer", concurrency = "3")
    @Transactional
    public void consumeOrderCreated(OrderCreatedEvent event) {
        log.info("Received order creation event: orderId={}, symbol={}", event.getOrderId(), event.getSymbol());

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            log.error("Order not found: {}", event.getOrderId());
            return;
        }

        try {
            Exchange exchange = Exchange.valueOf(event.getExchange());
            ExchangeAdapter adapter = adapterFactory.getAdapter(exchange);

            OrderRequest adapterRequest = OrderRequest.builder()
                    .exchange(exchange)
                    .symbol(event.getSymbol())
                    .side(Side.valueOf(event.getSide()))
                    .type(OrderType.valueOf(event.getType()))
                    .price(event.getPrice())
                    .quantity(event.getQuantity())
                    .clientOrderId(event.getClientOrderId())
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
                        .id(java.util.UUID.randomUUID().toString())
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

            OrderExecutedEvent executedEvent = OrderExecutedEvent.builder()
                    .orderId(order.getId())
                    .status(order.getStatus().name())
                    .filledQuantity(order.getFilledQuantity())
                    .remainingQuantity(order.getRemainingQuantity())
                    .avgPrice(order.getAvgPrice())
                    .updatedAt(order.getUpdatedAt())
                    .build();
            orderExecutedKafkaTemplate.send("order-executed", order.getId(), executedEvent);

            log.info("Order executed: id={}, status={}", order.getId(), order.getStatus());

        } catch (Exception e) {
            order.setStatus(OrderStatus.REJECTED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            log.error("Order execution failed: orderId={}, error={}", event.getOrderId(), e.getMessage());

            OrderExecutedEvent executedEvent = OrderExecutedEvent.builder()
                    .orderId(order.getId())
                    .status(OrderStatus.REJECTED.name())
                    .filledQuantity(BigDecimal.ZERO)
                    .remainingQuantity(order.getQuantity())
                    .updatedAt(order.getUpdatedAt())
                    .build();
            orderExecutedKafkaTemplate.send("order-executed", order.getId(), executedEvent);
        }
    }
}