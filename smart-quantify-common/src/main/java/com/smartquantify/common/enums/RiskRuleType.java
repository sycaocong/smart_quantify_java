package com.smartquantify.common.enums;

/**
 * 风控规则类型枚举
 * 设计文档: [DESIGN.md](../DESIGN.md#51-通用枚举)
 */
public enum RiskRuleType {
    /**
     * 持仓限制
     */
    POSITION_LIMIT,
    /**
     * 回撤限制
     */
    DRAWDOWN_LIMIT,
    /**
     * 频率限制
     */
    RATE_LIMIT,
    /**
     * 单笔最大交易金额
     */
    MAX_TRADE_SIZE,
    /**
     * 交易量限制
     */
    VOLUME_LIMIT
}