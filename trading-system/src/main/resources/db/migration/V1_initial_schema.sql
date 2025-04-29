-- Начальная схема базы данных для Intelligent Investment Decision Support System
-- Версия: 1.0
-- Дата: 2025-04-29

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    role VARCHAR(20) DEFAULT 'USER',
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Таблица для акций
CREATE TABLE IF NOT EXISTS stocks (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    exchange VARCHAR(50),
    sector VARCHAR(100),
    industry VARCHAR(100),
    current_price DECIMAL(19, 4),
    price_change DECIMAL(19, 4),
    price_change_percent DECIMAL(8, 4),
    market_cap DECIMAL(19, 2),
    volume BIGINT,
    average_volume BIGINT,
    fifty_two_week_high DECIMAL(19, 4),
    fifty_two_week_low DECIMAL(19, 4),
    pe_ratio DECIMAL(10, 2),
    dividend_yield DECIMAL(7, 4),
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индекс для быстрого поиска акций
CREATE INDEX IF NOT EXISTS idx_stocks_symbol ON stocks(symbol);

-- Таблица для исторических данных акций
CREATE TABLE IF NOT EXISTS stock_historical_data (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    date DATE NOT NULL,
    open_price DECIMAL(19, 4) NOT NULL,
    high_price DECIMAL(19, 4) NOT NULL,
    low_price DECIMAL(19, 4) NOT NULL,
    close_price DECIMAL(19, 4) NOT NULL,
    adjusted_close DECIMAL(19, 4) NOT NULL,
    volume BIGINT NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE,
    CONSTRAINT unique_stock_date UNIQUE (stock_id, date)
);

-- Индексы для исторических данных
CREATE INDEX IF NOT EXISTS idx_historical_stock_id ON stock_historical_data(stock_id);
CREATE INDEX IF NOT EXISTS idx_historical_date ON stock_historical_data(date);

-- Таблица для интрадей данных (внутридневных)
CREATE TABLE IF NOT EXISTS stock_intraday_data (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    datetime TIMESTAMP NOT NULL,
    open_price DECIMAL(19, 4) NOT NULL,
    high_price DECIMAL(19, 4) NOT NULL,
    low_price DECIMAL(19, 4) NOT NULL,
    close_price DECIMAL(19, 4) NOT NULL,
    volume BIGINT NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE,
    CONSTRAINT unique_stock_datetime UNIQUE (stock_id, datetime)
);

-- Индексы для интрадей данных
CREATE INDEX IF NOT EXISTS idx_intraday_stock_id ON stock_intraday_data(stock_id);
CREATE INDEX IF NOT EXISTS idx_intraday_datetime ON stock_intraday_data(datetime);

-- Таблица для портфелей
CREATE TABLE IF NOT EXISTS portfolios (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    total_value DECIMAL(19, 2) DEFAULT 0,
    cash_balance DECIMAL(19, 2) DEFAULT 0,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_portfolio_name_per_user UNIQUE (user_id, name)
);

-- Индекс для портфелей пользователя
CREATE INDEX IF NOT EXISTS idx_portfolio_user_id ON portfolios(user_id);

-- Таблица для позиций в портфеле (holdings)
CREATE TABLE IF NOT EXISTS holdings (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    quantity DECIMAL(19, 6) NOT NULL,
    average_price DECIMAL(19, 4) NOT NULL,
    current_price DECIMAL(19, 4),
    current_value DECIMAL(19, 2),
    profit_loss DECIMAL(19, 2),
    profit_loss_percent DECIMAL(8, 4),
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    FOREIGN KEY (stock_id) REFERENCES stocks(id),
    CONSTRAINT unique_stock_per_portfolio UNIQUE (portfolio_id, stock_id)
);

-- Индексы для позиций
CREATE INDEX IF NOT EXISTS idx_holding_portfolio_id ON holdings(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_holding_stock_id ON holdings(stock_id);

-- Таблица для торговых операций
CREATE TABLE IF NOT EXISTS trades (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL, -- BUY, SELL
    quantity DECIMAL(19, 6) NOT NULL,
    price DECIMAL(19, 4) NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    commission DECIMAL(19, 2) DEFAULT 0,
    trade_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    FOREIGN KEY (stock_id) REFERENCES stocks(id)
);

-- Индексы для торговых операций
CREATE INDEX IF NOT EXISTS idx_trade_portfolio_id ON trades(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_trade_stock_id ON trades(stock_id);
CREATE INDEX IF NOT EXISTS idx_trade_date ON trades(trade_date);

-- Таблица для показателей производительности портфеля
CREATE TABLE IF NOT EXISTS portfolio_performance (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    date DATE NOT NULL,
    value DECIMAL(19, 2) NOT NULL,
    daily_return DECIMAL(8, 4),
    weekly_return DECIMAL(8, 4),
    monthly_return DECIMAL(8, 4),
    yearly_return DECIMAL(8, 4),
    total_return DECIMAL(8, 4),
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    CONSTRAINT unique_portfolio_date UNIQUE (portfolio_id, date)
);

-- Индексы для показателей производительности
CREATE INDEX IF NOT EXISTS idx_performance_portfolio_id ON portfolio_performance(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_performance_date ON portfolio_performance(date);

-- Таблица для списка отслеживаемых акций (watchlist)
CREATE TABLE IF NOT EXISTS watchlist_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    notes TEXT,
    alert_price DECIMAL(19, 4),
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (stock_id) REFERENCES stocks(id),
    CONSTRAINT unique_watchlist_item UNIQUE (user_id, stock_id)
);

-- Индексы для списка отслеживаемых акций
CREATE INDEX IF NOT EXISTS idx_watchlist_user_id ON watchlist_items(user_id);
CREATE INDEX IF NOT EXISTS idx_watchlist_stock_id ON watchlist_items(stock_id);

-- Таблица для анализов акций
CREATE TABLE IF NOT EXISTS analyses (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    analysis_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    analysis_type VARCHAR(50) NOT NULL, -- TECHNICAL, FUNDAMENTAL, AI, TREND
    recommendation VARCHAR(20), -- BUY, SELL, HOLD
    confidence DECIMAL(5, 2), -- 0-100%
    summary TEXT NOT NULL,
    details TEXT,
    target_price DECIMAL(19, 4),
    time_horizon VARCHAR(20), -- SHORT_TERM, MEDIUM_TERM, LONG_TERM
    created_by VARCHAR(50), -- SYSTEM, или имя аналитика
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE
);

-- Индексы для анализов
CREATE INDEX IF NOT EXISTS idx_analysis_stock_id ON analyses(stock_id);
CREATE INDEX IF NOT EXISTS idx_analysis_date ON analyses(analysis_date);
CREATE INDEX IF NOT EXISTS idx_analysis_type ON analyses(analysis_type);

-- Таблица для новостей о акциях
CREATE TABLE IF NOT EXISTS stock_news (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT,
    title VARCHAR(255) NOT NULL,
    source VARCHAR(100),
    news_url VARCHAR(500),
    image_url VARCHAR(500),
    summary TEXT,
    sentiment VARCHAR(20), -- POSITIVE, NEGATIVE, NEUTRAL
    publish_date TIMESTAMP,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE
);

-- Индексы для новостей
CREATE INDEX IF NOT EXISTS idx_news_stock_id ON stock_news(stock_id);
CREATE INDEX IF NOT EXISTS idx_news_publish_date ON stock_news(publish_date);
CREATE INDEX IF NOT EXISTS idx_news_sentiment ON stock_news(sentiment);

-- Таблица для рыночных индексов
CREATE TABLE IF NOT EXISTS market_indices (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    current_value DECIMAL(19, 4),
    change_value DECIMAL(19, 4),
    change_percent DECIMAL(8, 4),
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Таблица для фундаментальных данных акций
CREATE TABLE IF NOT EXISTS stock_fundamentals (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    eps DECIMAL(10, 4),
    pe_ratio DECIMAL(10, 2),
    forward_pe DECIMAL(10, 2),
    dividend_yield DECIMAL(7, 4),
    peg_ratio DECIMAL(10, 2),
    price_to_book DECIMAL(10, 2),
    price_to_sales DECIMAL(10, 2),
    debt_to_equity DECIMAL(10, 2),
    revenue DECIMAL(19, 2),
    revenue_growth DECIMAL(8, 4),
    profit_margin DECIMAL(8, 4),
    beta DECIMAL(6, 4),
    fifty_day_ma DECIMAL(19, 4),
    two_hundred_day_ma DECIMAL(19, 4),
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE,
    CONSTRAINT unique_stock_fundamentals UNIQUE (stock_id)
);

-- Индекс для фундаментальных данных
CREATE INDEX IF NOT EXISTS idx_fundamentals_stock_id ON stock_fundamentals(stock_id);

-- Таблица для технических индикаторов
CREATE TABLE IF NOT EXISTS technical_indicators (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    calculation_date DATE NOT NULL,
    rsi_14 DECIMAL(7, 4),
    macd DECIMAL(10, 6),
    macd_signal DECIMAL(10, 6),
    macd_histogram DECIMAL(10, 6),
    sma_20 DECIMAL(19, 4),
    sma_50 DECIMAL(19, 4),
    sma_200 DECIMAL(19, 4),
    ema_12 DECIMAL(19, 4),
    ema_26 DECIMAL(19, 4),
    bollinger_upper DECIMAL(19, 4),
    bollinger_middle DECIMAL(19, 4),
    bollinger_lower DECIMAL(19, 4),
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE,
    CONSTRAINT unique_stock_date_indicators UNIQUE (stock_id, calculation_date)
);

-- Индексы для технических индикаторов
CREATE INDEX IF NOT EXISTS idx_indicators_stock_id ON technical_indicators(stock_id);
CREATE INDEX IF NOT EXISTS idx_indicators_date ON technical_indicators(calculation_date);