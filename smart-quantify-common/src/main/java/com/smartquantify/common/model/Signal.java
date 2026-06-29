package com.smartquantify.common.model;

import com.smartquantify.common.enums.*;
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
public class Signal {
    private String id;
    private String strategyId;
    private String strategyName;
    private String symbol;
    private Side side;
    private SignalType type;
    private BigDecimal price;
    private BigDecimal quantity;
    private Exchange exchange;
    private InstrumentType instrumentType;
    private Integer priority;
    private LocalDateTime createdAt;
    private SignalStatus status;
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
}