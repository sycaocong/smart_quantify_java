package com.smartquantify.execution.entity;

import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.OrderStatus;
import com.smartquantify.common.enums.OrderType;
import com.smartquantify.common.enums.Side;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 设计文档: [DESIGN.md](../DESIGN.md#44-订单执行服务)
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    private String id;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Side side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private BigDecimal filledQuantity;

    @Column(nullable = false)
    private BigDecimal remainingQuantity;

    @Column
    private BigDecimal avgPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Exchange exchange;

    @Column
    private String clientOrderId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private String strategyId;
}