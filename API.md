# Smart Quantify Java - API 文档

## 1. 概述

本文档描述了 Smart Quantify Java 量化交易系统的完整 API 接口规范，包括市场数据、策略管理、风控管理、订单执行和回测服务。

**API 网关地址**: `http://localhost:8080`

**统一响应格式**:

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

---

## 2. 市场数据接口

### 2.1 获取K线数据

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

**实现代码**: [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java#L22-L34)

### 2.2 获取订单簿

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

**实现代码**: [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java#L36-L47)

### 2.3 获取Ticker

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

**实现代码**: [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java#L49-L64)

### 2.4 获取合约列表

```
GET /api/v1/market/instruments
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exchange | string | 是 | 交易所 |
| type | string | 否 | 合约类型：SPOT/FUTURES/OPTIONS |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "instruments": [
      {
        "symbol": "BTCUSDT",
        "type": "SPOT",
        "exchange": "BINANCE",
        "tickSize": 0.01,
        "lotSize": 0.00001,
        "minQuantity": 0.00001,
        "maxQuantity": 100,
        "minNotional": 10,
        "baseAsset": "BTC",
        "quoteAsset": "USDT",
        "enabled": true
      }
    ]
  }
}
```

**实现代码**: [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java#L66-L76)

### 2.5 订阅K线

```
POST /api/v1/market/subscribe/klines
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exchange | string | 是 | 交易所 |
| symbol | string | 是 | 交易对 |
| interval | string | 是 | 时间间隔 |

**实现代码**: [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java#L78-L88)

### 2.6 订阅订单簿

```
POST /api/v1/market/subscribe/orderbook
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exchange | string | 是 | 交易所 |
| symbol | string | 是 | 交易对 |

**实现代码**: [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java#L90-L98)

### 2.7 取消订阅

```
POST /api/v1/market/unsubscribe
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exchange | string | 是 | 交易所 |
| symbol | string | 是 | 交易对 |

**实现代码**: [MarketController.java](smart-quantify-market/src/main/java/com/smartquantify/market/controller/MarketController.java#L101-L110)

---

## 3. 策略管理接口

### 3.1 创建策略

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

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L20-L28)

### 3.2 获取策略详情

```
GET /api/v1/strategies/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 策略ID |

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L30-L38)

### 3.3 获取策略列表

```
GET /api/v1/strategies
```

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L40-L48)

### 3.4 启动策略

```
POST /api/v1/strategies/{id}/start
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 策略ID |

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L50-L58)

### 3.5 停止策略

```
POST /api/v1/strategies/{id}/stop
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 策略ID |

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L60-L68)

### 3.6 暂停策略

```
POST /api/v1/strategies/{id}/pause
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 策略ID |

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L70-L78)

### 3.7 更新策略

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

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L80-L88)

### 3.8 删除策略

```
DELETE /api/v1/strategies/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 策略ID |

**实现代码**: [StrategyController.java](smart-quantify-strategy/src/main/java/com/smartquantify/strategy/controller/StrategyController.java#L90-L97)

---

## 4. 风控管理接口

### 4.1 创建风控规则

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
      "scope": "strategy"
    }
  }
}
```

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L24-L32)

### 4.2 获取风控规则详情

```
GET /api/v1/risk/rules/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 规则ID |

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L34-L42)

### 4.3 获取风控规则列表

```
GET /api/v1/risk/rules
```

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L44-L52)

### 4.4 更新风控规则

```
PUT /api/v1/risk/rules/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 规则ID |

**请求体**:

```json
{
  "name": "最大仓位限制(更新)",
  "enabled": false
}
```

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L54-L62)

### 4.5 删除风控规则

```
DELETE /api/v1/risk/rules/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 规则ID |

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L64-L71)

### 4.6 风险检查

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
    "riskCheck": {
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
}
```

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L73-L81)

### 4.7 获取风险限额

```
GET /api/v1/risk/limits
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| scope | string | 否 | 范围：strategy/global |
| strategyId | string | 否 | 策略ID |
| symbol | string | 否 | 交易对 |
| exchange | string | 否 | 交易所 |

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L83-L95)

### 4.8 获取风险状态

```
GET /api/v1/risk/state
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exchange | string | 是 | 交易所 |
| strategyId | string | 是 | 策略ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "state": {
      "exchange": "BINANCE",
      "strategyId": "uuid-xxx",
      "currentPosition": 0.0,
      "currentDrawdown": 0.0,
      "ordersInLastMinute": 0,
      "dailyVolume": 0.0
    }
  }
}
```

**实现代码**: [RiskController.java](smart-quantify-risk/src/main/java/com/smartquantify/risk/controller/RiskController.java#L97-L107)

---

## 5. 订单执行接口

### 5.1 提交订单

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
  "clientOrderId": "my-order-001",
  "exchange": "BINANCE",
  "strategyId": "uuid-xxx"
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
      "price": 45000.0,
      "status": "FILLED",
      "filledQuantity": 0.1,
      "remainingQuantity": 0.0,
      "avgPrice": 45000.0,
      "exchange": "BINANCE",
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00",
      "strategyId": "uuid-xxx"
    }
  }
}
```

**实现代码**: [ExecutionController.java](smart-quantify-execution/src/main/java/com/smartquantify/execution/controller/ExecutionController.java#L20-L28)

### 5.2 获取订单详情

```
GET /api/v1/orders/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 订单ID |

