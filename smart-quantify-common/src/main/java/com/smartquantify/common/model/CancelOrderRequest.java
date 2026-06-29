package com.smartquantify.common.model;

import com.smartquantify.common.enums.Exchange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {
    private Exchange exchange;
    private String symbol;
    private String orderId;
    private String clientOrderId;
}