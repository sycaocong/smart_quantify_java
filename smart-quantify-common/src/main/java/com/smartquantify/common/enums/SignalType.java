package com.smartquantify.common.enums;

/**
 * 信号类型枚举
 * 设计文档: [DESIGN.md](../DESIGN.md#51-通用枚举)
 */
public enum SignalType {
    /**
     * 入场信号
     */
    ENTRY,
    /**
     * 出场信号
     */
    EXIT,
    /**
     * 止损信号
     */
    STOP_LOSS,
    /**
     * 止盈信号
     */
    TAKE_PROFIT
}