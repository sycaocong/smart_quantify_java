package com.smartquantify.strategy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 策略创建/更新请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyRequest {

    /**
     * 策略名称
     */
    private String name;

    /**
     * 策略描述
     */
    private String description;

    /**
     * 策略类型（如：MACD、RSI、布林带等）
     */
    private String type;

    /**
     * 策略语言（Java/Python）
     */
    private String language;

    /**
     * 交易所（BINANCE/OKX/BYBIT/HUOBI）
     */
    private String exchange;

    /**
     * 交易对列表
     */
    private List<String> symbols;

    /**
     * K线周期（1m/5m/15m/1h/4h/1d）
     */
    private String interval;

    /**
     * 策略参数
     */
    private Map<String, String> parameters;

    /**
     * 策略配置
     */
    private Map<String, String> config;
}