package com.smartquantify.risk.entity;

import com.smartquantify.common.enums.Exchange;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风险状态实体类
 * 实时追踪策略的风险状态指标
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "risk_state")
public class RiskState {

    /**
     * 状态唯一标识
     */
    @Id
    private String id;

    /**
     * 交易所
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Exchange exchange;

    /**
     * 关联策略ID
     */
    @Column(nullable = false)
    private String strategyId;

    /**
     * 当前持仓金额
     */
    @Column
    private BigDecimal currentPosition;

    /**
     * 当前回撤率
     */
    @Column
    private BigDecimal currentDrawdown;

    /**
     * 最近一分钟订单数
     */
    @Column
    private Integer ordersInLastMinute;

    /**
     * 当日交易量
     */
    @Column
    private BigDecimal dailyVolume;

    /**
     * 最后检查时间
     */
    @Column(nullable = false)
    private LocalDateTime lastCheckTime;

    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}