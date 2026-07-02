package com.smartquantify.common.enums;

/**
 * 订单类型枚举
 * 设计文档: [DESIGN.md](../DESIGN.md#51-通用枚举)
 */
public enum OrderType {
    /**
     * 市价单
     */
    MARKET,
    /**
     * 限价单
     */
    LIMIT,
    /**
     * 止损单
     */
    STOP,
    /**
     * 止损限价单
     */
    STOP_LIMIT
}