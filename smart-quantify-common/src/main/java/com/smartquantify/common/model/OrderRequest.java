package com.smartquantify.common.model;

import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.OrderType;
import com.smartquantify.common.enums.Side;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 下单请求数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    /**
     * 交易所
     */
    private Exchange exchange;

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 交易方向（BUY/SELL）
     */
    private Side side;

    /**
     * 订单类型
     */
    private OrderType type;

    /**
     * 订单有效期（GTC/IOC/FOK）
     */
    private String timeInForce;

    /**
     * 订单价格
     */
    private BigDecimal price;

    /**
     * 止损价格
     */
    private BigDecimal stopPrice;

    /**
     * 订单数量
     */
    private BigDecimal quantity;

    /**
     * 客户端订单ID
     */
    private String clientOrderId;
}