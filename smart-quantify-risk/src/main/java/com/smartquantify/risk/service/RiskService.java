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

/**
 * 风控服务
 * 负责风控规则的管理和实时风险检查
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskService {

    /**
     * 风控规则数据访问层
     */
    private final RiskRuleRepository riskRuleRepository;

    /**
     * 风险限制数据访问层
     */
    private final RiskLimitRepository riskLimitRepository;

    /**
     * 风险状态数据访问层
     */
    private final RiskStateRepository riskStateRepository;

    /**
     * 创建风控规则
     * 创建后自动清除规则缓存
     * @param request 风控规则请求
     * @return 创建的风控规则实体
     */
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

    /**
     * 获取风控规则详情
     * @param id 规则ID
     * @return 风控规则实体
     * @throws RuntimeException 规则不存在时抛出异常
     */
    public RiskRule getRule(String id) {
        return riskRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Risk rule not found: " + id));
    }

    /**
     * 获取风控规则列表
     * 结果会被缓存，提高查询性能
     * @return 风控规则列表
     */
    @Cacheable(value = "riskRules")
    public List<RiskRule> listRules() {
        return riskRuleRepository.findAll();
    }

    /**
     * 更新风控规则
     * 更新后自动清除规则缓存
     * @param id 规则ID
     * @param request 风控规则更新请求
     * @return 更新后的风控规则实体
     * @throws RuntimeException 规则不存在时抛出异常
     */
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

    /**
     * 删除风控规则
     * 删除后自动清除规则缓存
     * @param id 规则ID
     * @throws RuntimeException 规则不存在时抛出异常
     */
    @Transactional
    @CacheEvict(value = "riskRules", allEntries = true)
    public void deleteRule(String id) {
        RiskRule rule = riskRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Risk rule not found: " + id));
        riskRuleRepository.delete(rule);
        log.info("Risk rule deleted: id={}, name={}", id, rule.getName());
    }

    /**
     * 执行风险检查
     * 遍历所有风控规则，判断订单是否符合风险要求
     * @param request 风险检查请求
     * @return 风险检查结果，包含每条规则的检查结果
     */
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

    /**
     * 评估单条风控规则
     * 根据规则类型判断是否通过检查
     * @param rule 风控规则
     * @param request 风险检查请求
     * @return 是否通过检查
     */
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

    /**
     * 获取风险限制列表
     * 优先按策略ID查询，若无则按作用范围查询
     * @param scope 作用范围（如：global、user）
     * @param strategyId 策略ID（可选）
     * @param symbol 交易对（可选）
     * @param exchange 交易所（可选）
     * @return 风险限制列表
     */
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

    /**
     * 获取风险状态
     * 结果会被缓存，提高查询性能
     * 如果状态不存在，则返回默认状态
     * @param exchange 交易所
     * @param strategyId 策略ID
     * @return 风险状态
     */
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