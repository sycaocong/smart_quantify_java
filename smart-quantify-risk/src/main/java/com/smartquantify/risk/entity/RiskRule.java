package com.smartquantify.risk.entity;

import com.smartquantify.common.enums.RiskRuleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 风控规则实体类
 * 设计文档: [DESIGN.md](../DESIGN.md#43-风控引擎)
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "risk_rule")
public class RiskRule {

    /**
     * 规则唯一标识
     */
    @Id
    private String id;

    /**
     * 规则名称
     */
    @Column(nullable = false)
    private String name;

    /**
     * 规则描述
     */
    @Column
    private String description;

    /**
     * 规则类型（POSITION_LIMIT/MAX_TRADE_SIZE/DRAWDOWN_LIMIT等）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskRuleType type;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled;

    /**
     * 优先级（数字越小优先级越高）
     */
    @Column(nullable = false)
    private Integer priority;

    /**
     * 规则条件（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String conditions;

    /**
     * 触发动作（JSON数组格式）
     */
    @Column(columnDefinition = "TEXT")
    private String actions;

    /**
     * 作用范围（global/user/strategy）
     */
    @Column(nullable = false)
    private String scope;

    /**
     * 适用策略ID列表（JSON数组格式）
     */
    @Column(columnDefinition = "TEXT")
    private String strategyIds;

    /**
     * 适用交易对列表（JSON数组格式）
     */
    @Column(columnDefinition = "TEXT")
    private String symbols;

    /**
     * 适用交易所列表（JSON数组格式）
     */
    @Column(columnDefinition = "TEXT")
    private String exchanges;

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
}