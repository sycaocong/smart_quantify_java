package com.smartquantify.execution.entity;

import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.Side;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成交记录实体类
 * 记录订单的实际成交信息
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trade")
public class Trade {

    /**
     * 成交记录唯一标识
     */
    @Id
    private String id;

    /**
     * 关联订单ID
     */
    @Column(nullable = false)
    private String orderId;

    /**
     * 交易对
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
     * 成交价格
     */
    @Column(nullable = false)
    private BigDecimal price;

    /**
     * 成交数量
     */
    @Column(nullable = false)
    private BigDecimal quantity;

    /**
     * 成交金额（quote货币）
     */
    @Column(nullable = false)
    private BigDecimal quoteQuantity;

    /**
     * 交易所
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Exchange exchange;

    /**
     * 成交时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
}