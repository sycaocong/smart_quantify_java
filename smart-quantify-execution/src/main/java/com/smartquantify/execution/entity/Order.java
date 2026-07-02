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

    /**
     * 订单唯一标识
     */
    @Id
    private String id;

    /**
     * 交易对（如 BTC_USDT）
     */
    @Column(nullable = false)
    private String symbol;

    /**
     * 交易方向（BUY/SELL）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Side side;

    /**
     * 订单类型（LIMIT/MARKET/STOP_LOSS等）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;

    /**
     * 订单数量
     */
    @Column(nullable = false)
    private BigDecimal quantity;

    /**
     * 订单价格（市价单可为空）
     */
    @Column
    private BigDecimal price;

    /**
     * 订单状态（NEW/FILLED/CANCELED等）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /**
     * 已成交数量
     */
    @Column(nullable = false)
    private BigDecimal filledQuantity;

    /**
     * 剩余未成交数量
     */
    @Column(nullable = false)
    private BigDecimal remainingQuantity;

    /**
     * 平均成交价格
     */
    @Column
    private BigDecimal avgPrice;

    /**
     * 交易所（BINANCE/OKX/BYBIT/HUOBI）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Exchange exchange;

    /**
     * 客户端订单ID（用户自定义）
     */
    @Column
    private String clientOrderId;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 关联策略ID
     */
    @Column
    private String strategyId;
}