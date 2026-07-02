package com.smartquantify.backtest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 回测请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestRequest {

    /**
     * 策略ID
     */
    private String strategyId;

    /**
     * 策略名称
     */
    private String strategyName;

    /**
     * 交易对
     */
    private String symbol;

    /**
     * K线周期
     */
    private String interval;

    /**
     * 回测开始时间
     */
    private LocalDateTime startTime;

    /**
     * 回测结束时间
     */
    private LocalDateTime endTime;

    /**
     * 初始资金
     */
    private BigDecimal initialCapital;

    /**
     * 策略参数
     */
    private Map<String, String> parameters;
}