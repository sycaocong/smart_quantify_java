# Smart Quantify Java - 企业级量化交易系统

Smart Quantify Java 是一套基于 Spring Boot 和 Spring Cloud 构建的企业级量化交易系统，采用微服务架构，支持策略管理、执行引擎、风险管理、市场数据和回测等核心功能。

## ✨ 核心特性

### 策略管理
- **策略创建**: 支持多种策略类型的创建和配置
- **策略生命周期**: 启动、停止、暂停、恢复等完整生命周期管理
- **策略监控**: 实时监控策略执行状态和绩效指标
- **多语言支持**: 支持 Java 和 Python 策略开发

### 执行引擎
- **多交易所支持**: Binance、OKX、Bybit、Huobi 等主流交易所
- **订单管理**: 订单的创建、修改、取消
- **实时状态更新**: 通过消息队列实时更新订单状态
- **执行报告**: 完整的执行报告生成

### 风险管理
- **实时监控**: 实时监控账户风险状态
- **仓位限制**: 单笔和累计仓位限制
- **熔断机制**: 使用 Resilience4j 实现熔断保护
- **风险规则**: 可配置的风险规则引擎

### 市场数据
- **实时行情**: 实时获取市场行情数据
- **订单簿**: 实时订单簿数据
- **K线数据**: 多种时间周期的 K线数据
- **数据缓存**: 本地缓存 + Redis 分布式缓存

### 回测系统
- **异步回测**: 支持异步回测任务执行
- **绩效指标**: 完整的回测绩效指标计算
- **报告生成**: 详细的回测报告生成
- **历史数据**: 支持从多种数据源获取历史数据

### 高可用架构
- **服务注册发现**: 使用 Nacos 进行服务注册和发现
- **API 网关**: Spring Cloud Gateway 作为统一入口
- **分布式锁**: Redis + Etcd 分布式锁实现
- **消息队列**: Kafka 异步消息处理

## 🏗️ 架构设计

```
┌─────────────────────────────────────────────────────────────────────┐
│                         API 网关                                    │
│                  Spring Cloud Gateway                               │
│  - 请求路由 | - 限流 | - 熔断 | - 认证鉴权 | - 负载均衡              │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  服务注册中心 (Nacos)                                │
│  - 服务注册 | - 服务发现 | - 配置管理                               │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        微服务层                                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                   │
│  │   Strategy  │ │  Execution  │ │    Risk     │                   │
│  │  (策略服务)  │ │  (执行服务)  │ │  (风控服务)  │                   │
│  └─────────────┘ └─────────────┘ └─────────────┘                   │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                   │
│  │    Market   │ │  Backtest   │ │   Adapter   │                   │
│  │  (行情服务)  │ │  (回测服务)  │ │  (适配层)    │                   │
│  └─────────────┘ └─────────────┘ └─────────────┘                   │
│  ┌─────────────┐ ┌─────────────┐                                   │
│  │   Common    │ │   Python    │                                   │
│  │  (公共模块)  │ │  (Python侧)  │                                   │
│  └─────────────┘ └─────────────┘                                   │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        消息队列层                                   │
│                           Kafka                                     │
│  Topics: order-created, order-executed, signal-generated           │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          存储层                                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐ ┌──────────────┐        │
│  │  MySQL   │ │  Redis   │ │ ClickHouse   │ │Elasticsearch │        │
│  │(业务数据) │ │(缓存/锁) │ │ (时序数据)   │ │ (日志搜索)   │        │
│  └──────────┘ └──────────┘ └──────────────┘ └──────────────┘        │
└─────────────────────────────────────────────────────────────────────┘
```

## 📁 项目结构

