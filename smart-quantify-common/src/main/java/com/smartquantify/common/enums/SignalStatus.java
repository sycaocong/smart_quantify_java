package com.smartquantify.common.enums;

/**
 * 信号状态枚举
 * 设计文档: [DESIGN.md](../DESIGN.md#51-通用枚举)
 */
public enum SignalStatus {
    /**
     * 待处理
     */
    PENDING,
    /**
     * 已接受
     */
    ACCEPTED,
    /**
     * 已拒绝
     */
    REJECTED,
    /**
     * 已执行
     */
    EXECUTED
}