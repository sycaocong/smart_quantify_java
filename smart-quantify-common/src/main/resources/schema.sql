CREATE TABLE IF NOT EXISTS strategy (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(50) NOT NULL,
    language VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    parameters TEXT,
    config TEXT,
    exchange VARCHAR(20) NOT NULL,
    symbols TEXT,
    interval VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    last_run_time TIMESTAMP,
    statistics TEXT,
    version VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_strategy_status ON strategy(status);
CREATE INDEX IF NOT EXISTS idx_strategy_exchange ON strategy(exchange);
CREATE INDEX IF NOT EXISTS idx_strategy_type ON strategy(type);

CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) PRIMARY KEY,
    symbol VARCHAR(50) NOT NULL,
    side VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL,
    quantity DECIMAL(20,8) NOT NULL,
    price DECIMAL(20,8),
    status VARCHAR(20) NOT NULL,
    filled_quantity DECIMAL(20,8) NOT NULL,
    remaining_quantity DECIMAL(20,8) NOT NULL,
    avg_price DECIMAL(20,8),
    exchange VARCHAR(20) NOT NULL,
    client_order_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    strategy_id VARCHAR(36)
);

CREATE INDEX IF NOT EXISTS idx_order_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_symbol ON orders(symbol);
CREATE INDEX IF NOT EXISTS idx_order_exchange ON orders(exchange);
CREATE INDEX IF NOT EXISTS idx_order_strategy_id ON orders(strategy_id);

CREATE TABLE IF NOT EXISTS risk_rule (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL,
    priority INT NOT NULL,
    conditions TEXT,
    actions TEXT,
    scope VARCHAR(20) NOT NULL,
    strategy_ids TEXT,
    symbols TEXT,
    exchanges TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_risk_rule_enabled ON risk_rule(enabled);
CREATE INDEX IF NOT EXISTS idx_risk_rule_type ON risk_rule(type);
CREATE INDEX IF NOT EXISTS idx_risk_rule_scope ON risk_rule(scope);

CREATE TABLE IF NOT EXISTS risk_limit (
    id VARCHAR(36) PRIMARY KEY,
    scope VARCHAR(20) NOT NULL,
    strategy_id VARCHAR(36),
    symbol VARCHAR(50),
    exchange VARCHAR(20),
    max_position DECIMAL(20,8),
    max_drawdown DECIMAL(10,4),
    max_orders_per_minute INT,
    max_trade_size DECIMAL(20,8),
    max_daily_volume DECIMAL(20,8)
);

CREATE INDEX IF NOT EXISTS idx_risk_limit_scope ON risk_limit(scope);
CREATE INDEX IF NOT EXISTS idx_risk_limit_strategy_id ON risk_limit(strategy_id);

CREATE TABLE IF NOT EXISTS backtest_task (
    id VARCHAR(36) PRIMARY KEY,
    strategy_id VARCHAR(36) NOT NULL,
    strategy_name VARCHAR(100) NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    interval VARCHAR(10) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    initial_capital DECIMAL(20,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    parameters TEXT,
    result TEXT,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    error_message TEXT
);

CREATE INDEX IF NOT EXISTS idx_backtest_status ON backtest_task(status);
CREATE INDEX IF NOT EXISTS idx_backtest_strategy_id ON backtest_task(strategy_id);

CREATE TABLE IF NOT EXISTS signal (
    id VARCHAR(36) PRIMARY KEY,
    strategy_id VARCHAR(36) NOT NULL,
    strategy_name VARCHAR(100) NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    side VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL,
    price DECIMAL(20,8),
    quantity DECIMAL(20,8),
    exchange VARCHAR(20) NOT NULL,
    instrument_type VARCHAR(20),
    priority INT,
    created_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    stop_loss DECIMAL(20,8),
    take_profit DECIMAL(20,8)
);

CREATE INDEX IF NOT EXISTS idx_signal_strategy_id ON signal(strategy_id);
CREATE INDEX IF NOT EXISTS idx_signal_status ON signal(status);
CREATE INDEX IF NOT EXISTS idx_signal_symbol ON signal(symbol);