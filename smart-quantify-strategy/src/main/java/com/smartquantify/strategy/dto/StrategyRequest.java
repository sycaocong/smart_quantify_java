package com.smartquantify.strategy.dto;

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
public class StrategyRequest {
    private String name;
    private String description;
    private String type;
    private String language;
    private String exchange;
    private List<String> symbols;
    private String interval;
    private Map<String, String> parameters;
    private Map<String, String> config;
}