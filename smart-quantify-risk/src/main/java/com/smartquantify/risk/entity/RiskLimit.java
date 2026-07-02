package com.smartquantify.risk.entity;

import com.smartquantify.common.enums.Exchange;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 风险限制实体类
 * 定义各种风险阈值限制
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "risk_limit")
public class RiskLimit {

    /**
     * 限制唯一标识
     */
    @Id
    private String id;

    /**
     * 作用范围（global/user/strategy）
     */
    @Column(nullable = false)
    private String scope;

    /**
     * 关联策略ID（可选）
     */
    @Column
    private String strategyId;

    /**
     * 适用交易对（可选）
     */
    @Column
    private String symbol;

    /**
     * 适用交易所（可选）
     */
    @Enumerated(EnumType.STRING)
    @Column
    private Exchange exchange;

    /**
     * 最大持仓限制
     */
    @Column
    private BigDecimal maxPosition;

    /**
     * 最大回撤限制
     */
    @Column
    private BigDecimal maxDrawdown;

    /**
     * 每分钟最大订单数
     */
    @Column
    private Integer maxOrdersPerMinute;

    /**
     * 单笔最大交易金额
     */
    @Column
    private BigDecimal maxTradeSize;

    /**
     * 每日最大交易量
     */
    @Column
    private BigDecimal maxDailyVolume;
}