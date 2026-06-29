package com.smartquantify.common.enums;

/**
 * 风控规则类型枚举
 * 设计文档: [DESIGN.md](../DESIGN.md#51-通用枚举)
 */
public enum RiskRuleType {
    POSITION_LIMIT,
    DRAWDOWN_LIMIT,
    RATE_LIMIT,
    MAX_TRADE_SIZE,
    VOLUME_LIMIT
}