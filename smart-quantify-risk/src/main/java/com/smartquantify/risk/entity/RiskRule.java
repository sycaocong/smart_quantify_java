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
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskRuleType type;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(nullable = false)
    private Integer priority;

    @Column(columnDefinition = "TEXT")
    private String conditions;

    @Column(columnDefinition = "TEXT")
    private String actions;

    @Column(nullable = false)
    private String scope;

    @Column(columnDefinition = "TEXT")
    private String strategyIds;

    @Column(columnDefinition = "TEXT")
    private String symbols;

    @Column(columnDefinition = "TEXT")
    private String exchanges;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}