```
smart_quantify_java/
├── smart-quantify-gateway/        # API 网关
│   ├── src/main/java/com/smartquantify/gateway/
│   │   ├── GatewayApplication.java
│   │   └── config/RateLimitConfig.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── bootstrap.yml
│   ├── Dockerfile
│   └── pom.xml
├── smart-quantify-strategy/       # 策略服务
│   ├── src/main/java/com/smartquantify/strategy/
│   │   ├── StrategyApplication.java
│   │   ├── controller/StrategyController.java
│   │   ├── service/StrategyService.java
│   │   ├── repository/StrategyRepository.java
│   │   ├── entity/Strategy.java
│   │   └── dto/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── bootstrap.yml
│   ├── Dockerfile
│   └── pom.xml
├── smart-quantify-risk/           # 风控服务
│   ├── src/main/java/com/smartquantify/risk/
│   │   ├── RiskApplication.java
│   │   ├── controller/RiskController.java
│   │   ├── service/RiskService.java
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── bootstrap.yml
│   ├── Dockerfile
│   └── pom.xml
├── smart-quantify-execution/      # 执行服务
│   ├── src/main/java/com/smartquantify/execution/
│   │   ├── ExecutionApplication.java
│   │   ├── controller/ExecutionController.java
│   │   ├── service/ExecutionService.java
│   │   ├── consumer/OrderExecutionConsumer.java
│   │   ├── repository/
│   │   └── entity/
│   ├── src/main/resources/application.yml
│   ├── Dockerfile
│   └── pom.xml
├── smart-quantify-market/         # 行情服务
│   ├── src/main/java/com/smartquantify/market/
│   │   ├── MarketApplication.java
│   │   ├── controller/MarketController.java
│   │   └── service/MarketService.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── bootstrap.yml
│   ├── Dockerfile
│   └── pom.xml
├── smart-quantify-backtest/       # 回测服务
│   ├── src/main/java/com/smartquantify/backtest/
│   │   ├── BacktestApplication.java
│   │   ├── controller/BacktestController.java
│   │   ├── service/BacktestService.java
│   │   ├── repository/BacktestTaskRepository.java
│   │   ├── entity/BacktestTask.java
│   │   └── dto/
│   ├── src/main/resources/application.yml
│   ├── Dockerfile
│   └── pom.xml
├── smart-quantify-adapter/        # 交易所适配器
│   ├── src/main/java/com/smartquantify/adapter/
│   │   ├── ExchangeAdapter.java
│   │   ├── AdapterFactory.java
│   │   ├── binance/BinanceAdapter.java
│   │   ├── okx/OkxAdapter.java
│   │   ├── bybit/BybitAdapter.java
│   │   └── huobi/HuobiAdapter.java
│   └── pom.xml
├── smart-quantify-common/         # 公共模块
│   ├── src/main/java/com/smartquantify/common/
│   │   ├── config/                # 配置类
│   │   ├── exception/             # 异常处理
│   │   ├── model/                 # 数据模型
│   │   ├── enums/                 # 枚举类型
│   │   ├── event/                 # 事件定义
│   │   ├── lock/                  # 分布式锁
│   │   ├── aspect/                # AOP 切面
│   │   ├── annotation/            # 自定义注解
│   │   └── util/                  # 工具类
│   ├── src/main/resources/
│   │   ├── schema.sql
│   │   └── nacos-mysql.properties
│   └── pom.xml
├── smart-quantify-api/            # API 定义
│   └── pom.xml
├── smart-quantify-python/         # Python 模块
│   ├── app/main.py
│   ├── proto/strategy.proto
│   └── requirements.txt
├── docker-compose.yml             # Docker Compose 配置
├── settings.xml                   # Maven 镜像配置
├── pom.xml                        # 父 POM
├── DESIGN.md                      # 设计文档
├── API.md                         # API 文档
└── .gitignore                     # Git 忽略文件
```

## 🛠️ 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 3.2.x |
| 微服务 | Spring Cloud | 2023.0.x |
| 服务发现 | Nacos | 2.3.x |
| API 网关 | Spring Cloud Gateway | 4.1.x |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis | 7.0+ |
| 缓存 | Caffeine | 3.1.x |
| 消息队列 | Kafka | 3.5+ |
| 时序数据库 | ClickHouse | 23.0+ |
| ORM | Spring Data JPA | 3.2.x |
| 熔断 | Resilience4j | 2.1.x |
| API 文档 | SpringDoc OpenAPI | 2.3.x |
| 构建工具 | Maven | 3.9+ |
| 容器 | Docker | 24.0+ |

