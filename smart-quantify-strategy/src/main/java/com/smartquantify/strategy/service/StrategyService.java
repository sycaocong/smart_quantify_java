package com.smartquantify.strategy.service;

import com.smartquantify.strategy.dto.StrategyRequest;
import com.smartquantify.strategy.dto.StrategyResponse;
import com.smartquantify.strategy.entity.Strategy;
import com.smartquantify.strategy.repository.StrategyRepository;
import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.enums.StrategyStatus;
import com.smartquantify.common.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 策略服务
 * 负责策略的全生命周期管理，包括创建、启动、停止、暂停、更新和删除操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyService {

    /**
     * 策略数据访问层
     */
    private final StrategyRepository strategyRepository;

    /**
     * 创建策略
     * @param request 策略创建请求，包含策略名称、类型、语言、交易所、参数等信息
     * @return 策略响应对象
     */
    @Transactional
    public StrategyResponse createStrategy(StrategyRequest request) {
        String parameters = request.getParameters() != null ? JsonUtil.toJson(request.getParameters()) : "{}";
        String config = request.getConfig() != null ? JsonUtil.toJson(request.getConfig()) : "{}";
        String symbols = request.getSymbols() != null ? JsonUtil.toJson(request.getSymbols()) : "[]";

        Strategy strategy = Strategy.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .language(request.getLanguage())
                .status(StrategyStatus.STOPPED)
                .parameters(parameters)
                .config(config)
                .exchange(Exchange.valueOf(request.getExchange()))
                .symbols(symbols)
                .interval(request.getInterval())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version("1.0.0")
                .build();

        strategy = strategyRepository.save(strategy);
        log.info("Strategy created: id={}, name={}", strategy.getId(), strategy.getName());
        return toResponse(strategy);
    }

    /**
     * 获取策略详情
     * @param id 策略ID
     * @return 策略响应对象
     * @throws RuntimeException 策略不存在时抛出异常
     */
    public StrategyResponse getStrategy(String id) {
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + id));
        return toResponse(strategy);
    }

    /**
     * 获取策略列表
     * @return 策略响应对象列表
     */
    public List<StrategyResponse> listStrategies() {
        List<Strategy> strategies = strategyRepository.findAll();
        List<StrategyResponse> responses = new ArrayList<>();
        for (Strategy strategy : strategies) {
            responses.add(toResponse(strategy));
        }
        return responses;
    }

    /**
     * 启动策略
     * 将策略状态从 STOPPED 或 PAUSED 变更为 RUNNING
     * @param id 策略ID
     * @return 策略响应对象
     * @throws RuntimeException 策略不存在时抛出异常
     */
    @Transactional
    public StrategyResponse startStrategy(String id) {
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + id));
        if (strategy.getStatus() == StrategyStatus.RUNNING) {
            return toResponse(strategy);
        }
        strategy.setStatus(StrategyStatus.RUNNING);
        strategy.setUpdatedAt(LocalDateTime.now());
        strategy = strategyRepository.save(strategy);
        log.info("Strategy started: id={}, name={}", strategy.getId(), strategy.getName());
        return toResponse(strategy);
    }

    /**
     * 停止策略
     * 将策略状态变更为 STOPPED，并记录最后运行时间
     * @param id 策略ID
     * @return 策略响应对象
     * @throws RuntimeException 策略不存在时抛出异常
     */
    @Transactional
    public StrategyResponse stopStrategy(String id) {
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + id));
        strategy.setStatus(StrategyStatus.STOPPED);
        strategy.setUpdatedAt(LocalDateTime.now());
        strategy.setLastRunTime(LocalDateTime.now());
        strategy = strategyRepository.save(strategy);
        log.info("Strategy stopped: id={}, name={}", strategy.getId(), strategy.getName());
        return toResponse(strategy);
    }

    /**
     * 暂停策略
     * 将策略状态变更为 PAUSED，暂停后可重新启动
     * @param id 策略ID
     * @return 策略响应对象
     * @throws RuntimeException 策略不存在时抛出异常
     */
    @Transactional
    public StrategyResponse pauseStrategy(String id) {
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + id));
        strategy.setStatus(StrategyStatus.PAUSED);
        strategy.setUpdatedAt(LocalDateTime.now());
        strategy = strategyRepository.save(strategy);
        log.info("Strategy paused: id={}, name={}", strategy.getId(), strategy.getName());
        return toResponse(strategy);
    }

    /**
     * 更新策略
     * 更新策略的名称、描述、参数和配置信息
     * @param id 策略ID
     * @param request 策略更新请求
     * @return 策略响应对象
     * @throws RuntimeException 策略不存在时抛出异常
     */
    @Transactional
    public StrategyResponse updateStrategy(String id, StrategyRequest request) {
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + id));

        if (request.getName() != null) {
            strategy.setName(request.getName());
        }
        if (request.getDescription() != null) {
            strategy.setDescription(request.getDescription());
        }
        if (request.getParameters() != null) {
            strategy.setParameters(JsonUtil.toJson(request.getParameters()));
        }
        if (request.getConfig() != null) {
            strategy.setConfig(JsonUtil.toJson(request.getConfig()));
        }

        strategy.setUpdatedAt(LocalDateTime.now());
        strategy = strategyRepository.save(strategy);
        log.info("Strategy updated: id={}", strategy.getId());
        return toResponse(strategy);
    }

    /**
     * 删除策略
     * 如果策略正在运行，先停止策略再删除
     * @param id 策略ID
     * @throws RuntimeException 策略不存在时抛出异常
     */
    @Transactional
    public void deleteStrategy(String id) {
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + id));

        if (strategy.getStatus() == StrategyStatus.RUNNING) {
            strategy.setStatus(StrategyStatus.STOPPED);
            strategyRepository.save(strategy);
        }

        strategyRepository.delete(strategy);
        log.info("Strategy deleted: id={}, name={}", id, strategy.getName());
    }

    /**
     * 将策略实体转换为响应对象
     * @param strategy 策略实体
     * @return 策略响应对象
     */
    private StrategyResponse toResponse(Strategy strategy) {
        return StrategyResponse.builder()
                .id(strategy.getId())
                .name(strategy.getName())
                .description(strategy.getDescription())
                .type(strategy.getType())
                .language(strategy.getLanguage())
                .status(strategy.getStatus().name())
                .exchange(strategy.getExchange().name())
                .symbols(strategy.getSymbols())
                .interval(strategy.getInterval())
                .createdAt(strategy.getCreatedAt())
                .updatedAt(strategy.getUpdatedAt())
                .build();
    }
}