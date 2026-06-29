package com.smartquantify.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBook {
    private String symbol;
    private Long timestamp;
    private List<OrderBookLevel> asks;
    private List<OrderBookLevel> bids;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderBookLevel {
        private BigDecimal price;
        private BigDecimal quantity;
    }
}