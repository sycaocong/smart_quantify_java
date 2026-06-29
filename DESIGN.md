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

**核心服务接口**:

| 方法 | 功能 |
|------|------|
| `submitOrder(OrderRequest)` | 提交订单 |
| `getOrder(String id)` | 获取订单 |
| `listOrders(String status, String symbol, String exchange)` | 列出订单 |
| `cancelOrder(String id)` | 取消订单 |
| `syncOrders()` | 同步交易所订单 |

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

**核心服务接口**:

| 方法 | 功能 |
|------|------|
| `getKlines(String symbol, String interval, Integer limit)` | 获取K线数据 |
| `getOrderBook(String symbol, Integer limit)` | 获取订单簿 |
| `getTicker(String symbol)` | 获取Ticker |
| `getInstruments(String exchange, String type)` | 获取合约列表 |
| `subscribeKlines(String symbol, String interval)` | 订阅K线 |
| `subscribeOrderBook(String symbol)` | 订阅订单簿 |

### 4.6 交易所适配器

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

**支持的交易所**:

| 交易所 | 类名 | 状态 |
|--------|------|------|
| Binance | `BinanceAdapter` | 支持 |
| OKX | `OkxAdapter` | 支持 |
| Bybit | `BybitAdapter` | 支持 |
| Huobi | `HuobiAdapter` | 支持 |

## 5. 数据模型

### 5.1 通用枚举

**交易方向**:

```java
public enum Side {
    BUY,
    SELL
}
```

**订单类型**:

```java
public enum OrderType {
    MARKET,
    LIMIT,
    STOP,
    STOP_LIMIT
}
```

**交易所**:

```java
public enum Exchange {
    BINANCE,
    OKX,
    BYBIT,
    HUOBI
}
```

**信号类型**:

```java
public enum SignalType {
    ENTRY,
    EXIT,
    STOP_LOSS,
    TAKE_PROFIT
}
```

**信号状态**:

```java
public enum SignalStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    EXECUTED
}
```

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

## 6. API 文档

### 6.1 市场数据接口

#### 6.1.1 获取K线数据

```
GET /api/v1/market/klines
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| symbol | string | 是 | 交易对，如 BTCUSDT |
| interval | string | 是 | 时间间隔，如 1m, 5m, 1h, 1d |
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
        "interval": "1m",
        "openTime": "2024-01-01T00:00:00Z",
        "open": 45000.0,
        "high": 45100.0,
        "low": 44900.0,
        "close": 45050.0,
        "volume": 100.5,
        "quoteVolume": 4527525.0
      }
    ]
  }
}
```

#### 6.1.2 获取订单簿

```
GET /api/v1/market/orderbook
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| symbol | string | 是 | 交易对 |
| limit | int | 否 | 返回深度，默认 100 |

#### 6.1.3 获取Ticker

```
GET /api/v1/market/ticker
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| symbol | string | 否 | 交易对，不传则返回全部 |

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
  "script": "def execute(kline):\n    return signal"
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
      "status": "STOPPED",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  }
}
```

#### 6.2.2 获取策略列表

```
GET /api/v1/strategies
```

#### 6.2.3 获取策略详情

```
GET /api/v1/strategies/{id}
```

#### 6.2.4 启动策略

```
POST /api/v1/strategies/{id}/start
```

#### 6.2.5 停止策略

```
POST /api/v1/strategies/{id}/stop
```

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

#### 6.3.2 风险检查

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

### 6.4 订单执行接口

#### 6.4.1 提交订单

```
POST /api/v1/orders
```

**请求体**:

```json
{
  "symbol": "BTCUSDT",
  "side": "BUY",
  "type": "MARKET",
  "quantity": 0.1,
  "price": 45000.0,
  "timeInForce": "GTC",
  "clientOrderId": "my-order-001"
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
      "createdAt": "2024-01-01T00:00:00Z"
    }
  }
}
```

#### 6.4.2 获取订单

```
GET /api/v1/orders/{id}
```

#### 6.4.3 获取订单列表

```
GET /api/v1/orders
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 订单状态 |
| symbol | string | 否 | 交易对 |
| exchange | string | 否 | 交易所 |

#### 6.4.4 取消订单

```
DELETE /api/v1/orders/{id}
```

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
  "symbol": "BTCUSDT",
  "interval": "1h",
  "startTime": "2024-01-01T00:00:00Z",
  "endTime": "2024-01-31T23:59:59Z",
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
      "createdAt": "2024-01-01T00:00:00Z"
    }
  }
}
```

#### 6.5.2 获取回测任务列表

```
GET /api/v1/backtests
```

#### 6.5.3 获取回测任务详情

```
GET /api/v1/backtests/{id}
```

#### 6.5.4 运行回测

```
POST /api/v1/backtests/{id}/run
```

#### 6.5.5 取消回测

```
POST /api/v1/backtests/{id}/cancel
```

#### 6.5.6 获取回测结果

```
GET /api/v1/backtests/{id}/result
```

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
| `smart_quantify.order_events` | 订单事件 | execution-service | - |

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

用于缓存和实时数据存储。

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
│       ├── enums/                       # 枚举类
│       ├── model/                       # 通用模型
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
│       └── GatewayApplication.java
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

## 13. 安全设计

### 13.1 API 认证

- 使用 JWT Token 认证
- API Key + Secret 签名验证
- 接口限流和熔断

### 13.2 数据加密

- 数据库敏感字段加密（API Key/Secret）
- HTTPS 传输加密
- Redis 数据加密

### 13.3 访问控制

- 基于角色的权限控制（RBAC）
- IP 白名单限制
- 操作日志审计

## 14. 扩展性设计

### 14.1 新交易所接入

1. 实现 `ExchangeAdapter` 接口
2. 添加配置类
3. 在 Nacos 中注册配置
4. 重启服务自动加载

### 14.2 新策略类型接入

1. 创建策略执行器实现类
2. 注册到策略工厂
3. 在前端配置策略参数

### 14.3 新风控规则接入

1. 在 Drools 规则文件中添加新规则
2. 在枚举中添加规则类型
3. 实现规则评估逻辑

## 15. 测试策略

### 15.1 单元测试

- 核心业务逻辑测试
- 规则引擎测试
- 适配器接口测试

### 15.2 集成测试

- 服务间调用测试
- Kafka 消息传递测试
- 数据库操作测试

### 15.3 端到端测试

- 完整交易流程测试
- 策略执行流程测试
- 风控检查流程测试

## 16. 性能指标

| 指标 | 目标值 |
|------|--------|
| 策略执行延迟 | < 100ms |
| 风控检查延迟 | < 50ms |
| 订单执行延迟 | < 200ms |
| 市场数据处理 | 10000 msg/s |
| 服务并发量 | 10000 QPS |
| 消息队列吞吐量 | 100000 msg/s |