package com.smartquantify.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private String orderId;
    private String symbol;
    private String side;
    private String type;
    private BigDecimal quantity;
    private BigDecimal price;
    private String exchange;
    private String clientOrderId;
    private String strategyId;
    private LocalDateTime createdAt;
}