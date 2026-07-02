package com.smartquantify.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * K线数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kline {

    /**
     * 交易对
     */
    private String symbol;

    /**
     * K线周期（如 1m, 5m, 1h, 1d）
     */
    private String interval;

    /**
     * 开盘时间
     */
    private LocalDateTime openTime;

    /**
     * 开盘价
     */
    private BigDecimal open;

    /**
     * 最高价
     */
    private BigDecimal high;

    /**
     * 最低价
     */
    private BigDecimal low;

    /**
     * 收盘价
     */
    private BigDecimal close;

    /**
     * 成交量（基础资产）
     */
    private BigDecimal volume;

    /**
     * 成交额（计价资产）
     */
    private BigDecimal quoteVolume;

    /**
     * 收盘时间
     */
    private LocalDateTime closeTime;
}