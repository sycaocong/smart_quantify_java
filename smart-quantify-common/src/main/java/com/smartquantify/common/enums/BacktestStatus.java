package com.smartquantify.common.enums;

/**
 * 回测状态枚举
 * 设计文档: [DESIGN.md](../DESIGN.md#51-通用枚举)
 */
public enum BacktestStatus {
    /**
     * 等待中
     */
    PENDING,
    /**
     * 运行中
     */
    RUNNING,
    /**
     * 已完成
     */
    COMPLETED,
    /**
     * 失败
     */
    FAILED,
    /**
     * 已取消
     */
    CANCELLED
}