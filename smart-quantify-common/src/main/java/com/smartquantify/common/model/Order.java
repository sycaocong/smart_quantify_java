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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private String id;
    
    private String symbol;
    
    private Side side;
    
    private OrderType type;
    
    private BigDecimal quantity;
    
    private BigDecimal price;
    
    private OrderStatus status;
    
    private BigDecimal filledQuantity;
    
    private BigDecimal remainingQuantity;
    
    private BigDecimal avgPrice;
    
    private Exchange exchange;
    
    private String clientOrderId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String strategyId;
}