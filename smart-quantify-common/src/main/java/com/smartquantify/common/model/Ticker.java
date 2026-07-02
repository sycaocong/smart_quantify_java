package com.smartquantify.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ticker数据模型
 * 包含交易对的实时行情信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticker {

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 最新成交价
     */
    private BigDecimal lastPrice;

    /**
     * 24小时开盘价
     */
    private BigDecimal openPrice;

    /**
     * 24小时最高价
     */
    private BigDecimal highPrice;

    /**
     * 24小时最低价
     */
    private BigDecimal lowPrice;

    /**
     * 24小时成交量（基础资产）
     */
    private BigDecimal volume24h;

    /**
     * 24小时成交额（计价资产）
     */
    private BigDecimal quoteVolume24h;

    /**
     * 24小时价格变动
     */
    private BigDecimal priceChange24h;

    /**
     * 24小时价格变动百分比
     */
    private BigDecimal priceChangePercent24h;

    /**
     * 更新时间
     */
    private LocalDateTime timestamp;
}