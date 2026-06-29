package com.smartquantify.common.enums;

/**
 * 回测状态枚举
 * 设计文档: [DESIGN.md](../DESIGN.md#51-通用枚举)
 */
public enum BacktestStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}