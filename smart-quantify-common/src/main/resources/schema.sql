CREATE DATABASE IF NOT EXISTS nacos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smart_quantify DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE nacos;

CREATE TABLE IF NOT EXISTS config_info (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    data_id VARCHAR(255) NOT NULL,
    group_id VARCHAR(128) DEFAULT NULL,
    content LONGTEXT NOT NULL,
    md5 VARCHAR(32) DEFAULT NULL,
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    src_user TEXT,
    src_ip VARCHAR(50) DEFAULT NULL,
    app_name VARCHAR(128) DEFAULT NULL,
    tenant_id VARCHAR(128) DEFAULT '',
    c_desc VARCHAR(256) DEFAULT NULL,
    c_use VARCHAR(64) DEFAULT NULL,
    effect VARCHAR(64) DEFAULT NULL,
    type VARCHAR(64) DEFAULT NULL,
    c_schema TEXT,
    encrypted_data_key TEXT NOT NULL DEFAULT '',
    INDEX uk_configinfo_datagrouptenant (data_id, group_id, tenant_id)
);

CREATE TABLE IF NOT EXISTS config_info_beta (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    data_id VARCHAR(255) NOT NULL,
    group_id VARCHAR(128) DEFAULT NULL,
    content LONGTEXT NOT NULL,
    md5 VARCHAR(32) DEFAULT NULL,
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    src_user TEXT,
    src_ip VARCHAR(50) DEFAULT NULL,
    app_name VARCHAR(128) DEFAULT NULL,
    tenant_id VARCHAR(128) DEFAULT '',
    c_desc VARCHAR(256) DEFAULT NULL,
    c_use VARCHAR(64) DEFAULT NULL,
    effect VARCHAR(64) DEFAULT NULL,
    type VARCHAR(64) DEFAULT NULL,
    c_schema TEXT,
    encrypted_data_key TEXT NOT NULL DEFAULT '',
    INDEX uk_configinfobeta_datagrouptenant (data_id, group_id, tenant_id)
);

CREATE TABLE IF NOT EXISTS config_info_tag (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    data_id VARCHAR(255) NOT NULL,
    group_id VARCHAR(128) DEFAULT NULL,
    tenant_id VARCHAR(128) DEFAULT '',
    tag_id VARCHAR(128) NOT NULL,
    app_name VARCHAR(128) DEFAULT NULL,
    content LONGTEXT NOT NULL,
    md5 VARCHAR(32) DEFAULT NULL,
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    src_user TEXT,
    src_ip VARCHAR(50) DEFAULT NULL,
    INDEX uk_configinfotag_datagrouptenanttag (data_id, group_id, tenant_id, tag_id)
);

CREATE TABLE IF NOT EXISTS config_info_aggr (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    data_id VARCHAR(255) NOT NULL,
    group_id VARCHAR(128) DEFAULT NULL,
    tenant_id VARCHAR(128) DEFAULT '',
    datatype VARCHAR(255) DEFAULT NULL,
    content LONGTEXT NOT NULL,
    gmt_modified DATETIME NOT NULL,
    INDEX uk_configinfoaggr_datagrouptenant (data_id, group_id, tenant_id)
);

CREATE TABLE IF NOT EXISTS config_history (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    data_id VARCHAR(255) NOT NULL,
    group_id VARCHAR(128) DEFAULT NULL,
    tenant_id VARCHAR(128) DEFAULT '',
    app_name VARCHAR(128) DEFAULT NULL,
    content LONGTEXT NOT NULL,
    md5 VARCHAR(32) DEFAULT NULL,
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    src_user TEXT,
    src_ip VARCHAR(50) DEFAULT NULL,
    operation VARCHAR(10) DEFAULT NULL,
    INDEX uk_confighistory_datagrouptenant (data_id, group_id, tenant_id)
);

CREATE TABLE IF NOT EXISTS his_config_info (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    data_id VARCHAR(255) NOT NULL,
    group_id VARCHAR(128) DEFAULT NULL,
    tenant_id VARCHAR(128) DEFAULT '',
    app_name VARCHAR(128) DEFAULT NULL,
    content LONGTEXT NOT NULL,
    md5 VARCHAR(32) DEFAULT NULL,
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    src_user TEXT,
    src_ip VARCHAR(50) DEFAULT NULL,
    INDEX uk_hisconfiginfo_datagrouptenant (data_id, group_id, tenant_id)
);

CREATE TABLE IF NOT EXISTS tenant_info (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    tenant_name VARCHAR(128) DEFAULT NULL,
    tenant_desc VARCHAR(256) DEFAULT NULL,
    create_source VARCHAR(32) DEFAULT NULL,
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX uk_tenant_info_tenant_id (tenant_id)
);

CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS roles (
    username VARCHAR(50) NOT NULL,
    role VARCHAR(50) NOT NULL,
    INDEX idx_roles_username (username)
);

CREATE TABLE IF NOT EXISTS permissions (
    role VARCHAR(50) NOT NULL,
    resource VARCHAR(255) NOT NULL,
    action VARCHAR(8) NOT NULL,
    INDEX uk_permissions_role_action (role, resource, action)
);

CREATE TABLE IF NOT EXISTS app_config (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    app_name VARCHAR(128) NOT NULL,
    namespace VARCHAR(128) DEFAULT '',
    config TEXT,
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX uk_app_config_app_namespace (app_name, namespace)
);

USE smart_quantify;

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
    `interval` VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    last_run_time TIMESTAMP,
    statistics TEXT,
    version VARCHAR(20) NOT NULL,
    INDEX idx_strategy_status (status),
    INDEX idx_strategy_exchange (exchange),
    INDEX idx_strategy_type (type)
);

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
    strategy_id VARCHAR(36),
    INDEX idx_order_status (status),
    INDEX idx_order_symbol (symbol),
    INDEX idx_order_exchange (exchange),
    INDEX idx_order_strategy_id (strategy_id)
);

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
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_risk_rule_enabled (enabled),
    INDEX idx_risk_rule_type (type),
    INDEX idx_risk_rule_scope (scope)
);

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
    max_daily_volume DECIMAL(20,8),
    INDEX idx_risk_limit_scope (scope),
    INDEX idx_risk_limit_strategy_id (strategy_id)
);

CREATE TABLE IF NOT EXISTS backtest_task (
    id VARCHAR(36) PRIMARY KEY,
    strategy_id VARCHAR(36) NOT NULL,
    strategy_name VARCHAR(100) NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    `interval` VARCHAR(10) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    initial_capital DECIMAL(20,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    parameters TEXT,
    result TEXT,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    error_message TEXT,
    INDEX idx_backtest_status (status),
    INDEX idx_backtest_strategy_id (strategy_id)
);

CREATE TABLE IF NOT EXISTS `signal` (
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
    take_profit DECIMAL(20,8),
    INDEX idx_signal_strategy_id (strategy_id),
    INDEX idx_signal_status (status),
    INDEX idx_signal_symbol (symbol)
);