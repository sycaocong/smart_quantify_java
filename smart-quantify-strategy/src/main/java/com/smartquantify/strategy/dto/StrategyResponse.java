package com.smartquantify.strategy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 策略响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyResponse {

    /**
     * 策略唯一标识
     */
    private String id;

    /**
     * 策略名称
     */
    private String name;

    /**
     * 策略描述
     */
    private String description;

    /**
     * 策略类型
     */
    private String type;

    /**
     * 策略语言
     */
    private String language;

    /**
     * 策略状态
     */
    private String status;

    /**
     * 交易所
     */
    private String exchange;

    /**
     * 交易对列表（JSON格式）
     */
    private String symbols;

    /**
     * K线周期
     */
    private String interval;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}