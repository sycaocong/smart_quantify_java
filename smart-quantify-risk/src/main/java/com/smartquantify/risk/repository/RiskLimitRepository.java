package com.smartquantify.risk.repository;

import com.smartquantify.risk.entity.RiskLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskLimitRepository extends JpaRepository<RiskLimit, String> {
    List<RiskLimit> findByScope(String scope);
    List<RiskLimit> findByStrategyId(String strategyId);
}