package com.smartquantify.risk.repository;

import com.smartquantify.risk.entity.RiskState;
import com.smartquantify.common.enums.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskStateRepository extends JpaRepository<RiskState, String> {
    Optional<RiskState> findByExchangeAndStrategyId(Exchange exchange, String strategyId);
}