package com.smartquantify.backtest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 回测结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestResult {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 策略ID
     */
    private String strategyId;

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 回测开始时间
     */
    private LocalDateTime startTime;

    /**
     * 回测结束时间
     */
    private LocalDateTime endTime;

    /**
     * 初始资金
     */
    private BigDecimal initialCapital;

    /**
     * 最终资金
     */
    private BigDecimal finalCapital;

    /**
     * 总收益率
     */
    private BigDecimal totalReturn;

    /**
     * 年化收益率
     */
    private BigDecimal annualizedReturn;

    /**
     * 最大回撤率
     */
    private BigDecimal maxDrawdown;

    /**
     * 夏普比率
     */
    private BigDecimal sharpeRatio;

    /**
     * 胜率
     */
    private BigDecimal winRate;

    /**
     * 总交易次数
     */
    private Integer totalTrades;

    /**
     * 盈利交易次数
     */
    private Integer winningTrades;

    /**
     * 亏损交易次数
     */
    private Integer losingTrades;

    /**
     * 平均盈利
     */
    private BigDecimal avgProfit;

    /**
     * 平均亏损
     */
    private BigDecimal avgLoss;

    /**
     * 盈亏比
     */
    private BigDecimal profitFactor;

    /**
     * 交易记录列表
     */
    private List<TradeRecord> trades;

    /**
     * 交易记录内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeRecord {

        /**
         * 交易时间
         */
        private LocalDateTime time;

        /**
         * 交易方向（BUY/SELL）
         */
        private String side;

        /**
         * 交易价格
         */
        private BigDecimal price;

        /**
         * 交易数量
         */
        private BigDecimal quantity;

        /**
         * 盈利金额
         */
        private BigDecimal profit;

        /**
         * 账户余额
         */
        private BigDecimal balance;
    }
}