**实现代码**: [ExecutionController.java](smart-quantify-execution/src/main/java/com/smartquantify/execution/controller/ExecutionController.java#L30-L38)

### 5.3 获取订单列表

```
GET /api/v1/orders
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 订单状态：NEW/FILLED/CANCELED/REJECTED |
| symbol | string | 否 | 交易对 |
| exchange | string | 否 | 交易所 |

**实现代码**: [ExecutionController.java](smart-quantify-execution/src/main/java/com/smartquantify/execution/controller/ExecutionController.java#L40-L51)

### 5.4 取消订单

```
DELETE /api/v1/orders/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 订单ID |

**实现代码**: [ExecutionController.java](smart-quantify-execution/src/main/java/com/smartquantify/execution/controller/ExecutionController.java#L53-L61)

### 5.5 同步订单

```
POST /api/v1/orders/sync
```

**实现代码**: [ExecutionController.java](smart-quantify-execution/src/main/java/com/smartquantify/execution/controller/ExecutionController.java#L63-L71)

---

## 6. 回测服务接口

### 6.1 创建回测任务

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
      "strategyName": "MA交叉策略",
      "symbol": "BTCUSDT",
      "interval": "1h",
      "status": "PENDING",
      "createdAt": "2024-01-01T00:00:00"
    }
  }
}
```

**实现代码**: [BacktestController.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/controller/BacktestController.java#L21-L29)

### 6.2 获取回测任务详情

```
GET /api/v1/backtests/{id}
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 回测任务ID |

**实现代码**: [BacktestController.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/controller/BacktestController.java#L31-L39)

### 6.3 获取回测任务列表

```
GET /api/v1/backtests
```

**实现代码**: [BacktestController.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/controller/BacktestController.java#L41-L49)

### 6.4 运行回测

```
POST /api/v1/backtests/{id}/run
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 回测任务ID |

**实现代码**: [BacktestController.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/controller/BacktestController.java#L51-L59)

### 6.5 取消回测

```
POST /api/v1/backtests/{id}/cancel
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 回测任务ID |

**实现代码**: [BacktestController.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/controller/BacktestController.java#L61-L69)

### 6.6 获取回测结果

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
      "losingTrades": 18,
      "avgProfit": 150.0,
      "avgLoss": 80.0,
      "profitFactor": 1.875,
      "trades": []
    }
  }
}
```

**实现代码**: [BacktestController.java](smart-quantify-backtest/src/main/java/com/smartquantify/backtest/controller/BacktestController.java#L71-L79)

---

## 7. gRPC 接口

### 7.1 策略执行

```protobuf
rpc Execute(ExecuteRequest) returns (ExecuteResponse);
```

**请求结构**:

```protobuf
message ExecuteRequest {
    string strategy_id = 1;
    KlineData kline = 2;
    map<string, string> parameters = 3;
}

message KlineData {
    string symbol = 1;
    string interval = 2;
    string open_time = 3;
    double open = 4;
    double high = 5;
    double low = 6;
    double close = 7;
    double volume = 8;
    double quote_volume = 9;
    string close_time = 10;
}
```

**响应结构**:

```protobuf
message ExecuteResponse {
    SignalData signal = 1;
}

message SignalData {
    string id = 1;
    string strategy_id = 2;
    string strategy_name = 3;
    string symbol = 4;
    string side = 5;
    string type = 6;
    double price = 7;
    double quantity = 8;
    string exchange = 9;
    string instrument_type = 10;
    int32 priority = 11;
    string created_at = 12;
    string status = 13;
    double stop_loss = 14;
    double take_profit = 15;
}
```

**实现代码**: [strategy.proto](smart-quantify-python/proto/strategy.proto)

### 7.2 回测执行

```protobuf
rpc Backtest(BacktestRequest) returns (BacktestResponse);
```

**请求结构**:

```protobuf
message BacktestRequest {
    string strategy_id = 1;
    string symbol = 2;
    string interval = 3;
    string start_time = 4;
    string end_time = 5;
    double initial_capital = 6;
    map<string, string> parameters = 7;
    repeated KlineData klines = 8;
}
```

**响应结构**:

```protobuf
message BacktestResponse {
    string task_id = 1;
    double initial_capital = 2;
    double final_capital = 3;
    double total_return = 4;
    double annualized_return = 5;
    double max_drawdown = 6;
    double sharpe_ratio = 7;
    double win_rate = 8;
    int32 total_trades = 9;
    int32 winning_trades = 10;
    int32 losing_trades = 11;
}
```

**实现代码**: [strategy.proto](smart-quantify-python/proto/strategy.proto)

---

## 8. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 9. 接口权限

| 模块 | 接口 | 需要认证 |
|------|------|----------|
| 市场数据 | /api/v1/market/** | 否 |
| 策略管理 | /api/v1/strategies/** | 是 |
| 风控管理 | /api/v1/risk/** | 是 |
| 订单执行 | /api/v1/orders/** | 是 |
| 回测服务 | /api/v1/backtests/** | 是 |