package com.smartquantify.common.model;

import com.smartquantify.common.enums.Exchange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 取消订单请求数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {

    /**
     * 交易所
     */
    private Exchange exchange;

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 客户端订单ID
     */
    private String clientOrderId;
}