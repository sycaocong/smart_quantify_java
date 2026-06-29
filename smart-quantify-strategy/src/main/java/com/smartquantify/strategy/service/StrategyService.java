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

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyService {
    private final StrategyRepository strategyRepository;

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

    public StrategyResponse getStrategy(String id) {
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + id));
        return toResponse(strategy);
    }

    public List<StrategyResponse> listStrategies() {
        List<Strategy> strategies = strategyRepository.findAll();
        List<StrategyResponse> responses = new ArrayList<>();
        for (Strategy strategy : strategies) {
            responses.add(toResponse(strategy));
        }
        return responses;
    }

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