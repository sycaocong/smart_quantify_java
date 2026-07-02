package com.smartquantify.adapter;

import com.smartquantify.common.enums.Exchange;
import com.smartquantify.common.model.*;

import java.util.List;
import java.util.function.Consumer;

/**
 * 交易所适配器接口
 * 设计文档: [DESIGN.md](../DESIGN.md#47-交易所适配器)
 * 定义与交易所交互的标准接口，实现不同交易所的统一访问
 */
public interface ExchangeAdapter {

    /**
     * 获取适配器名称
     * @return 适配器名称
     */
    String getName();

    /**
     * 获取交易所枚举
     * @return 交易所枚举值
     */
    Exchange getExchange();

    /**
     * 获取K线数据
     * @param symbol 交易对
     * @param interval K线周期
     * @param limit 返回条数
     * @return K线数据列表
     */
    List<Kline> getKlines(String symbol, String interval, int limit);

    /**
     * 获取订单簿数据
     * @param symbol 交易对
     * @param limit 返回深度
     * @return 订单簿数据
     */
    OrderBook getOrderBook(String symbol, int limit);

    /**
     * 获取Ticker数据
     * @param symbol 交易对
     * @return Ticker数据
     */
    Ticker getTicker(String symbol);

    /**
     * 获取合约列表
     * @return 合约列表
     */
    List<Instrument> getInstruments();

    /**
     * 下单
     * @param request 下单请求
     * @return 订单对象
     */
    com.smartquantify.common.model.Order placeOrder(OrderRequest request);

    /**
     * 取消订单
     * @param request 取消订单请求
     */
    void cancelOrder(CancelOrderRequest request);

    /**
     * 获取订单信息
     * @param symbol 交易对
     * @param orderId 订单ID
     * @return 订单对象
     */
    com.smartquantify.common.model.Order getOrder(String symbol, String orderId);

    /**
     * 获取当前挂单列表
     * @param symbol 交易对
     * @return 挂单列表
     */
    List<com.smartquantify.common.model.Order> getOpenOrders(String symbol);

    /**
     * 订阅K线实时数据
     * @param symbol 交易对
     * @param interval K线周期
     * @param handler 数据处理器
     */
    void subscribeKlines(String symbol, String interval, Consumer<Kline> handler);

    /**
     * 订阅订单簿实时数据
     * @param symbol 交易对
     * @param handler 数据处理器
     */
    void subscribeOrderBook(String symbol, Consumer<OrderBook> handler);

    /**
     * 取消订阅
     * @param symbol 交易对
     */
    void unsubscribe(String symbol);
}