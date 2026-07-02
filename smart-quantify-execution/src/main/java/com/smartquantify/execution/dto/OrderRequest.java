package com.smartquantify.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单提交请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    /**
     * 交易对（如 BTC_USDT）
     */
    private String symbol;

    /**
     * 交易方向（BUY/SELL）
     */
    private String side;

    /**
     * 订单类型（LIMIT/MARKET/STOP_LOSS等）
     */
    private String type;

    /**
     * 订单数量
     */
    private BigDecimal quantity;

    /**
     * 订单价格（市价单可不填）
     */
    private BigDecimal price;

    /**
     * 止损/止盈价格
     */
    private BigDecimal stopPrice;

    /**
     * 订单有效期（GTC/IOC/FOK等）
     */
    private String timeInForce;

    /**
     * 客户端订单ID（用户自定义）
     */
    private String clientOrderId;

    /**
     * 交易所（BINANCE/OKX/BYBIT/HUOBI）
     */
    private String exchange;

    /**
     * 关联策略ID
     */
    private String strategyId;
}