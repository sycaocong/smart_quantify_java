package com.smartquantify.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskCheckRequest {
    private String strategyId;
    private String symbol;
    private String side;
    private BigDecimal quantity;
    private BigDecimal price;
    private String exchange;
}