package com.smartquantify.risk.entity;

import com.smartquantify.common.enums.Exchange;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "risk_state")
public class RiskState {
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Exchange exchange;

    @Column(nullable = false)
    private String strategyId;

    @Column
    private BigDecimal currentPosition;

    @Column
    private BigDecimal currentDrawdown;

    @Column
    private Integer ordersInLastMinute;

    @Column
    private BigDecimal dailyVolume;

    @Column(nullable = false)
    private LocalDateTime lastCheckTime;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}