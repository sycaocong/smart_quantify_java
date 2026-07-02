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

    /**
     * 任务唯一标识
     */
    @Id
    private String id;

    /**
     * 关联策略ID
     */
    @Column(nullable = false)
    private String strategyId;

    /**
     * 策略名称
     */
    @Column(nullable = false)
    private String strategyName;

    /**
     * 交易对
     */
    @Column(nullable = false)
    private String symbol;

    /**
     * K线周期
     */
    @Column(nullable = false)
    private String interval;

    /**
     * 回测开始时间
     */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /**
     * 回测结束时间
     */
    @Column(nullable = false)
    private LocalDateTime endTime;

    /**
     * 初始资金
     */
    @Column(nullable = false)
    private BigDecimal initialCapital;

    /**
     * 回测状态（PENDING/RUNNING/COMPLETED/FAILED/CANCELLED）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BacktestStatus status;

    /**
     * 策略参数（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String parameters;

    /**
     * 回测结果（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String result;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 完成时间
     */
    @Column
    private LocalDateTime completedAt;

    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}