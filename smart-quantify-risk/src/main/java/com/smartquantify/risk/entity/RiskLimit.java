package com.smartquantify.risk.entity;

import com.smartquantify.common.enums.Exchange;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "risk_limit")
public class RiskLimit {
    @Id
    private String id;

    @Column(nullable = false)
    private String scope;

    @Column
    private String strategyId;

    @Column
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column
    private Exchange exchange;

    @Column
    private BigDecimal maxPosition;

    @Column
    private BigDecimal maxDrawdown;

    @Column
    private Integer maxOrdersPerMinute;

    @Column
    private BigDecimal maxTradeSize;

    @Column
    private BigDecimal maxDailyVolume;
}