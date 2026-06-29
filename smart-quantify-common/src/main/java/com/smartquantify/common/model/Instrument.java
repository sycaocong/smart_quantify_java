package com.smartquantify.common.model;

import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.InstrumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Instrument {
    private String symbol;
    private InstrumentType type;
    private Exchange exchange;
    private BigDecimal tickSize;
    private BigDecimal lotSize;
    private BigDecimal minQuantity;
    private BigDecimal maxQuantity;
    private BigDecimal minNotional;
    private String baseAsset;
    private String quoteAsset;
    private Boolean enabled;
}