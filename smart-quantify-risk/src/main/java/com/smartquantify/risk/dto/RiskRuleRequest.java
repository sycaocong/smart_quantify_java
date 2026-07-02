package com.smartquantify.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 风控规则创建/更新请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskRuleRequest {

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 规则类型（POSITION_LIMIT/MAX_TRADE_SIZE/DRAWDOWN_LIMIT等）
     */
    private String type;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 规则条件
     */
    private Map<String, String> conditions;

    /**
     * 触发动作列表
     */
    private List<String> actions;

    /**
     * 作用范围（global/user/strategy）
     */
    private String scope;

    /**
     * 适用策略ID列表
     */
    private List<String> strategyIds;

    /**
     * 适用交易对列表
     */
    private List<String> symbols;

    /**
     * 适用交易所列表
     */
    private List<String> exchanges;
}