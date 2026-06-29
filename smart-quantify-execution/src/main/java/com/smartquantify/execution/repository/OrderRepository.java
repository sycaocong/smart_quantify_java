package com.smartquantify.execution.repository;

import com.smartquantify.execution.entity.Order;
import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByStatus(OrderStatus status);
    List<Order> findBySymbol(String symbol);
    List<Order> findByExchange(Exchange exchange);
    List<Order> findByStrategyId(String strategyId);
    List<Order> findByStatusAndSymbol(OrderStatus status, String symbol);
}