## 🚀 快速开始

### 环境要求

- **JDK**: 21+
- **Maven**: 3.9+
- **Docker**: 24.0+
- **Docker Compose**: 2.20+

### 使用 Docker 启动

```bash
# 进入项目目录
cd smart_quantify_java

# 启动所有服务（包括依赖服务）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看特定服务日志
docker-compose logs -f smart-quantify-gateway
```

### 本地开发

首先启动依赖服务：

```bash
# 启动 MySQL、Redis、Nacos、Kafka、ClickHouse
docker-compose up -d mysql redis nacos kafka zookeeper clickhouse
```

然后启动各个微服务：

```bash
# 启动策略服务
mvn spring-boot:run -pl smart-quantify-strategy -am

# 启动风控服务
mvn spring-boot:run -pl smart-quantify-risk -am

# 启动执行服务
mvn spring-boot:run -pl smart-quantify-execution -am

# 启动行情服务
mvn spring-boot:run -pl smart-quantify-market -am

# 启动回测服务
mvn spring-boot:run -pl smart-quantify-backtest -am

# 启动 API 网关
mvn spring-boot:run -pl smart-quantify-gateway -am
```

### 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| API 网关 | 8080 | 统一入口 |
| 策略服务 | 8081 | 策略管理 |
| 风控服务 | 8082 | 风险控制 |
| 执行服务 | 8083 | 订单执行 |
| 行情服务 | 8084 | 市场数据 |
| 回测服务 | 8085 | 回测任务 |
| Nacos | 8848 | 服务注册发现 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Kafka | 9092 | 消息队列 |
| ClickHouse | 8123 | 时序数据库 |

### API 文档

启动后访问 Swagger UI:
- 网关文档: http://localhost:8080/swagger-ui.html
- 策略服务: http://localhost:8081/swagger-ui.html
- 风控服务: http://localhost:8082/swagger-ui.html
- 执行服务: http://localhost:8083/swagger-ui.html
- 行情服务: http://localhost:8084/swagger-ui.html
- 回测服务: http://localhost:8085/swagger-ui.html

### 配置说明

每个服务都有 `application.yml` 配置文件，支持环境变量覆盖：

```yaml
# 数据源配置
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/smart_quantify}
    username: ${SPRING_DATASOURCE_USERNAME:root}
    password: ${SPRING_DATASOURCE_PASSWORD:password}

# Nacos 配置
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}

# Redis 配置
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

# Kafka 配置
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

## 🧪 测试

```bash
# 运行所有测试
mvn test

# 运行特定模块测试
mvn test -pl smart-quantify-strategy
mvn test -pl smart-quantify-risk

# 生成测试覆盖率报告
mvn jacoco:report

# 运行集成测试
mvn integration-test
```

## 🔧 API 接口

### 策略接口

```bash
# 创建策略
POST /api/v1/strategies
Content-Type: application/json

{
  "name": "MA 交叉策略",
  "type": "MA_CROSS",
  "symbol": "BTC/USDT",
  "exchange": "BINANCE",
  "parameters": {
    "fastPeriod": 50,
    "slowPeriod": 200
  },
  "initialCapital": 10000
}

# 获取策略列表
GET /api/v1/strategies

# 获取策略详情
GET /api/v1/strategies/{strategyId}

# 启动策略
POST /api/v1/strategies/{strategyId}/start

# 停止策略
POST /api/v1/strategies/{strategyId}/stop

# 删除策略
DELETE /api/v1/strategies/{strategyId}
```

### 订单接口

```bash
# 创建订单
POST /api/v1/orders
Content-Type: application/json

