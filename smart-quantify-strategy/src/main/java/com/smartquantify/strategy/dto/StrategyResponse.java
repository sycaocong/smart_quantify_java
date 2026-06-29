package com.smartquantify.strategy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyResponse {
    private String id;
    private String name;
    private String description;
    private String type;
    private String language;
    private String status;
    private String exchange;
    private String symbols;
    private String interval;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}