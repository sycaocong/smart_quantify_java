package com.smartquantify.common.model;

import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.OrderStatus;
import com.smartquantify.common.enums.OrderType;
import com.smartquantify.common.enums.Side;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * 订单唯一标识
     */
    private String id;
    
    /**
     * 交易对
     */
    private String symbol;
    
    /**
     * 交易方向（BUY/SELL）
     */
    private Side side;
    
    /**
     * 订单类型（MARKET/LIMIT/STOP等）
     */
    private OrderType type;
    
    /**
     * 订单数量
     */
    private BigDecimal quantity;
    
    /**
     * 订单价格
     */
    private BigDecimal price;
    
    /**
     * 订单状态
     */
    private OrderStatus status;
    
    /**
     * 已成交数量
     */
    private BigDecimal filledQuantity;
    
    /**
     * 剩余数量
     */
    private BigDecimal remainingQuantity;
    
    /**
     * 平均成交价格
     */
    private BigDecimal avgPrice;
    
    /**
     * 交易所
     */
    private Exchange exchange;
    
    /**
     * 客户端订单ID
     */
    private String clientOrderId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 关联策略ID
     */
    private String strategyId;
}