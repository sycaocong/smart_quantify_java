package com.smartquantify.strategy.entity;

import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.StrategyStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 策略实体类
 * 设计文档: [DESIGN.md](../DESIGN.md#42-策略引擎)
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "strategy")
public class Strategy {

    /**
     * 策略唯一标识
     */
    @Id
    private String id;

    /**
     * 策略名称
     */
    @Column(nullable = false)
    private String name;

    /**
     * 策略描述
     */
    @Column
    private String description;

    /**
     * 策略类型（如：MACD、RSI、布林带等）
     */
    @Column(nullable = false)
    private String type;

    /**
     * 策略语言（Java/Python）
     */
    @Column(nullable = false)
    private String language;

    /**
     * 策略状态（RUNNING/STOPPED/PAUSED）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StrategyStatus status;

    /**
     * 策略参数（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String parameters;

    /**
     * 策略配置（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String config;

    /**
     * 交易所（BINANCE/OKX/BYBIT/HUOBI）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Exchange exchange;

    /**
     * 交易对列表（JSON数组格式）
     */
    @Column(columnDefinition = "TEXT")
    private String symbols;

    /**
     * K线周期（1m/5m/15m/1h/4h/1d）
     */
    @Column(nullable = false)
    private String interval;

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
     * 最后运行时间
     */
    @Column
    private LocalDateTime lastRunTime;

    /**
     * 策略统计数据（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String statistics;

    /**
     * 策略版本号
     */
    @Column(nullable = false)
    private String version;
}