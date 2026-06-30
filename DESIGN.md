# Smart Quantify Java - 量化交易系统设计文档

## 1. 项目概述

Smart Quantify Java 是一套基于 Java Spring Boot + Spring Cloud 的现代化量化交易系统，参考 Go 版本 `smart_quantify` 的架构设计，支持主流交易所（Binance、OKX、Bybit、Huobi）的现货、期货和期权交易。系统采用微服务架构，支持 Kubernetes 部署，并提供完整的管理界面。

## 2. 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 3.2.x |
| 微服务 | Spring Cloud | 2023.0.x |
| 注册中心 | Nacos | 2.3.x |
| 配置中心 | Nacos | 2.3.x |
| 服务网关 | Spring Cloud Gateway | 4.1.x |
| RPC | gRPC | 1.61.x |
| 消息队列 | Apache Kafka | 3.6.x |
| 时序数据库 | ClickHouse | 24.x |
| 缓存 | Redis | 7.2.x |
| ORM | Spring Data JPA | 3.2.x |
| 数据库 | MySQL | 8.0.x |
| 日志 | Logback | 1.4.x |
| 监控 | Prometheus + Grafana | - |
| 测试 | JUnit 5 + Mockito | - |

## 3. 系统架构

### 3.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端管理界面 (Web)                        │
│                     React 18 + TypeScript                       │
└───────────────────────┬─────────────────────────────────────────┘
                        │ REST API
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│              Spring Cloud Gateway (API Gateway)                  │
│                      端口: 8080                                 │
└───────────────────────┬─────────────────────────────────────────┘
                        │ HTTP/gRPC
        ┌───────────────┼───────────────┬───────────────┐
        ▼               ▼               ▼               ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ Strategy    │  │    Risk     │  │ Execution   │  │   Market    │
│   Service   │  │   Service   │  │   Service   │  │   Service   │
│  端口:8081  │  │  端口:8082  │  │  端口:8083  │  │  端口:8084  │
└──────┬──────┘  └─────────────┘  └──────┬──────┘  └──────┬──────┘
       │                                  │               │
       │ gRPC                             │ gRPC          │ gRPC
       ▼                                  ▼               ▼
┌─────────────┐                    ┌─────────────┐  ┌─────────────┐
│  Python     │                    │  Exchange   │  │  Exchange   │
│   Bridge    │                    │   Adapter   │  │   Adapter   │
│ (gRPC)      │                    └──────┬──────┘  └──────┬──────┘
└─────────────┘                           │               │
                                          │               │
                                          └───────┬───────┘
                                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                        交易所 (Binance/OKX/Bybit/Huobi)         │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 数据流程

```
市场数据: 交易所 WebSocket → Kafka → ClickHouse
交易信号: 策略引擎 → Kafka → 风控引擎 → 订单执行服务 → 交易所
```

### 3.3 模块划分

| 模块 | 职责 | 技术实现 |
|------|------|----------|
| `smart-quantify-common` | 公共模块，包含通用类型、枚举、工具类 | Spring Boot Starter |
| `smart-quantify-api` | API 定义模块，包含 DTO、gRPC 定义 | - |
| `smart-quantify-gateway` | API 网关，路由转发、认证授权 | Spring Cloud Gateway |
| `smart-quantify-strategy` | 策略引擎，策略管理、信号生成 | Spring Boot + gRPC |
| `smart-quantify-risk` | 风控引擎，规则引擎、风险检查 | Spring Boot |
| `smart-quantify-backtest` | 回测服务，策略回测、绩效分析 | Spring Boot |
| `smart-quantify-execution` | 订单执行服务，订单提交、执行 | Spring Boot |
| `smart-quantify-market` | 市场服务，行情数据收集、推送 | Spring Boot |
| `smart-quantify-adapter` | 交易所适配器，统一接口封装 | Spring Boot |
| `smart-quantify-python` | Python 策略支持模块 | gRPC Sidecar |

## 4. 核心服务设计

### 4.1 API Gateway

**模块**: `smart-quantify-gateway`

API 网关使用 Spring Cloud Gateway 实现，负责路由转发、认证授权、限流熔断。

**路由配置**:

| 模块 | 前缀 | 目标服务 |
|------|------|----------|
| 市场数据 | `/api/v1/market/**` | market-service |
| 策略管理 | `/api/v1/strategies/**` | strategy-service |
| 风控管理 | `/api/v1/risk/**` | risk-service |
| 订单执行 | `/api/v1/orders/**` | execution-service |

