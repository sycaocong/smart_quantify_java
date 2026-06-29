package com.smartquantify.backtest.entity;

import com.smartquantify.common.enums.BacktestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 回测任务实体类
 * 设计文档: [DESIGN.md](../DESIGN.md#45-回测服务)
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "backtest_task")
public class BacktestTask {
    @Id
    private String id;

    @Column(nullable = false)
    private String strategyId;

    @Column(nullable = false)
    private String strategyName;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String interval;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private BigDecimal initialCapital;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BacktestStatus status;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}