package com.smartquantify.common.model;

import com.smartquantify.common.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易信号数据模型
 * 策略生成的交易指令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Signal {

    /**
     * 信号唯一标识
     */
    private String id;

    /**
     * 关联策略ID
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
     * 交易方向（BUY/SELL）
     */
    private Side side;

    /**
     * 信号类型（MARKET/LIMIT/STOP等）
     */
    private SignalType type;

    /**
     * 目标价格
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 目标交易所
     */
    private Exchange exchange;

    /**
     * 合约类型
     */
    private InstrumentType instrumentType;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 信号状态
     */
    private SignalStatus status;

    /**
     * 止损价格
     */
    private BigDecimal stopLoss;

    /**
     * 止盈价格
     */
    private BigDecimal takeProfit;
}