**核心配置**:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: market-service
          uri: lb://market-service
          predicates:
            - Path=/api/v1/market/**
        - id: strategy-service
          uri: lb://strategy-service
          predicates:
            - Path=/api/v1/strategies/**
        - id: risk-service
          uri: lb://risk-service
          predicates:
            - Path=/api/v1/risk/**
        - id: execution-service
          uri: lb://execution-service
          predicates:
            - Path=/api/v1/orders/**
```

### 4.2 策略引擎

**模块**: `smart-quantify-strategy`

策略引擎负责策略的注册、生命周期管理和信号生成。

**策略状态枚举**:

```java
public enum StrategyStatus {
    RUNNING,
    STOPPED,
    PAUSED
}
```

**实现代码**: [StrategyStatus.java](smart-quantify-common/src/main/java/com/smartquantify/common/enums/StrategyStatus.java)

**策略实体**:

```java
@Entity
@Table(name = "strategy")
public class Strategy {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private String language;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StrategyStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String parameters;
    
    @Column(columnDefinition = "TEXT")
    private String config;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Exchange exchange;
    
    @Column(columnDefinition = "TEXT")
    private String symbols;
    
    @Column(nullable = false)
    private String interval;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime lastRunTime;
    
    @Column(columnDefinition = "TEXT")
    private String statistics;
    
    @Column(nullable = false)
    private String version;
}
```

**实现代码**: [Strategy.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/entity/Strategy.java)

**核心服务接口**:

| 方法 | 功能 |
|------|------|
| `createStrategy(StrategyRequest)` | 创建策略 |
| `getStrategy(String id)` | 获取策略详情 |
| `listStrategies()` | 列出所有策略 |
| `startStrategy(String id)` | 启动策略 |
| `stopStrategy(String id)` | 停止策略 |
| `pauseStrategy(String id)` | 暂停策略 |
| `updateStrategy(String id, StrategyRequest)` | 更新策略 |
| `deleteStrategy(String id)` | 删除策略 |

**实现代码**: [StrategyService.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/service/StrategyService.java), [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java)

**Python 策略支持**:

使用 gRPC Sidecar 模式实现 Python 策略执行，策略服务通过 gRPC 调用独立的 Python 进程，避免 Jython 兼容性问题。Python 进程维护策略运行环境，支持 Python 3.x 及主流数据分析库（pandas、numpy、ta-lib）。

```java
public class PythonStrategyExecutor {
    private PythonBridgeGrpc.PythonBridgeBlockingStub pythonStub;
    
    public Signal execute(String strategyId, Kline kline) {
        ExecuteRequest request = ExecuteRequest.newBuilder()
                .setStrategyId(strategyId)
                .setKline(convertToProto(kline))
                .build();
        ExecuteResponse response = pythonStub.execute(request);
        return convertToSignal(response.getSignal());
    }
}
```

### 4.3 风控引擎

**模块**: `smart-quantify-risk`

风控引擎实现规则引擎和风险检查功能，采用轻量级自定义规则引擎实现，支持动态规则加载和优先级评估。

**风控规则类型**:

```java
public enum RiskRuleType {
    POSITION_LIMIT,
    DRAWDOWN_LIMIT,
    RATE_LIMIT,
    MAX_TRADE_SIZE,
    VOLUME_LIMIT
}
```

**实现代码**: [RiskRuleType.java](smart-quantify-common/src/main/java/com/smartquantify/common/enums/RiskRuleType.java)

**风控规则实体**:

```java
@Entity
@Table(name = "risk_rule")
public class RiskRule {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskRuleType type;
    
    @Column(nullable = false)
    private Boolean enabled;
    
    @Column(nullable = false)
    private Integer priority;
    
    @Column(columnDefinition = "TEXT")
    private String conditions;
    
    @Column(columnDefinition = "TEXT")
    private String actions;
    
    @Column(nullable = false)
    private String scope;
    
    @Column(columnDefinition = "TEXT")
    private String strategyIds;
    
    @Column(columnDefinition = "TEXT")
    private String symbols;
    
    @Column(columnDefinition = "TEXT")
    private String exchanges;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

**实现代码**: [RiskRule.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/entity/RiskRule.java)

**核心服务接口**:

| 方法 | 功能 |
|------|------|
| `createRule(RiskRule)` | 创建风控规则 |
| `getRule(String id)` | 获取规则详情 |
| `listRules()` | 列出所有规则 |
| `updateRule(String id, RiskRule)` | 更新规则 |
| `deleteRule(String id)` | 删除规则 |
| `checkRisk(Signal)` | 检查信号风险 |
| `getLimits(String scope, String strategyId, String symbol, String exchange)` | 获取风险限额 |
| `getState(String exchange, String strategyId)` | 获取风险状态 |

**实现代码**: [RiskService.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/service/RiskService.java), [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java)

**缓存策略**:

使用 Spring Cache + Caffeine 实现风控规则和状态的缓存，提高风险检查性能。

**缓存配置**:

| 方法 | 缓存名称 | 缓存键 | 过期时间 |
|------|----------|--------|----------|
| `listRules` | riskRules | - | 10s |
| `getState` | riskState | exchange:strategyId | 10s |

**缓存更新**:
- 创建/更新/删除规则时自动清除 `riskRules` 缓存（`@CacheEvict`）

**风险检查流程**:

```
1. 获取所有启用的规则
2. 根据 scope/symbol/exchange 过滤规则
3. 按优先级排序规则
4. 使用自定义规则引擎评估每条规则（条件匹配 → 动作执行）
5. 返回检查结果和是否通过
```

### 4.4 订单执行服务

**模块**: `smart-quantify-execution`

订单执行服务负责订单的提交、管理和执行。

**订单状态枚举**:

```java
public enum OrderStatus {
    NEW,
    PARTIALLY_FILLED,
    FILLED,
    CANCELED,
    REJECTED
}
```

**实现代码**: [OrderStatus.java](smart-quantify-common/src/main/java/com/smartquantify/common/enums/OrderStatus.java)

**订单实体**:

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String symbol;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Side side;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;
    
    @Column(nullable = false)
    private BigDecimal quantity;
    
    @Column
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false)
    private BigDecimal filledQuantity;
    
    @Column(nullable = false)
    private BigDecimal remainingQuantity;
    
    @Column
    private BigDecimal avgPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Exchange exchange;
    
    @Column
    private String clientOrderId;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column
    private String strategyId;
}
```

**实现代码**: [Order.java](smart-quantify-execution/src/main/java/com/smartquantify/execution/entity/Order.java)

**核心服务接口**:

| 方法 | 功能 |
|------|------|
| `submitOrder(OrderRequest)` | 提交订单 |
| `getOrder(String id)` | 获取订单 |
| `listOrders(String status, String symbol, String exchange)` | 列出订单 |
| `cancelOrder(String id)` | 取消订单 |
| `syncOrders()` | 同步交易所订单 |

**实现代码**: [ExecutionService.java](smart-quantify-execution/src/main/java/com/smartquantify/execution/service/ExecutionService.java), [ExecutionController.java](smart-quantify-execution/src/main/java/com/smartquantify/execution/controller/ExecutionController.java)

**异步订单执行**:

采用 Outbox 模式实现异步订单提交，通过 Kafka 解耦订单创建和执行，提高系统吞吐量和可用性。

**订单执行流程**:
```
1. 用户提交订单请求
2. ExecutionService 保存订单到数据库（状态为 NEW）
3. 发布 OrderCreatedEvent 到 Kafka 主题 `order-created`
4. OrderExecutionConsumer 消费消息，调用交易所适配器执行订单
5. 更新订单状态，发布 OrderExecutedEvent 到 Kafka 主题 `order-executed`
6. 如果订单完全成交，创建 Trade 记录
```

**实现代码**: [OrderExecutionConsumer.java](smart-quantify-execution/src/main/java/com/smartquantify/execution/consumer/OrderExecutionConsumer.java)

### 4.5 回测服务

**模块**: `smart-quantify-backtest`

回测服务负责策略回测、绩效分析和报告生成。

**回测状态枚举**:

```java
public enum BacktestStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}
```

**实现代码**: [BacktestStatus.java](smart-quantify-common/src/main/java/com/smartquantify/common/enums/BacktestStatus.java)

**回测任务实体**:

```java
@Entity
@Table(name = "backtest_task")
public class BacktestTask {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String strategyId;
    
    @Column(nullable = false)
    private String strategyName;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column(nullable = false)
    private String interval;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    @Column(nullable = false)
    private BigDecimal initialCapital;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BacktestStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String parameters;
    
    @Column(columnDefinition = "TEXT")
    private String result;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime completedAt;
    
    @Column
    private String errorMessage;
}
```

**实现代码**: [BacktestTask.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/entity/BacktestTask.java)

**回测结果实体**:

```java
public class BacktestResult {
    private String taskId;
    private String strategyId;
    private String symbol;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal initialCapital;
    private BigDecimal finalCapital;
    private BigDecimal totalReturn;
    private BigDecimal annualizedReturn;
    private BigDecimal maxDrawdown;
    private BigDecimal sharpeRatio;
    private BigDecimal winRate;
    private Integer totalTrades;
    private Integer winningTrades;
    private Integer losingTrades;
    private BigDecimal avgProfit;
    private BigDecimal avgLoss;
    private BigDecimal profitFactor;
    private List<TradeRecord> trades;
}
```

**实现代码**: [BacktestResult.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/dto/BacktestResult.java)

**核心服务接口**:

| 方法 | 功能 |
|------|------|
| `createBacktest(BacktestRequest)` | 创建回测任务 |
| `getBacktest(String id)` | 获取回测任务详情 |
| `listBacktests()` | 列出回测任务 |
| `runBacktest(String id)` | 运行回测 |
| `cancelBacktest(String id)` | 取消回测 |
| `getBacktestResult(String id)` | 获取回测结果 |
| `generateReport(String id)` | 生成回测报告 |

**实现代码**: [BacktestService.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/service/BacktestService.java), [BacktestController.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/controller/BacktestController.java)

**异步回测执行**:

使用 `@Async` 注解配合独立线程池实现异步回测，避免阻塞请求线程。

**线程池配置**:
- 线程池名称: `backtestExecutor`
- 核心线程数: 4
- 最大线程数: 8
- 队列容量: 100
- 线程前缀: `backtest-`

**实现代码**: [BacktestAsyncConfig.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/config/BacktestAsyncConfig.java)

**回测流程**:

```
1. 用户提交回测请求（策略ID、交易对、时间范围、初始资金）
2. 回测服务从 ClickHouse 加载历史K线数据
3. 调用策略引擎执行策略（使用历史数据模拟）
4. 记录每笔交易和资金变化
5. 计算绩效指标（收益率、最大回撤、夏普比率等）
6. 保存回测结果并生成报告
```

### 4.6 市场服务

**模块**: `smart-quantify-market`

市场服务负责收集市场数据并发布到 Kafka。

**K线实体**:

```java
public class Kline {
    private String symbol;
    private String interval;
    private LocalDateTime openTime;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal volume;
    private BigDecimal quoteVolume;
    private LocalDateTime closeTime;
}
```

**实现代码**: [Kline.java](smart-quantify-common/src/main/java/com/smartquantify/common/model/Kline.java)

**核心服务接口**:

| 方法 | 功能 |
|------|------|
| `getKlines(String symbol, String interval, Integer limit)` | 获取K线数据 |
| `getOrderBook(String symbol, Integer limit)` | 获取订单簿 |
| `getTicker(String symbol)` | 获取Ticker |
| `getInstruments(String exchange, String type)` | 获取合约列表 |
| `subscribeKlines(String symbol, String interval)` | 订阅K线 |
| `subscribeOrderBook(String symbol)` | 订阅订单簿 |

**实现代码**: [MarketService.java](smart-quantify-market/src/main/java/com/smartquantify/market/service/MarketService.java), [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java)

**缓存策略**:

使用 Spring Cache + Caffeine 实现市场数据缓存，减少对交易所 API 的调用频率。

**缓存配置**:

| 方法 | 缓存名称 | 缓存键 | 过期时间 |
|------|----------|--------|----------|
| `getKlines` | klines | exchange:symbol:interval:limit | 10s |
| `getOrderBook` | orderBook | exchange:symbol:limit | 10s |
| `getTicker` | ticker | exchange:symbol | 10s |
| `getInstruments` | instruments | exchange:type | 10s |

**实现代码**: [CacheConfig.java](smart-quantify-common/src/main/java/com/smartquantify/common/config/CacheConfig.java)

### 4.7 交易所适配器

**模块**: `smart-quantify-adapter`

交易所适配器提供统一的接口封装，支持多个交易所。

**适配器接口**:

```java
public interface ExchangeAdapter {
    String getName();
    
    Exchange getExchange();
    
    List<Kline> getKlines(String symbol, String interval, int limit);
    
    OrderBook getOrderBook(String symbol, int limit);
    
    Ticker getTicker(String symbol);
    
    List<Instrument> getInstruments();
    
    Order placeOrder(OrderRequest request);
    
    void cancelOrder(CancelOrderRequest request);
    
    Order getOrder(String symbol, String orderId);
    
    List<Order> getOpenOrders(String symbol);
    
    void subscribeKlines(String symbol, String interval, Consumer<Kline> handler);
    
    void subscribeOrderBook(String symbol, Consumer<OrderBook> handler);
    
    void unsubscribe(String symbol);
}
```

**实现代码**: [ExchangeAdapter.java](smart-quantify-adapter/src/main/java/com/smartquantify/adapter/ExchangeAdapter.java)

**支持的交易所**:

| 交易所 | 类名 | 状态 |
|--------|------|------|
| Binance | `BinanceAdapter` | 支持 |
| OKX | `OkxAdapter` | 支持 |
| Bybit | `BybitAdapter` | 支持 |
| Huobi | `HuobiAdapter` | 支持 |

**实现代码**: [BinanceAdapter.java](smart-quantify-adapter/src/main/java/com/smartquantify/adapter/binance/BinanceAdapter.java), [OkxAdapter.java](smart-quantify-adapter/src/main/java/com/smartquantify/adapter/okx/OkxAdapter.java)

## 5. 数据模型

### 5.1 通用枚举

**交易方向**:

```java
public enum Side {
    BUY,
    SELL
}
```

**实现代码**: [Side.java](smart-quantify-common/src/main/java/com/smartquantify/common/enums/Side.java)

**订单类型**:

```java
public enum OrderType {
    MARKET,
    LIMIT,
    STOP,
    STOP_LIMIT
}
```

**实现代码**: [OrderType.java](smart-quantify-common/src/main/java/com/smartquantify/common/enums/OrderType.java)

**交易所**:

```java
public enum Exchange {
    BINANCE,
    OKX,
    BYBIT,
    HUOBI
}
```

**实现代码**: [Exchange.java](smart-quantify-common/src/main/java/com/smartquantify/common/enums/Exchange.java)

**信号类型**:

```java
public enum SignalType {
    ENTRY,
    EXIT,
    STOP_LOSS,
    TAKE_PROFIT
}
```

**实现代码**: [SignalType.java](smart-quantify-common/src/main/java/com/smartquantify/common/enums/SignalType.java)

**信号状态**:

```java
public enum SignalStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    EXECUTED
}
```

**实现代码**: [SignalStatus.java](smart-quantify-common/src/main/java/com/smartquantify/common/enums/SignalStatus.java)

### 5.2 信号实体

```java
public class Signal {
    private String id;
    private String strategyId;
    private String strategyName;
    private String symbol;
    private Side side;
    private SignalType type;
    private BigDecimal price;
    private BigDecimal quantity;
    private Exchange exchange;
    private InstrumentType instrumentType;
    private Integer priority;
    private LocalDateTime createdAt;
    private SignalStatus status;
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
}
```

**实现代码**: [Signal.java](smart-quantify-common/src/main/java/com/smartquantify/common/model/Signal.java)

## 6. API 文档

### 6.1 市场数据接口

#### 6.1.1 获取K线数据

```
GET /api/v1/market/klines
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exchange | string | 是 | 交易所：BINANCE/OKX/BYBIT/HUOBI |
| symbol | string | 是 | 交易对，如 BTCUSDT |
| interval | string | 是 | 时间间隔：1m/5m/15m/1h/4h/1d |
| limit | int | 否 | 返回数量，默认 100 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "klines": [
      {
        "symbol": "BTCUSDT",
        "interval": "1h",
        "openTime": "2024-01-01T00:00:00",
        "open": 45000.0,
        "high": 45100.0,
        "low": 44900.0,
        "close": 45050.0,
        "volume": 100.5,
        "quoteVolume": 4527525.0,
        "closeTime": "2024-01-01T01:00:00"
      }
    ]
  }
}
```

**实现代码**: [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java#L31-L42)

#### 6.1.2 获取订单簿

```
GET /api/v1/market/orderbook
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exchange | string | 是 | 交易所 |
| symbol | string | 是 | 交易对 |
| limit | int | 否 | 返回深度，默认 100 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderBook": {
      "symbol": "BTCUSDT",
      "timestamp": 1704067200000,
      "asks": [
        {"price": 45100.0, "quantity": 10.0}
      ],
      "bids": [
        {"price": 44900.0, "quantity": 10.0}
      ]
    }
  }
}
```

**实现代码**: [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java#L50-L60)

#### 6.1.3 获取Ticker

```
GET /api/v1/market/ticker
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exchange | string | 是 | 交易所 |
| symbol | string | 否 | 交易对，不传则返回全部 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "ticker": {
      "symbol": "BTCUSDT",
      "lastPrice": 45050.0,
      "openPrice": 44800.0,
      "highPrice": 45200.0,
      "lowPrice": 44700.0,
      "volume24h": 10000.0,
      "quoteVolume24h": 450500000.0,
      "priceChange24h": 250.0,
      "priceChangePercent24h": 0.56,
      "timestamp": "2024-01-01T12:00:00"
    }
  }
}
```

**实现代码**: [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java#L68-L82)

#### 6.1.4 获取合约列表

```
GET /api/v1/market/instruments
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exchange | string | 是 | 交易所 |
| type | string | 否 | 合约类型：SPOT/FUTURES/OPTIONS |

**实现代码**: [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java#L90-L100)

#### 6.1.5 订阅K线

```
POST /api/v1/market/subscribe/klines
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exchange | string | 是 | 交易所 |
| symbol | string | 是 | 交易对 |
| interval | string | 是 | 时间间隔 |

**实现代码**: [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java#L108-L116)

#### 6.1.6 订阅订单簿

```
POST /api/v1/market/subscribe/orderbook
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exchange | string | 是 | 交易所 |
| symbol | string | 是 | 交易对 |

**实现代码**: [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java#L124-L132)

#### 6.1.7 取消订阅

```
POST /api/v1/market/unsubscribe
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exchange | string | 是 | 交易所 |
| symbol | string | 是 | 交易对 |

**实现代码**: [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java#L140-L148)

### 6.2 策略管理接口

#### 6.2.1 创建策略

```
POST /api/v1/strategies
```

**请求体**:

```json
{
  "name": "MA交叉策略",
  "description": "基于5日均线和20日均线交叉的策略",
  "type": "MA_CROSS",
  "language": "python",
  "exchange": "BINANCE",
  "symbols": ["BTCUSDT", "ETHUSDT"],
  "interval": "1h",
  "parameters": {
    "short_period": "5",
    "long_period": "20"
  },
  "config": {}
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "strategy": {
      "id": "uuid-xxx",
      "name": "MA交叉策略",
      "type": "MA_CROSS",
      "language": "python",
      "status": "STOPPED",
      "exchange": "BINANCE",
      "symbols": "[\"BTCUSDT\",\"ETHUSDT\"]",
      "interval": "1h",
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    }
  }
}
```

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L30-L38)

#### 6.2.2 获取策略列表

```
GET /api/v1/strategies
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "strategies": [
      {
        "id": "uuid-xxx",
        "name": "MA交叉策略",
        "type": "MA_CROSS",
        "status": "RUNNING",
        "exchange": "BINANCE",
        "interval": "1h",
        "createdAt": "2024-01-01T00:00:00"
      }
    ]
  }
}
```

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L59-L67)

#### 6.2.3 获取策略详情

```
GET /api/v1/strategies/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 策略ID |

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L40-L48)

#### 6.2.4 启动策略

```
POST /api/v1/strategies/{id}/start
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 策略ID |

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L74-L82)

#### 6.2.5 停止策略

```
POST /api/v1/strategies/{id}/stop
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 策略ID |

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L89-L97)

#### 6.2.6 暂停策略

```
POST /api/v1/strategies/{id}/pause
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 策略ID |

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L104-L112)

#### 6.2.7 更新策略

```
PUT /api/v1/strategies/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 策略ID |

**请求体**:

```json
{
  "name": "MA交叉策略(更新)",
  "description": "更新后的描述",
  "parameters": {
    "short_period": "10",
    "long_period": "30"
  }
}
```

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L119-L127)

#### 6.2.8 删除策略

```
DELETE /api/v1/strategies/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 策略ID |

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L134-L141)

### 6.3 风控管理接口

#### 6.3.1 创建风控规则

```
POST /api/v1/risk/rules
```

**请求体**:

```json
{
  "name": "最大仓位限制",
  "description": "单笔交易最大仓位不超过10000 USDT",
  "type": "POSITION_LIMIT",
  "enabled": true,
  "priority": 1,
  "conditions": {
    "maxPosition": "10000.0"
  },
  "actions": ["REJECT"],
  "scope": "strategy",
  "strategyIds": ["uuid-xxx"],
  "symbols": ["BTCUSDT"],
  "exchanges": ["BINANCE"]
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "rule": {
      "id": "rule-uuid",
      "name": "最大仓位限制",
      "type": "POSITION_LIMIT",
      "enabled": true,
      "priority": 1,
      "createdAt": "2024-01-01T00:00:00"
    }
  }
}
```

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L30-L38)

#### 6.3.2 获取风控规则列表

```
GET /api/v1/risk/rules
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "rules": [
      {
        "id": "rule-uuid",
        "name": "最大仓位限制",
        "type": "POSITION_LIMIT",
        "enabled": true,
        "priority": 1
      }
    ]
  }
}
```

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L59-L67)

#### 6.3.3 获取风控规则详情

```
GET /api/v1/risk/rules/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 规则ID |

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L40-L48)

#### 6.3.4 更新风控规则

```
PUT /api/v1/risk/rules/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 规则ID |

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L104-L112)

#### 6.3.5 删除风控规则

```
DELETE /api/v1/risk/rules/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 规则ID |

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L119-L127)

#### 6.3.6 风险检查

```
POST /api/v1/risk/check
```

**请求体**:

```json
{
  "strategyId": "uuid-xxx",
  "symbol": "BTCUSDT",
  "side": "BUY",
  "quantity": 0.1,
  "price": 45000.0,
  "exchange": "BINANCE"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "passed": true,
    "results": [
      {
        "passed": true,
        "ruleId": "rule-uuid",
        "ruleName": "最大仓位限制",
        "action": "ALLOW"
      }
    ]
  }
}
```

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L74-L82)

#### 6.3.7 获取风控限额

```
GET /api/v1/risk/limits
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| strategyId | string | 否 | 策略ID |
| exchange | string | 否 | 交易所 |

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L134-L142)

#### 6.3.8 获取风控状态

```
GET /api/v1/risk/state
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| strategyId | string | 否 | 策略ID |

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L149-L157)

### 6.4 订单执行接口

#### 6.4.1 提交订单

```
POST /api/v1/orders
```

**请求体**:

```json
{
  "exchange": "BINANCE",
  "symbol": "BTCUSDT",
  "side": "BUY",
  "type": "MARKET",
  "quantity": 0.1,
  "price": 45000.0,
  "timeInForce": "GTC",
  "clientOrderId": "my-order-001",
  "strategyId": "strategy-uuid"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "order": {
      "id": "order-uuid",
      "symbol": "BTCUSDT",
      "side": "BUY",
      "type": "MARKET",
      "quantity": 0.1,
      "status": "NEW",
      "exchange": "BINANCE",
      "strategyId": "strategy-uuid",
      "createdAt": "2024-01-01T00:00:00"
    }
  }
}
```

**实现代码**: [ExecutionController.java](smart-quantify-execution/src/main/java/com/smartquantify/execution/controller/ExecutionController.java#L30-L38)

#### 6.4.2 获取订单

```
GET /api/v1/orders/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 订单ID |

**实现代码**: [ExecutionController.java](smart-quantify-execution/src/main/java/com/smartquantify/execution/controller/ExecutionController.java#L40-L48)

#### 6.4.3 获取订单列表

```
GET /api/v1/orders
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 订单状态：NEW/PARTIALLY_FILLED/FILLED/CANCELLED/REJECTED |
| symbol | string | 否 | 交易对 |
| exchange | string | 否 | 交易所 |
| strategyId | string | 否 | 策略ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orders": [
      {
        "id": "order-uuid",
        "symbol": "BTCUSDT",
        "side": "BUY",
        "type": "MARKET",
        "quantity": 0.1,
        "status": "FILLED",
        "exchange": "BINANCE",
        "createdAt": "2024-01-01T00:00:00"
      }
    ]
  }
}
```

**实现代码**: [ExecutionController.java](smart-quantify-execution/src/main/java/com/smartquantify/execution/controller/ExecutionController.java#L56-L64)

#### 6.4.4 取消订单

```
DELETE /api/v1/orders/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 订单ID |

**实现代码**: [ExecutionController.java](smart-quantify-execution/src/main/java/com/smartquantify/execution/controller/ExecutionController.java#L71-L79)

#### 6.4.5 同步订单状态

```
POST /api/v1/orders/{id}/sync
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 订单ID |

**实现代码**: [ExecutionController.java](smart-quantify-execution/src/main/java/com/smartquantify/execution/controller/ExecutionController.java#L86-L94)

### 6.5 回测接口

#### 6.5.1 创建回测任务

```
POST /api/v1/backtests
```

**请求体**:

```json
{
  "strategyId": "uuid-xxx",
  "strategyName": "MA交叉策略",
  "exchange": "BINANCE",
  "symbol": "BTCUSDT",
  "interval": "1h",
  "startTime": "2024-01-01T00:00:00",
  "endTime": "2024-01-31T23:59:59",
  "initialCapital": 10000.0,
  "parameters": {
    "short_period": "5",
    "long_period": "20"
  }
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "backtest": {
      "id": "backtest-uuid",
      "strategyId": "uuid-xxx",
      "symbol": "BTCUSDT",
      "status": "PENDING",
      "exchange": "BINANCE",
      "initialCapital": 10000.0,
      "createdAt": "2024-01-01T00:00:00"
    }
  }
}
```

**实现代码**: [BacktestController.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/controller/BacktestController.java#L30-L38)

#### 6.5.2 获取回测任务列表

```
GET /api/v1/backtests
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "backtests": [
      {
        "id": "backtest-uuid",
        "strategyId": "uuid-xxx",
        "symbol": "BTCUSDT",
        "status": "COMPLETED",
        "createdAt": "2024-01-01T00:00:00"
      }
    ]
  }
}
```

**实现代码**: [BacktestController.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/controller/BacktestController.java#L59-L67)

#### 6.5.3 获取回测任务详情

```
GET /api/v1/backtests/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 回测任务ID |

**实现代码**: [BacktestController.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/controller/BacktestController.java#L40-L48)

#### 6.5.4 运行回测

```
POST /api/v1/backtests/{id}/run
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 回测任务ID |

**实现代码**: [BacktestController.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/controller/BacktestController.java#L74-L82)

#### 6.5.5 取消回测

```
POST /api/v1/backtests/{id}/cancel
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 回测任务ID |

**实现代码**: [BacktestController.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/controller/BacktestController.java#L89-L97)

#### 6.5.6 获取回测结果

```
GET /api/v1/backtests/{id}/result
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 回测任务ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "result": {
      "taskId": "backtest-uuid",
      "strategyId": "uuid-xxx",
      "symbol": "BTCUSDT",
      "initialCapital": 10000.0,
      "finalCapital": 12500.0,
      "totalReturn": 0.25,
      "annualizedReturn": 0.30,
      "maxDrawdown": 0.08,
      "sharpeRatio": 2.5,
      "winRate": 0.65,
      "totalTrades": 50,
      "winningTrades": 32,
      "losingTrades": 18
    }
  }
}
```

## 7. 消息队列

### 7.1 Kafka 主题

| 主题 | 内容 | 生产者 | 消费者 |
|------|------|--------|--------|
| `smart_quantify.klines` | K线数据 | market-service | strategy-service, clickhouse-sink |
| `smart_quantify.orderbook` | 订单簿数据 | market-service | strategy-service |
| `smart_quantify.tickers` | Ticker数据 | market-service | - |
| `smart_quantify.signals` | 交易信号 | strategy-service | risk-service |
| `order-created` | 订单创建事件 | execution-service | execution-consumer |
| `order-executed` | 订单执行事件 | execution-consumer | strategy-service |

### 7.2 Kafka 配置

```yaml
spring:
  kafka:
    bootstrap-servers: kafka:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
    consumer:
      group-id: smart_quantify_consumer
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
```

## 8. 数据存储

### 8.1 MySQL

用于存储业务数据，如策略、风控规则、订单等。

**数据库表**:

| 表名 | 描述 |
|------|------|
| `strategy` | 策略表 |
| `risk_rule` | 风控规则表 |
| `risk_limit` | 风险限额表 |
| `risk_state` | 风险状态表 |
| `orders` | 订单表 |
| `trade` | 成交明细表 |
| `signal` | 信号表 |
| `exchange_config` | 交易所配置表 |

### 8.2 ClickHouse

用于存储时间序列市场数据（K线、订单簿、Ticker）。

**数据表**:

| 表名 | 描述 |
|------|------|
| `klines` | K线数据表 |
| `orderbook_snapshots` | 订单簿快照表 |
| `tickers` | Ticker数据表 |

**建表语句**:

```sql
CREATE TABLE klines (
    symbol String,
    interval String,
    open_time DateTime,
    open Float64,
    high Float64,
    low Float64,
    close Float64,
    volume Float64,
    quote_volume Float64,
    close_time DateTime
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(open_time)
ORDER BY (symbol, interval, open_time);
```

### 8.3 Redis

用于缓存和实时数据存储，支持 Spring Data Redis 和 Redisson 双客户端。

**Redis 配置**:

使用 Lettuce 连接池管理 Redis 连接，配合 Redisson 实现分布式锁。

| 参数 | 值 |
|------|-----|
| max-active | 16 |
| max-idle | 8 |
| min-idle | 2 |
| max-wait | 2000ms |

**实现代码**: [RedisConfig.java](smart-quantify-common/src/main/java/com/smartquantify/common/config/RedisConfig.java)

**缓存键**:

| Key | 类型 | 描述 |
|-----|------|------|
| `strategy:{id}` | Hash | 策略信息 |
| `risk_rules` | Set | 风控规则列表 |
| `order:{id}` | Hash | 订单信息 |
| `ticker:{symbol}` | String | Ticker数据 |
| `orderbook:{symbol}` | String | 订单簿数据 |

### 8.4 Nacos

用于服务注册发现和配置管理。

## 9. 监控与告警

### 9.1 Prometheus

通过 Spring Boot Actuator 暴露指标，Prometheus 采集。

**指标类型**:

| 指标 | 描述 |
|------|------|
| `strategy_count` | 策略数量 |
| `strategy_running_count` | 运行中策略数量 |
| `signal_total` | 信号总数 |
| `order_total` | 订单总数 |
| `order_filled_count` | 已成交订单数 |
| `risk_check_passed_total` | 风控检查通过数 |
| `risk_check_failed_total` | 风控检查失败数 |
| `exchange_api_requests_total` | 交易所API请求数 |
| `exchange_api_errors_total` | 交易所API错误数 |

### 9.2 Alert Rules

```yaml
groups:
  - name: smart_quantify_alerts
    rules:
      - alert: StrategyDown
        expr: strategy_running_count < 1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "策略引擎无运行策略"
      
      - alert: HighRiskFailedRate
        expr: rate(risk_check_failed_total[5m]) / rate(risk_check_passed_total[5m]) > 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "风控检查失败率过高"
      
      - alert: ExchangeApiErrors
        expr: rate(exchange_api_errors_total[5m]) > 10
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "交易所API错误过多"
```

### 9.3 Grafana

通过 Docker 部署，用于可视化监控面板。

## 10. 部署

### 10.1 Docker Compose

```yaml
version: '3.8'
services:
  nacos:
    image: nacos/nacos-server:v2.3.0
    ports:
      - "8848:8848"
    environment:
      - MODE=standalone
  
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=password
      - MYSQL_DATABASE=smart_quantify
  
  redis:
    image: redis:7.2
    ports:
      - "6379:6379"
  
  kafka:
    image: wurstmeister/kafka:2.13-2.8.1
    ports:
      - "9092:9092"
    environment:
      - KAFKA_ADVERTISED_HOST_NAME=kafka
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
  
  zookeeper:
    image: wurstmeister/zookeeper:3.4.6
    ports:
      - "2181:2181"
  
  clickhouse:
    image: yandex/clickhouse-server:24.3
    ports:
      - "8123:8123"
      - "9000:9000"
  
  gateway:
    build: ./smart-quantify-gateway
    ports:
      - "8080:8080"
    depends_on:
      - nacos
  
  strategy-service:
    build: ./smart-quantify-strategy
    ports:
      - "8081:8081"
    depends_on:
      - nacos
      - mysql
      - kafka
  
  risk-service:
    build: ./smart-quantify-risk
    ports:
      - "8082:8082"
    depends_on:
      - nacos
      - mysql
      - kafka
  
  execution-service:
    build: ./smart-quantify-execution
    ports:
      - "8083:8083"
    depends_on:
      - nacos
      - mysql
      - kafka
  
  market-service:
    build: ./smart-quantify-market
    ports:
      - "8084:8084"
    depends_on:
      - nacos
      - kafka
      - clickhouse
```

### 10.2 Kubernetes

**目录结构**:

```
scripts/k8s/
├── namespace.yaml
├── configmap.yaml
├── nacos.yaml
├── mysql.yaml
├── redis.yaml
├── kafka.yaml
├── clickhouse.yaml
├── gateway.yaml
├── strategy-service.yaml
├── risk-service.yaml
├── execution-service.yaml
├── market-service.yaml
└── monitoring.yaml
```

## 11. 项目结构

```
smart-quantify-java/
├── pom.xml                              # 父POM
├── smart-quantify-common/               # 公共模块
│   ├── pom.xml
│   └── src/main/java/com/smartquantify/common/
│       ├── config/                      # 配置类（缓存、Redis、熔断）
│       ├── enums/                       # 枚举类
│       ├── model/                       # 通用模型
│       ├── event/                       # 事件类（Kafka）
│       └── util/                        # 工具类
├── smart-quantify-api/                  # API定义模块
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/smartquantify/api/
│       │   ├── dto/                    # 请求/响应DTO
│       │   └── grpc/                   # gRPC定义
│       └── proto/                       # Protobuf定义
├── smart-quantify-gateway/              # API网关
│   ├── pom.xml
│   └── src/main/java/com/smartquantify/gateway/
│       ├── GatewayApplication.java
│       └── config/                       # 限流配置
├── smart-quantify-strategy/             # 策略引擎
│   ├── pom.xml
│   └── src/main/java/com/smartquantify/strategy/
│       ├── StrategyApplication.java
│       ├── controller/                  # REST控制器
│       ├── service/                     # 业务服务
│       ├── repository/                  # 数据访问
│       ├── entity/                      # 实体类
│       ├── python/                      # Python策略支持
│       └── kafka/                       # Kafka消费者
├── smart-quantify-risk/                 # 风控引擎
│   ├── pom.xml
│   └── src/main/java/com/smartquantify/risk/
│       ├── RiskApplication.java
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       ├── rule/                        # 规则引擎
│       └── kafka/
├── smart-quantify-execution/            # 订单执行服务
│   ├── pom.xml
│   └── src/main/java/com/smartquantify/execution/
│       ├── ExecutionApplication.java
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       ├── consumer/                     # Kafka消费者（订单执行）
│       └── kafka/
├── smart-quantify-market/               # 市场服务
│   ├── pom.xml
│   └── src/main/java/com/smartquantify/market/
│       ├── MarketApplication.java
│       ├── controller/
│       ├── service/
│       ├── kafka/
│       └── websocket/                   # WebSocket订阅
├── smart-quantify-backtest/             # 回测服务
│   ├── pom.xml
│   └── src/main/java/com/smartquantify/backtest/
│       ├── BacktestApplication.java
│       ├── config/                      # 异步线程池配置
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       └── engine/                      # 回测引擎
├── smart-quantify-adapter/              # 交易所适配器
│   ├── pom.xml
│   └── src/main/java/com/smartquantify/adapter/
│       ├── ExchangeAdapter.java         # 适配器接口
│       ├── binance/                     # Binance适配器
│       ├── okx/                         # OKX适配器
│       ├── bybit/                       # Bybit适配器
│       └── huobi/                       # Huobi适配器
├── smart-quantify-python/               # Python策略支持模块
│   ├── requirements.txt
│   └── app/
│       ├── main.py                      # gRPC服务入口
│       ├── strategy/                    # 策略执行器
│       ├── grpc/                        # gRPC生成代码
│       └── utils/                       # 工具类
└── README.md
```

## 12. 关键设计决策

### 12.1 策略执行模式

- **Java策略**: 直接实现 `StrategyExecutor` 接口
- **Python策略**: 使用 gRPC Sidecar 模式，策略服务通过 gRPC 调用独立的 Python 进程，支持 Python 3.x 及主流数据分析库
- **策略脚本存储**: 存储在数据库中，支持动态修改
- **策略隔离**: 每个策略运行在独立的进程/线程中，避免相互影响

### 12.2 风控规则引擎

- 使用轻量级自定义规则引擎，规则存储在数据库中，支持动态更新
- 规则优先级机制，支持规则排序和条件匹配
- 规则评估流程：过滤 → 排序 → 评估 → 动作执行
- 后续可按需引入 Drools 或其他规则引擎

### 12.3 订单执行机制

- 异步执行模式，使用 Kafka 解耦
- 支持信号转订单的自动转换
- 支持订单状态同步

### 12.4 市场数据处理

- WebSocket 实时订阅
- Kafka 异步写入 ClickHouse
- Redis 缓存实时数据

## 13. Spring Boot 注解配置

各模块 Application 类需要添加相应的启用注解：

| 模块 | 注解 | 说明 |
|------|------|------|
| StrategyApplication | `@EnableDiscoveryClient`, `@EnableCaching` | 服务注册、缓存支持 |
| RiskApplication | `@EnableDiscoveryClient`, `@EnableCaching` | 服务注册、缓存支持 |
| ExecutionApplication | `@EnableDiscoveryClient`, `@EnableCaching`, `@EnableKafka` | 服务注册、缓存、Kafka |
| MarketApplication | `@EnableDiscoveryClient`, `@EnableCaching` | 服务注册、缓存支持 |
| BacktestApplication | `@EnableDiscoveryClient`, `@EnableCaching`, `@EnableAsync` | 服务注册、缓存、异步执行 |
| GatewayApplication | `@EnableDiscoveryClient` | 服务注册 |

## 14. 安全设计

### 14.1 API 认证

- 使用 JWT Token 认证
- API Key + Secret 签名验证
- 接口限流和熔断

### 14.2 数据加密

- 数据库敏感字段加密（API Key/Secret）
- HTTPS 传输加密
- Redis 数据加密

### 14.3 访问控制

- 基于角色的权限控制（RBAC）
- IP 白名单限制
- 操作日志审计

## 15. 扩展性设计

### 15.1 新交易所接入

1. 实现 `ExchangeAdapter` 接口
2. 添加配置类
3. 在 Nacos 中注册配置
4. 重启服务自动加载

### 15.2 新策略类型接入

1. 创建策略执行器实现类
2. 注册到策略工厂
3. 在前端配置策略参数

### 15.3 新风控规则接入

1. 在枚举中添加规则类型
2. 实现规则评估逻辑
3. 在数据库中配置规则

## 16. 测试策略

### 16.1 单元测试

- 核心业务逻辑测试
- 规则引擎测试
- 适配器接口测试

### 16.2 集成测试

- 服务间调用测试
- Kafka 消息传递测试
- 数据库操作测试

### 16.3 端到端测试

- 完整交易流程测试
- 策略执行流程测试
- 风控检查流程测试

## 17. 性能指标

| 指标 | 目标值 |
|------|--------|
| 策略执行延迟 | < 100ms |
| 风控检查延迟 | < 50ms |
| 订单执行延迟 | < 200ms |
| 市场数据处理 | 10000 msg/s |
| 服务并发量 | 10000 QPS |
| 消息队列吞吐量 | 100000 msg/s |

## 18. 高并发高可用设计

### 18.1 缓存策略

采用多级缓存架构，结合 Caffeine 本地缓存和 Redis 分布式缓存，提高数据访问性能。

**Caffeine 本地缓存配置**：

| 缓存名称 | 初始容量 | 最大容量 | 过期时间 | 访问过期 |
|----------|----------|----------|----------|----------|
| klines | 500 | 50000 | 10s | 5s |
| orderBook | 500 | 50000 | 10s | 5s |
| ticker | 500 | 50000 | 10s | 5s |
| instruments | 500 | 50000 | 10s | 5s |
| riskRules | 500 | 50000 | 10s | 5s |
| riskState | 500 | 50000 | 10s | 5s |

**实现代码**: [CacheConfig.java](smart-quantify-common/src/main/java/com/smartquantify/common/config/CacheConfig.java)

**Redis 分布式缓存配置**：

| 缓存名称 | 过期时间 |
|----------|----------|
| klines | 30s |
| orderBook | 15s |
| ticker | 10s |
| instruments | 5min |
| riskRules | 10min |
| riskState | 5min |

### 18.2 分布式锁

支持 Redis 和 etcd 两种分布式锁模式，通过配置切换。

**接口定义**:

```java
public interface DistributedLock {
    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;
    void lock(String lockKey, long leaseTime, TimeUnit unit);
    void unlock(String lockKey);
    boolean isLocked(String lockKey);
}
```

**实现代码**: [DistributedLock.java](smart-quantify-common/src/main/java/com/smartquantify/common/lock/DistributedLock.java)

**Redis 实现**: 使用 Redisson 实现，支持可重入锁、自动续期。

**实现代码**: [RedisDistributedLock.java](smart-quantify-common/src/main/java/com/smartquantify/common/lock/RedisDistributedLock.java)

**Etcd 实现**: 基于内存模拟，实际生产环境应接入 etcd 客户端。

**实现代码**: [EtcdDistributedLock.java](smart-quantify-common/src/main/java/com/smartquantify/common/lock/EtcdDistributedLock.java)

**工厂模式**: 通过配置 `smartquantify.lock.mode` 切换锁模式。

**实现代码**: [DistributedLockFactory.java](smart-quantify-common/src/main/java/com/smartquantify/common/lock/DistributedLockFactory.java)

### 18.3 数据库读写分离

通过 Spring AOP 实现自动读写分离，读操作路由到从库，写操作路由到主库。

**配置示例**:

```yaml
spring:
  datasource:
    master:
      url: jdbc:mysql://localhost:3306/smart_quantify
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5
        pool-name: master-hikari-pool
    slave:
      url: jdbc:mysql://localhost:3306/smart_quantify
      hikari:
        maximum-pool-size: 30
        minimum-idle: 10
        pool-name: slave-hikari-pool
```

**实现代码**: [DataSourceConfig.java](smart-quantify-common/src/main/java/com/smartquantify/common/config/DataSourceConfig.java)

**AOP 切面**: 自动识别 CRUD 操作并路由到对应数据源。

**实现代码**: [DataSourceAspect.java](smart-quantify-common/src/main/java/com/smartquantify/common/aspect/DataSourceAspect.java)

### 18.4 异步线程池

配置多个业务线程池，隔离不同业务的资源使用。

**线程池配置**:

| 线程池名称 | 核心线程数 | 最大线程数 | 队列容量 | 线程前缀 |
|------------|------------|------------|----------|----------|
| commonExecutor | 8 | 16 | 1000 | common- |
| backtestExecutor | 4 | 8 | 50 | backtest- |
| orderExecutor | 10 | 20 | 500 | order- |
| marketDataExecutor | 12 | 24 | 2000 | market- |
| riskExecutor | 6 | 12 | 200 | risk- |
| strategyExecutor | 8 | 16 | 100 | strategy- |

**实现代码**: [AsyncThreadPoolConfig.java](smart-quantify-common/src/main/java/com/smartquantify/common/config/AsyncThreadPoolConfig.java)

### 18.5 熔断与限流

使用 Resilience4j 实现熔断器、限流器和舱壁模式。

**熔断器配置**:

| 参数 | 值 |
|------|-----|
| failureRateThreshold | 50% |
| waitDurationInOpenState | 30s |
| permittedNumberOfCallsInHalfOpenState | 5 |
| slidingWindowSize | 100 |
| minimumNumberOfCalls | 10 |

**限流器配置**:

| 参数 | 值 |
|------|-----|
| limitForPeriod | 100 |
| limitRefreshPeriod | 1s |
| timeoutDuration | 5s |

**舱壁配置**:

| 参数 | 值 |
|------|-----|
| maxConcurrentCalls | 20 |
| maxWaitDuration | 500ms |

**实现代码**: [Resilience4jConfig.java](smart-quantify-common/src/main/java/com/smartquantify/common/config/Resilience4jConfig.java)

**交易所适配器集成**: 在 Binance、OKX、Bybit、Huobi 适配器中添加熔断注解。

**实现代码**: [BinanceAdapter.java](smart-quantify-adapter/src/main/java/com/smartquantify/adapter/binance/BinanceAdapter.java)

### 18.6 Kafka 优化

**生产者优化**:

| 参数 | 值 |
|------|-----|
| acks | all |
| batch-size | 32768 |
| linger-ms | 10 |
| retries | 5 |
| compression-type | lz4 |
| max-in-flight-requests-per-connection | 1 |

**消费者优化**:

| 参数 | 值 |
|------|-----|
| fetch-min-size | 1 |
| fetch-max-wait | 5000ms |
| max-poll-records | 100 |
| max-poll-interval-ms | 300000ms |
| session-timeout-ms | 30000ms |

### 18.7 接口幂等性

通过 Redis + AOP 实现接口幂等性校验，支持多种幂等键提取方式。

**注解定义**:

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    String key() default "";
    long expireTime() default 60;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    String prefix() default "idempotent:";
}
```

**实现代码**: [Idempotent.java](smart-quantify-common/src/main/java/com/smartquantify/common/annotation/Idempotent.java)

**AOP 切面**: 支持从 Header、Parameter 或自动生成 UUID 提取幂等键。

**实现代码**: [IdempotentAspect.java](smart-quantify-common/src/main/java/com/smartquantify/common/aspect/IdempotentAspect.java)

### 18.8 API 网关限流

支持多种限流维度：IP、用户ID、API Key、策略ID。

**限流配置**:

| 服务 | 限流维度 | 补充速率 | 突发容量 |
|------|----------|----------|----------|
| market-service | IP | 200/s | 400 |
| strategy-service | 用户ID | 100/s | 200 |
| risk-service | IP | 150/s | 300 |
| execution-service | API Key | 300/s | 600 |
| backtest-service | 用户ID | 30/s | 100 |

**实现代码**: [RateLimitConfig.java](smart-quantify-gateway/src/main/java/com/smartquantify/gateway/config/RateLimitConfig.java)

### 18.9 服务健康检查

集成 Spring Boot Actuator，提供健康检查和监控端点。

**自定义健康指示器**:

| 指示器 | 检查内容 |
|--------|----------|
| redisHealthIndicator | Redis 连接状态 |
| kafkaHealthIndicator | Kafka 可用性 |
| exchangeAdaptersHealthIndicator | 交易所适配器状态 |

**实现代码**: [HealthIndicatorConfig.java](smart-quantify-common/src/main/java/com/smartquantify/common/config/HealthIndicatorConfig.java)

**端点配置**:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
```

### 18.10 全局异常处理

增强全局异常处理，添加熔断、限流、服务不可用等异常类型。

**新增异常类型**:

| 异常类型 | HTTP状态码 | 用途 |
|----------|------------|------|
| CircuitBreakerException | 503 | 熔断器打开 |
| RateLimitException | 429 | 限流触发 |
| ServiceUnavailableException | 503 | 服务降级 |

**实现代码**: [GlobalExceptionHandler.java](smart-quantify-common/src/main/java/com/smartquantify/common/exception/GlobalExceptionHandler.java), [CircuitBreakerException.java](smart-quantify-common/src/main/java/com/smartquantify/common/exception/CircuitBreakerException.java), [RateLimitException.java](smart-quantify-common/src/main/java/com/smartquantify/common/exception/RateLimitException.java), [ServiceUnavailableException.java](smart-quantify-common/src/main/java/com/smartquantify/common/exception/ServiceUnavailableException.java)

### 18.11 高可用架构

**服务注册与发现**: Nacos 实现服务注册、心跳检测、故障转移。

**服务降级**: 
- 熔断降级：交易所 API 调用失败时返回缓存数据或默认值
- 限流降级：超过限流阈值时返回 429 状态码
- 服务降级：依赖服务不可用时返回缓存数据

**故障恢复**:
- Kafka 消息重试机制
- 数据库连接池自动重连
- Redis 哨兵模式支持
- 健康检查触发服务下线

**数据一致性**:
- 订单使用 Outbox 模式，保证最终一致性
- Kafka 事务消息保证消息不丢失
- 分布式锁保证同一操作的唯一性