{
  "symbol": "BTC/USDT",
  "side": "BUY",
  "type": "MARKET",
  "quantity": 0.1,
  "exchange": "BINANCE",
  "strategyId": "strat-001"
}

# 获取订单状态
GET /api/v1/orders/{orderId}

# 取消订单
DELETE /api/v1/orders/{orderId}

# 获取当前订单
GET /api/v1/orders/open?symbol=BTC/USDT

# 获取订单历史
GET /api/v1/orders/history?page=0&size=20
```

### 风控接口

```bash
# 创建风险规则
POST /api/v1/risk/rules
Content-Type: application/json

{
  "name": "最大持仓限制",
  "type": "POSITION_LIMIT",
  "symbol": "BTC/USDT",
  "maxPosition": 10,
  "enabled": true
}

# 获取风险规则
GET /api/v1/risk/rules

# 风险检查
POST /api/v1/risk/check
Content-Type: application/json

{
  "symbol": "BTC/USDT",
  "side": "BUY",
  "quantity": 1,
  "price": 45000
}
```

### 行情接口

```bash
# 获取 K线数据
GET /api/v1/market/klines?symbol=BTC/USDT&interval=1h&limit=100

# 获取订单簿
GET /api/v1/market/orderbook?symbol=BTC/USDT&limit=10

# 获取 Ticker
GET /api/v1/market/ticker?symbol=BTC/USDT

# 获取所有 Ticker
GET /api/v1/market/tickers

# 获取交易对列表
GET /api/v1/market/instruments?exchange=BINANCE
```

### 回测接口

```bash
# 创建回测任务
POST /api/v1/backtest
Content-Type: application/json

{
  "strategyId": "strat-001",
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "initialCapital": 10000,
  "feeRate": 0.001
}

# 获取回测任务列表
GET /api/v1/backtest

# 获取回测结果
GET /api/v1/backtest/{taskId}

# 取消回测任务
DELETE /api/v1/backtest/{taskId}
```

## 📊 核心模块

### API 网关

API 网关是系统的统一入口，负责：

- **请求路由**: 将请求路由到对应的微服务
- **限流控制**: 使用 Redis 实现分布式限流
- **熔断保护**: 使用 Resilience4j 实现熔断
- **认证鉴权**: 统一的认证和授权处理
- **负载均衡**: 基于 Nacos 的负载均衡

### 策略服务

策略服务负责策略的全生命周期管理：

- **策略创建**: 创建新策略并配置参数
- **策略执行**: 实时执行策略并生成交易信号
- **策略监控**: 监控策略执行状态和绩效
- **策略回测**: 在历史数据上测试策略效果

### 执行服务

执行服务负责订单的实际执行：

- **订单创建**: 根据交易信号创建订单
- **订单管理**: 跟踪订单状态和执行结果
- **消息消费**: 消费 Kafka 中的订单创建事件
- **交易所适配**: 通过适配器连接不同交易所

### 风控服务

风控服务负责风险控制：

- **风险规则管理**: 创建和管理风险规则
- **实时风险检查**: 在下单前检查风险限制
- **仓位监控**: 监控和限制持仓规模
- **熔断保护**: 对交易所连接进行熔断

### 行情服务

行情服务负责市场数据的获取和分发：

- **数据获取**: 从交易所获取实时行情
- **数据缓存**: 使用 Caffeine 和 Redis 缓存数据
- **数据分发**: 通过 Kafka 分发行情数据
- **数据查询**: 提供行情数据查询接口

### 回测服务

回测服务负责策略回测：

- **回测任务管理**: 创建和管理回测任务
- **异步执行**: 使用 @Async 异步执行回测
- **绩效计算**: 计算回测绩效指标
- **报告生成**: 生成详细的回测报告

## 📖 文档

- [设计文档](DESIGN.md) - 系统架构和设计细节
- [API 文档](API.md) - REST API 接口说明

## 📝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE.txt](LICENSE.txt)

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 微信图片_20260630212706_17_2.jpg
- 发送邮件至 494919536@qq.com