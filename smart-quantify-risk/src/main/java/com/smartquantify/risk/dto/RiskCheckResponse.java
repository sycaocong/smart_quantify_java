package com.smartquantify.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskCheckResponse {
    private Boolean passed;
    private List<RuleCheckResult> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleCheckResult {
        private Boolean passed;
        private String ruleId;
        private String ruleName;
        private String action;
    }
}