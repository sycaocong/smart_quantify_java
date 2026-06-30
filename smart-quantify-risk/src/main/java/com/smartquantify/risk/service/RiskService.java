package com.smartquantify.risk.service;

import com.smartquantify.risk.dto.RiskCheckRequest;
import com.smartquantify.risk.dto.RiskCheckResponse;
import com.smartquantify.risk.dto.RiskRuleRequest;
import com.smartquantify.risk.entity.RiskLimit;
import com.smartquantify.risk.entity.RiskRule;
import com.smartquantify.risk.entity.RiskState;
import com.smartquantify.risk.repository.RiskLimitRepository;
import com.smartquantify.risk.repository.RiskRuleRepository;
import com.smartquantify.risk.repository.RiskStateRepository;
import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.RiskRuleType;
import com.smartquantify.common.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskService {
    private final RiskRuleRepository riskRuleRepository;
    private final RiskLimitRepository riskLimitRepository;
    private final RiskStateRepository riskStateRepository;

    @Transactional
    @CacheEvict(value = "riskRules", allEntries = true)
    public RiskRule createRule(RiskRuleRequest request) {
        String conditions = request.getConditions() != null ? JsonUtil.toJson(request.getConditions()) : "{}";
        String actions = request.getActions() != null ? JsonUtil.toJson(request.getActions()) : "[]";
        String strategyIds = request.getStrategyIds() != null ? JsonUtil.toJson(request.getStrategyIds()) : "[]";
        String symbols = request.getSymbols() != null ? JsonUtil.toJson(request.getSymbols()) : "[]";
        String exchanges = request.getExchanges() != null ? JsonUtil.toJson(request.getExchanges()) : "[]";

        RiskRule rule = RiskRule.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .type(RiskRuleType.valueOf(request.getType()))
                .enabled(request.getEnabled())
                .priority(request.getPriority())
                .conditions(conditions)
                .actions(actions)
                .scope(request.getScope())
                .strategyIds(strategyIds)
                .symbols(symbols)
                .exchanges(exchanges)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        rule = riskRuleRepository.save(rule);
        log.info("Risk rule created: id={}, name={}", rule.getId(), rule.getName());
        return rule;
    }

    public RiskRule getRule(String id) {
        return riskRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Risk rule not found: " + id));
    }

    @Cacheable(value = "riskRules")
    public List<RiskRule> listRules() {
        return riskRuleRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = "riskRules", allEntries = true)
    public RiskRule updateRule(String id, RiskRuleRequest request) {
        RiskRule rule = riskRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Risk rule not found: " + id));

        if (request.getName() != null) {
            rule.setName(request.getName());
        }
        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
        }
        if (request.getEnabled() != null) {
            rule.setEnabled(request.getEnabled());
        }
        if (request.getPriority() != null) {
            rule.setPriority(request.getPriority());
        }
        if (request.getConditions() != null) {
            rule.setConditions(JsonUtil.toJson(request.getConditions()));
        }
        if (request.getActions() != null) {
            rule.setActions(JsonUtil.toJson(request.getActions()));
        }

        rule.setUpdatedAt(LocalDateTime.now());
        rule = riskRuleRepository.save(rule);
        log.info("Risk rule updated: id={}", rule.getId());
        return rule;
    }

    @Transactional
    @CacheEvict(value = "riskRules", allEntries = true)
    public void deleteRule(String id) {
        RiskRule rule = riskRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Risk rule not found: " + id));
        riskRuleRepository.delete(rule);
        log.info("Risk rule deleted: id={}, name={}", id, rule.getName());
    }

    public RiskCheckResponse checkRisk(RiskCheckRequest request) {
        List<RiskRule> rules = listRules();
        List<RiskCheckResponse.RuleCheckResult> results = new ArrayList<>();
        boolean allPassed = true;

        for (RiskRule rule : rules) {
            boolean passed = evaluateRule(rule, request);
            if (!passed) {
                allPassed = false;
            }
            results.add(RiskCheckResponse.RuleCheckResult.builder()
                    .passed(passed)
                    .ruleId(rule.getId())
                    .ruleName(rule.getName())
                    .action(passed ? "ALLOW" : "REJECT")
                    .build());
        }

        log.info("Risk check result: strategyId={}, passed={}", request.getStrategyId(), allPassed);
        return RiskCheckResponse.builder()
                .passed(allPassed)
                .results(results)
                .build();
    }

    private boolean evaluateRule(RiskRule rule, RiskCheckRequest request) {
        try {
            BigDecimal position = BigDecimal.ZERO;
            RiskState state = getState(request.getExchange(), request.getStrategyId());
            if (state != null) {
                position = state.getCurrentPosition() != null ? state.getCurrentPosition() : BigDecimal.ZERO;
            }

            BigDecimal tradeValue = request.getQuantity().multiply(request.getPrice());

            switch (rule.getType()) {
                case POSITION_LIMIT:
                    return position.add(tradeValue).compareTo(new BigDecimal("10000")) <= 0;
                case MAX_TRADE_SIZE:
                    return tradeValue.compareTo(new BigDecimal("5000")) <= 0;
                case DRAWDOWN_LIMIT:
                    return state == null || state.getCurrentDrawdown() == null ||
                            state.getCurrentDrawdown().compareTo(new BigDecimal("0.2")) <= 0;
                default:
                    return true;
            }
        } catch (Exception e) {
            log.warn("Error evaluating rule: {}", e.getMessage());
            return true;
        }
    }

    public List<RiskLimit> getLimits(String scope, String strategyId, String symbol, String exchange) {
        List<RiskLimit> limits = new ArrayList<>();
        if (strategyId != null) {
            limits.addAll(riskLimitRepository.findByStrategyId(strategyId));
        }
        if (limits.isEmpty() && scope != null) {
            limits.addAll(riskLimitRepository.findByScope(scope));
        }
        return limits;
    }

    @Cacheable(value = "riskState", key = "#exchange + ':' + #strategyId")
    public RiskState getState(String exchange, String strategyId) {
        return riskStateRepository.findByExchangeAndStrategyId(Exchange.valueOf(exchange), strategyId)
                .orElse(RiskState.builder()
                        .id(UUID.randomUUID().toString())
                        .exchange(Exchange.valueOf(exchange))
                        .strategyId(strategyId)
                        .currentPosition(BigDecimal.ZERO)
                        .currentDrawdown(BigDecimal.ZERO)
                        .ordersInLastMinute(0)
                        .dailyVolume(BigDecimal.ZERO)
                        .lastCheckTime(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
    }
}