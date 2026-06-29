package com.smartquantify.common.model;

import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.OrderType;
import com.smartquantify.common.enums.Side;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private Exchange exchange;
    private String symbol;
    private Side side;
    private OrderType type;
    private String timeInForce;
    private BigDecimal price;
    private BigDecimal stopPrice;
    private BigDecimal quantity;
    private String clientOrderId;
}