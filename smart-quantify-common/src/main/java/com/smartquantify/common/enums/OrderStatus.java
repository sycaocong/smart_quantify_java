package com.smartquantify.common.enums;

/**
 * 订单状态枚举
 * 设计文档: [DESIGN.md](../DESIGN.md#51-通用枚举)
 */
public enum OrderStatus {
    /**
     * 新建
     */
    NEW,
    /**
     * 部分成交
     */
    PARTIALLY_FILLED,
    /**
     * 完全成交
     */
    FILLED,
    /**
     * 已取消
     */
    CANCELED,
    /**
     * 已拒绝
     */
    REJECTED
}