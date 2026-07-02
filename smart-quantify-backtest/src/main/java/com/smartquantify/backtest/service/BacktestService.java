package com.smartquantify.backtest.service;

import com.smartquantify.backtest.dto.BacktestRequest;
import com.smartquantify.backtest.dto.BacktestResult;
import com.smartquantify.backtest.entity.BacktestTask;
import com.smartquantify.backtest.repository.BacktestTaskRepository;
import com.smartquantify.common.enums.BacktestStatus;
import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.model.Kline;
import com.smartquantify.adapter.AdapterFactory;
import com.smartquantify.adapter.ExchangeAdapter;
import com.smartquantify.common.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 回测服务
 * 负责回测任务的创建、执行和结果查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BacktestService {

    /**
     * 回测任务仓储
     */
    private final BacktestTaskRepository backtestTaskRepository;

    /**
     * 交易所适配器工厂
     */
    private final AdapterFactory adapterFactory;

    /**
     * 创建回测任务
     * @param request 回测请求参数
     * @return 创建的回测任务实体
     */
    @Transactional
    public BacktestTask createBacktest(BacktestRequest request) {
        String parameters = request.getParameters() != null ? JsonUtil.toJson(request.getParameters()) : "{}";

        BacktestTask task = BacktestTask.builder()
                .id(UUID.randomUUID().toString())
                .strategyId(request.getStrategyId())
                .strategyName(request.getStrategyName())
                .symbol(request.getSymbol())
                .interval(request.getInterval())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .initialCapital(request.getInitialCapital())
                .status(BacktestStatus.PENDING)
                .parameters(parameters)
                .createdAt(LocalDateTime.now())
                .build();

        task = backtestTaskRepository.save(task);
        log.info("Backtest task created: id={}, strategy={}, symbol={}",
                task.getId(), task.getStrategyName(), task.getSymbol());
        return task;
    }

    /**
     * 获取回测任务详情
     * @param id 回测任务ID
     * @return 回测任务实体
     * @throws RuntimeException 任务不存在时抛出异常
     */
    public BacktestTask getBacktest(String id) {
        return backtestTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Backtest task not found: " + id));
    }

    /**
     * 获取回测任务列表
     * @return 回测任务列表
     */
    public List<BacktestTask> listBacktests() {
        return backtestTaskRepository.findAll();
    }

    /**
     * 运行回测
     * 将任务状态变更为 RUNNING 并异步执行回测逻辑
     * @param id 回测任务ID
     * @return 更新后的回测任务实体
     * @throws RuntimeException 任务不存在时抛出异常
     */
    @Transactional
    public BacktestTask runBacktest(String id) {
        BacktestTask task = backtestTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Backtest task not found: " + id));

        if (task.getStatus() == BacktestStatus.RUNNING) {
            return task;
        }

        task.setStatus(BacktestStatus.RUNNING);
        task = backtestTaskRepository.save(task);
        log.info("Backtest started: id={}", task.getId());

        executeBacktestAsync(task.getId());

        return task;
    }

    /**
     * 异步执行回测
     * 使用 backtestExecutor 线程池执行回测逻辑
     * @param taskId 回测任务ID
     */
    @Async("backtestExecutor")
    public void executeBacktestAsync(String taskId) {
        log.info("Async backtest execution started: taskId={}", taskId);

        try {
            BacktestTask task = backtestTaskRepository.findById(taskId).orElse(null);
            if (task == null) {
                log.error("Backtest task not found during async execution: {}", taskId);
                return;
            }

            BacktestResult result = executeBacktest(task);

            task.setResult(JsonUtil.toJson(result));
            task.setStatus(BacktestStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
            backtestTaskRepository.save(task);

            log.info("Backtest completed: id={}, finalCapital={}, totalReturn={}%",
                    task.getId(), result.getFinalCapital(),
                    result.getTotalReturn().multiply(new BigDecimal("100")));

        } catch (Exception e) {
            BacktestTask task = backtestTaskRepository.findById(taskId).orElse(null);
            if (task != null) {
                task.setStatus(BacktestStatus.FAILED);
                task.setErrorMessage(e.getMessage());
                backtestTaskRepository.save(task);
            }
            log.error("Backtest failed: id={}, error={}", taskId, e.getMessage());
        }
    }

    /**
     * 执行回测核心逻辑
     * 使用K线数据模拟策略交易，计算收益指标
     * @param task 回测任务实体
     * @return 回测结果
     */
    private BacktestResult executeBacktest(BacktestTask task) {
        ExchangeAdapter adapter = adapterFactory.getAdapter(Exchange.BINANCE);
        List<Kline> klines = adapter.getKlines(task.getSymbol(), task.getInterval(), 100);

        BigDecimal capital = task.getInitialCapital();
        BigDecimal position = BigDecimal.ZERO;
        BigDecimal entryPrice = BigDecimal.ZERO;
        BigDecimal maxCapital = capital;
        BigDecimal minCapital = capital;

        List<BacktestResult.TradeRecord> trades = new ArrayList<>();
        int winningTrades = 0;
        int losingTrades = 0;
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;

        for (Kline kline : klines) {
            BigDecimal close = kline.getClose();
            BigDecimal high = kline.getHigh();
            BigDecimal low = kline.getLow();

            if (position.compareTo(BigDecimal.ZERO) == 0) {
                if (close.compareTo(high) > -1 && close.compareTo(low) < 1) {
                    BigDecimal quantity = capital.multiply(new BigDecimal("0.1")).divide(close, 6, RoundingMode.HALF_UP);
                    position = quantity;
                    entryPrice = close;
                    capital = capital.subtract(quantity.multiply(close));
                }
            } else {
                BigDecimal profit = position.multiply(close.subtract(entryPrice));
                if (profit.compareTo(BigDecimal.ZERO) > 0 && profit.compareTo(entryPrice.multiply(position).multiply(new BigDecimal("0.02"))) > 0) {
                    capital = capital.add(position.multiply(close));
                    trades.add(BacktestResult.TradeRecord.builder()
                            .time(kline.getCloseTime())
                            .side("SELL")
                            .price(close)
                            .quantity(position)
                            .profit(profit)
                            .balance(capital)
                            .build());
                    winningTrades++;
                    totalProfit = totalProfit.add(profit);
                    position = BigDecimal.ZERO;
                } else if (profit.compareTo(entryPrice.multiply(position).multiply(new BigDecimal("-0.01"))) < 0) {
                    capital = capital.add(position.multiply(close));
                    trades.add(BacktestResult.TradeRecord.builder()
                            .time(kline.getCloseTime())
                            .side("SELL")
                            .price(close)
                            .quantity(position)
                            .profit(profit)
                            .balance(capital)
                            .build());
                    losingTrades++;
                    totalLoss = totalLoss.add(profit.abs());
                    position = BigDecimal.ZERO;
                }
            }

            if (capital.compareTo(maxCapital) > 0) maxCapital = capital;
            if (capital.compareTo(minCapital) < 0) minCapital = capital;
        }

        if (position.compareTo(BigDecimal.ZERO) > 0) {
            capital = capital.add(position.multiply(klines.get(klines.size() - 1).getClose()));
        }

        int totalTrades = winningTrades + losingTrades;
        BigDecimal totalReturn = capital.subtract(task.getInitialCapital())
                .divide(task.getInitialCapital(), 4, RoundingMode.HALF_UP);

        Duration duration = Duration.between(task.getStartTime(), task.getEndTime());
        double years = duration.toDays() / 365.0;
        BigDecimal annualizedReturn = years > 0 ?
                BigDecimal.valueOf(Math.pow(1 + totalReturn.doubleValue(), 1 / years) - 1) : BigDecimal.ZERO;

        BigDecimal maxDrawdown = maxCapital.subtract(minCapital)
                .divide(maxCapital, 4, RoundingMode.HALF_UP);

        BigDecimal winRate = totalTrades > 0 ?
                BigDecimal.valueOf(winningTrades).divide(BigDecimal.valueOf(totalTrades), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal avgProfit = winningTrades > 0 ?
                totalProfit.divide(BigDecimal.valueOf(winningTrades), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal avgLoss = losingTrades > 0 ?
                totalLoss.divide(BigDecimal.valueOf(losingTrades), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal profitFactor = avgLoss.compareTo(BigDecimal.ZERO) > 0 ?
                avgProfit.divide(avgLoss, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal sharpeRatio = totalReturn.compareTo(BigDecimal.ZERO) > 0 ?
                totalReturn.divide(maxDrawdown.max(new BigDecimal("0.001")), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return BacktestResult.builder()
                .taskId(task.getId())
                .strategyId(task.getStrategyId())
                .symbol(task.getSymbol())
                .startTime(task.getStartTime())
                .endTime(task.getEndTime())
                .initialCapital(task.getInitialCapital())
                .finalCapital(capital)
                .totalReturn(totalReturn)
                .annualizedReturn(annualizedReturn)
                .maxDrawdown(maxDrawdown)
                .sharpeRatio(sharpeRatio)
                .winRate(winRate)
                .totalTrades(totalTrades)
                .winningTrades(winningTrades)
                .losingTrades(losingTrades)
                .avgProfit(avgProfit)
                .avgLoss(avgLoss)
                .profitFactor(profitFactor)
                .trades(trades)
                .build();
    }

    /**
     * 取消回测任务
     * 仅支持取消 RUNNING 状态的任务
     * @param id 回测任务ID
     * @return 更新后的回测任务实体
     * @throws RuntimeException 任务不存在时抛出异常
     */
    @Transactional
    public BacktestTask cancelBacktest(String id) {
        BacktestTask task = backtestTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Backtest task not found: " + id));

        if (task.getStatus() == BacktestStatus.RUNNING) {
            task.setStatus(BacktestStatus.CANCELLED);
            task.setCompletedAt(LocalDateTime.now());
            task = backtestTaskRepository.save(task);
            log.info("Backtest cancelled: id={}", task.getId());
        }

        return task;
    }

    /**
     * 获取回测结果
     * 仅当任务状态为 COMPLETED 时返回结果
     * @param id 回测任务ID
     * @return 回测结果
     * @throws RuntimeException 任务不存在或未完成时抛出异常
     */
    public BacktestResult getBacktestResult(String id) {
        BacktestTask task = backtestTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Backtest task not found: " + id));

        if (task.getStatus() != BacktestStatus.COMPLETED) {
            throw new RuntimeException("Backtest not completed yet: " + task.getStatus());
        }

        return JsonUtil.fromJson(task.getResult(), BacktestResult.class);
    }
}