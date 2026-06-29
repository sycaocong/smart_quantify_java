package com.smartquantify.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskRuleRequest {
    private String name;
    private String description;
    private String type;
    private Boolean enabled;
    private Integer priority;
    private Map<String, String> conditions;
    private List<String> actions;
    private String scope;
    private List<String> strategyIds;
    private List<String> symbols;
    private List<String> exchanges;
}