package com.smartquantify.common.model;

import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.InstrumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 合约/交易对数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Instrument {

    /**
     * 交易对符号
     */
    private String symbol;

    /**
     * 合约类型
     */
    private InstrumentType type;

    /**
     * 交易所
     */
    private Exchange exchange;

    /**
     * 最小价格变动单位
     */
    private BigDecimal tickSize;

    /**
     * 最小下单数量单位
     */
    private BigDecimal lotSize;

    /**
     * 最小下单数量
     */
    private BigDecimal minQuantity;

    /**
     * 最大下单数量
     */
    private BigDecimal maxQuantity;

    /**
     * 最小下单金额
     */
    private BigDecimal minNotional;

    /**
     * 基础资产
     */
    private String baseAsset;

    /**
     * 计价资产
     */
    private String quoteAsset;

    /**
     * 是否启用
     */
    private Boolean enabled;
}