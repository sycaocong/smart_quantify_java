package com.smartquantify.backtest.repository;

import com.smartquantify.backtest.entity.BacktestTask;
import com.smartquantify.common.enums.BacktestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BacktestTaskRepository extends JpaRepository<BacktestTask, String> {
    List<BacktestTask> findByStatus(BacktestStatus status);
    List<BacktestTask> findByStrategyId(String strategyId);
}