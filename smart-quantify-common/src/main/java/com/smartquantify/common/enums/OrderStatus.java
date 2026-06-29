package com.smartquantify.common.enums;

/**
 * 订单状态枚举
 * 设计文档: [DESIGN.md](../DESIGN.md#51-通用枚举)
 */
public enum OrderStatus {
    NEW,
    PARTIALLY_FILLED,
    FILLED,
    CANCELED,
    REJECTED
}