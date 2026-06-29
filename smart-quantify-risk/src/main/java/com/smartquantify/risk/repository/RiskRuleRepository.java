package com.smartquantify.risk.repository;

import com.smartquantify.risk.entity.RiskRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskRuleRepository extends JpaRepository<RiskRule, String> {
    List<RiskRule> findByEnabledTrue();
    List<RiskRule> findByScope(String scope);
}