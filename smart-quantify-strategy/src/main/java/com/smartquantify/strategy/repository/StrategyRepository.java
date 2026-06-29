package com.smartquantify.strategy.repository;

import com.smartquantify.strategy.entity.Strategy;
import com.smartquantify.common.enums.StrategyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StrategyRepository extends JpaRepository<Strategy, String> {
    List<Strategy> findByStatus(StrategyStatus status);
    List<Strategy> findByType(String type);
    List<Strategy> findByExchange(String exchange);
}