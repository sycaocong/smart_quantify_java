package com.smartquantify.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单簿数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBook {

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 卖盘列表
     */
    private List<OrderBookLevel> asks;

    /**
     * 买盘列表
     */
    private List<OrderBookLevel> bids;

    /**
     * 订单簿档位内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderBookLevel {

        /**
         * 价格
         */
        private BigDecimal price;

        /**
         * 数量
         */
        private BigDecimal quantity;
    }
}