package com.smartquantify.common.enums;

/**
 * 合约类型枚举
 * 设计文档: [DESIGN.md](../DESIGN.md#51-通用枚举)
 */
public enum InstrumentType {
    /**
     * 现货
     */
    SPOT,
    /**
     * 期货
     */
    FUTURES,
    /**
     * 期权
     */
    OPTIONS
}