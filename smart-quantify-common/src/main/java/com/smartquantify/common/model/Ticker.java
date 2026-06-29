package com.smartquantify.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticker {
    private String symbol;
    private BigDecimal lastPrice;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal volume24h;
    private BigDecimal quoteVolume24h;
    private BigDecimal priceChange24h;
    private BigDecimal priceChangePercent24h;
    private LocalDateTime timestamp;
}