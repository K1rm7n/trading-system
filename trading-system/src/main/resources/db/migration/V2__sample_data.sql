-- Демонстрационные данные для Intelligent Investment Decision Support System
-- Версия: 1.0
-- Дата: 2025-04-29

-- Добавление тестового пользователя
-- Пароль: password123 (с использованием BCrypt)
INSERT INTO users (username, email, password, first_name, last_name, is_active, role)
VALUES
    ('admin', 'admin@example.com', '$2a$10$9tWICcFCizF/r9JGp8zH2uaK9zBTRj5aGoNVJ8TUgKZF1mYsIl7Gu', 'Admin', 'User', true, 'ADMIN'),
    ('user', 'user@example.com', '$2a$10$9tWICcFCizF/r9JGp8zH2uaK9zBTRj5aGoNVJ8TUgKZF1mYsIl7Gu', 'John', 'Doe', true, 'USER'),
    ('demo', 'demo@example.com', '$2a$10$9tWICcFCizF/r9JGp8zH2uaK9zBTRj5aGoNVJ8TUgKZF1mYsIl7Gu', 'Demo', 'User', true, 'USER')
ON CONFLICT (username) DO NOTHING;

-- Добавление некоторых популярных акций
INSERT INTO stocks (symbol, name, exchange, sector, industry, current_price, price_change, price_change_percent, market_cap, volume, average_volume, fifty_two_week_high, fifty_two_week_low)
VALUES
    ('AAPL', 'Apple Inc.', 'NASDAQ', 'Technology', 'Consumer Electronics', 192.53, 1.25, 0.65, 2978000000000, 42680000, 54870000, 199.62, 141.32),
    ('MSFT', 'Microsoft Corporation', 'NASDAQ', 'Technology', 'Software-Infrastructure', 412.65, -2.35, -0.57, 3070000000000, 20150000, 24520000, 430.82, 275.37),
    ('GOOGL', 'Alphabet Inc.', 'NASDAQ', 'Communication Services', 'Internet Content & Information', 160.79, 0.92, 0.58, 1990000000000, 18690000, 20780000, 164.74, 102.63),
    ('AMZN', 'Amazon.com, Inc.', 'NASDAQ', 'Consumer Cyclical', 'Internet Retail', 175.95, 0.48, 0.27, 1830000000000, 27150000, 37970000, 186.57, 101.15),
    ('META', 'Meta Platforms, Inc.', 'NASDAQ', 'Communication Services', 'Internet Content & Information', 480.39, 5.23, 1.10, 1210000000000, 12540000, 14680000, 531.49, 167.67),
    ('TSLA', 'Tesla, Inc.', 'NASDAQ', 'Consumer Cyclical', 'Auto Manufacturers', 194.05, -3.25, -1.65, 618000000000, 81250000, 95680000, 278.98, 138.80),
    ('JPM', 'JPMorgan Chase & Co.', 'NYSE', 'Financial Services', 'Banks-Diversified', 193.82, 1.05, 0.54, 558000000000, 8420000, 9310000, 200.94, 135.19),
    ('JNJ', 'Johnson & Johnson', 'NYSE', 'Healthcare', 'Drug Manufacturers-General', 148.89, -0.37, -0.25, 358000000000, 6580000, 7390000, 175.97, 144.95),
    ('V', 'Visa Inc.', 'NYSE', 'Financial Services', 'Credit Services', 278.57, 2.16, 0.78, 564000000000, 5740000, 7280000, 290.96, 216.14),
    ('PG', 'The Procter & Gamble Company', 'NYSE', 'Consumer Defensive', 'Household & Personal Products', 165.73, 0.86, 0.52, 390000000000, 5120000, 6350000, 168.89, 141.45)
ON CONFLICT (symbol) DO NOTHING;

-- Добавление рыночных индексов
INSERT INTO market_indices (symbol, name, current_value, change_value, change_percent)
VALUES
    ('SPX', 'S&P 500', 5069.35, 15.29, 0.30),
    ('DJI', 'Dow Jones Industrial Average', 38239.98, 95.74, 0.25),
    ('IXIC', 'NASDAQ Composite', 15927.90, 25.38, 0.16),
    ('RUT', 'Russell 2000', 2020.95, -5.75, -0.28),
    ('VIX', 'CBOE Volatility Index', 14.75, -0.35, -2.32)
ON CONFLICT (symbol) DO NOTHING;

-- Создание тестового портфеля для демо-пользователя
INSERT INTO portfolios (user_id, name, description, total_value, cash_balance)
VALUES
    ((SELECT id FROM users WHERE username = 'demo'), 'My Tech Portfolio', 'Portfolio focused on technology stocks', 15250.75, 2500.00),
    ((SELECT id FROM users WHERE username = 'demo'), 'Dividend Portfolio', 'Portfolio focused on dividend stocks', 8750.25, 1000.00)
ON CONFLICT (user_id, name) DO NOTHING;

-- Добавление акций в тестовый портфель
INSERT INTO holdings (portfolio_id, stock_id, quantity, average_price, current_price, current_value, profit_loss, profit_loss_percent)
VALUES
    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     (SELECT id FROM stocks WHERE symbol = 'AAPL'), 15, 175.25, 192.53, 2887.95, 259.20, 9.86),

    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     (SELECT id FROM stocks WHERE symbol = 'MSFT'), 10, 380.50, 412.65, 4126.50, 321.50, 8.45),

    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     (SELECT id FROM stocks WHERE symbol = 'GOOGL'), 12, 145.25, 160.79, 1929.48, 186.48, 10.70),

    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     (SELECT id FROM stocks WHERE symbol = 'AMZN'), 20, 168.75, 175.95, 3519.00, 144.00, 4.27),

    ((SELECT id FROM portfolios WHERE name = 'Dividend Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     (SELECT id FROM stocks WHERE symbol = 'JNJ'), 15, 145.50, 148.89, 2233.35, 50.85, 2.33),

    ((SELECT id FROM portfolios WHERE name = 'Dividend Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     (SELECT id FROM stocks WHERE symbol = 'PG'), 20, 160.25, 165.73, 3314.60, 109.60, 3.42),

    ((SELECT id FROM portfolios WHERE name = 'Dividend Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     (SELECT id FROM stocks WHERE symbol = 'JPM'), 10, 185.75, 193.82, 1938.20, 80.70, 4.34)
ON CONFLICT (portfolio_id, stock_id) DO NOTHING;

-- Добавление торговых операций в портфель
INSERT INTO trades (portfolio_id, stock_id, type, quantity, price, total_amount, commission, trade_date, notes)
VALUES
    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     (SELECT id FROM stocks WHERE symbol = 'AAPL'), 'BUY', 10, 170.25, 1702.50, 4.99, CURRENT_TIMESTAMP - INTERVAL '60 days', 'Initial position'),

    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     (SELECT id FROM stocks WHERE symbol = 'AAPL'), 'BUY', 5, 185.25, 926.25, 4.99, CURRENT_TIMESTAMP - INTERVAL '30 days', 'Adding to position after earnings'),

    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     (SELECT id FROM stocks WHERE symbol = 'MSFT'), 'BUY', 10, 380.50, 3805.00, 4.99, CURRENT_TIMESTAMP - INTERVAL '45 days', 'Long-term investment'),

    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     (SELECT id FROM stocks WHERE symbol = 'GOOGL'), 'BUY', 15, 142.25, 2133.75, 4.99, CURRENT_TIMESTAMP - INTERVAL '90 days', 'Initial position'),

    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     (SELECT id FROM stocks WHERE symbol = 'GOOGL'), 'SELL', 3, 158.75, 476.25, 4.99, CURRENT_TIMESTAMP - INTERVAL '15 days', 'Taking some profits'),

    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     (SELECT id FROM stocks WHERE symbol = 'AMZN'), 'BUY', 20, 168.75, 3375.00, 4.99, CURRENT_TIMESTAMP - INTERVAL '75 days', 'Buying the dip');

-- Добавление элементов в список отслеживания для демо-пользователя
INSERT INTO watchlist_items (user_id, stock_id, notes, alert_price)
VALUES
    ((SELECT id FROM users WHERE username = 'demo'),
     (SELECT id FROM stocks WHERE symbol = 'TSLA'), 'Waiting for a good entry point', 180.00),

    ((SELECT id FROM users WHERE username = 'demo'),
     (SELECT id FROM stocks WHERE symbol = 'META'), 'Interested in AI developments', 450.00),

    ((SELECT id FROM users WHERE username = 'demo'),
     (SELECT id FROM stocks WHERE symbol = 'V'), 'Strong dividend stock', 260.00);

-- Добавление показателей производительности портфеля
INSERT INTO portfolio_performance (portfolio_id, date, value, daily_return, weekly_return, monthly_return, yearly_return, total_return)
VALUES
    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     CURRENT_DATE - INTERVAL '7 days', 14750.25, 0.25, 1.25, 3.45, 12.75, 18.35),

    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     CURRENT_DATE - INTERVAL '6 days', 14820.50, 0.48, 1.42, 3.65, 13.05, 18.92),

    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     CURRENT_DATE - INTERVAL '5 days', 14905.75, 0.58, 1.68, 3.85, 13.35, 19.68),

    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     CURRENT_DATE - INTERVAL '4 days', 14855.25, -0.34, 1.25, 3.65, 13.15, 19.28),

    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     CURRENT_DATE - INTERVAL '3 days', 14925.50, 0.47, 1.45, 3.85, 13.45, 19.84),

    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     CURRENT_DATE - INTERVAL '2 days', 15125.25, 1.34, 2.25, 4.35, 14.25, 21.48),

    ((SELECT id FROM portfolios WHERE name = 'My Tech Portfolio' AND user_id = (SELECT id FROM users WHERE username = 'demo')),
     CURRENT_DATE - INTERVAL '1 day', 15250.75, 0.83, 3.12, 4.75, 14.55, 22.54);

-- Добавление некоторых технических аналитических данных
INSERT INTO technical_indicators (stock_id, calculation_date, rsi_14, macd, macd_signal, macd_histogram, sma_20, sma_50, sma_200, ema_12, ema_26, bollinger_upper, bollinger_middle, bollinger_lower)
VALUES
    ((SELECT id FROM stocks WHERE symbol = 'AAPL'), CURRENT_DATE, 62.45, 2.35, 1.25, 1.10, 188.75, 182.50, 175.25, 190.25, 185.75, 198.50, 188.75, 179.00),
    ((SELECT id FROM stocks WHERE symbol = 'MSFT'), CURRENT_DATE, 58.75, 5.25, 4.75, 0.50, 405.50, 395.25, 375.50, 410.25, 400.75, 425.25, 405.50, 385.75),
    ((SELECT id FROM stocks WHERE symbol = 'GOOGL'), CURRENT_DATE, 65.25, 3.50, 2.75, 0.75, 158.25, 152.50, 145.75, 159.50, 155.25, 165.75, 158.25, 150.75),
    ((SELECT id FROM stocks WHERE symbol = 'AMZN'), CURRENT_DATE, 55.50, 2.25, 1.85, 0.40, 172.50, 165.25, 155.50, 174.25, 170.75, 180.25, 172.50, 164.75);

-- Добавление примеров анализа акций
INSERT INTO analyses (stock_id, analysis_date, analysis_type, recommendation, confidence, summary, details, target_price, time_horizon, created_by)
VALUES
    ((SELECT id FROM stocks WHERE symbol = 'AAPL'), CURRENT_TIMESTAMP - INTERVAL '1 day', 'TECHNICAL', 'BUY', 75.50,
     'Apple shows strong technical signals with price above all major moving averages.',
     'RSI at 62.45 indicates bullish momentum without being overbought. MACD is positive and the stock is trading above its 20, 50, and 200-day moving averages, suggesting a strong uptrend. Bollinger Bands show potential for continued upside with the price closer to the upper band.',
     215.00, 'MEDIUM_TERM', 'SYSTEM'),

    ((SELECT id FROM stocks WHERE symbol = 'MSFT'), CURRENT_TIMESTAMP - INTERVAL '2 days', 'FUNDAMENTAL', 'HOLD', 65.25,
     'Microsoft continues to show strong fundamentals but valuation is somewhat stretched.',
     'Strong revenue growth from cloud services and productivity software. Azure growth remains impressive at over 30% YoY. However, current P/E ratio is above historical average, suggesting limited near-term upside. Long-term outlook remains positive due to AI initiatives and cloud dominance.',
     430.00, 'LONG_TERM', 'SYSTEM'),

    ((SELECT id FROM stocks WHERE symbol = 'GOOGL'), CURRENT_TIMESTAMP - INTERVAL '3 days', 'AI', 'BUY', 82.75,
     'Alphabet positioned well for AI-driven growth with strong core business.',
     'Google\'s core search business provides stable cash flow while the company invests heavily in AI research. YouTube ad revenue continues to grow impressively. Cloud business showing improved profitability. Valuation remains reasonable compared to other tech giants, providing good risk/reward ratio.',
     185.00, 'LONG_TERM', 'SYSTEM'),

    ((SELECT id FROM stocks WHERE symbol = 'TSLA'), CURRENT_TIMESTAMP - INTERVAL '4 days', 'TREND', 'SELL', 68.50,
     'Tesla shows weakening price momentum and faces increased competition.',
     'Price trends have turned negative with the stock trading below key moving averages. Volume on down days exceeds volume on up days, indicating selling pressure. Relative strength compared to the overall market has deteriorated. Increasing competition in the EV space may pressure margins.',
     165.00, 'SHORT_TERM', 'SYSTEM');

-- Добавление некоторых новостей о акциях
INSERT INTO stock_news (stock_id, title, source, news_url, summary, sentiment, publish_date)
VALUES
    ((SELECT id FROM stocks WHERE symbol = 'AAPL'),
     'Apple Reports Record Services Revenue in Latest Quarter',
     'Financial Times',
     'https://example.com/apple-news-1',
     'Apple\'s services segment, which includes App Store and Apple Music, reached an all-time high, offsetting slower iPhone growth.',
     'POSITIVE',
     CURRENT_TIMESTAMP - INTERVAL '5 days'),

    ((SELECT id FROM stocks WHERE symbol = 'AAPL'),
     'Apple Announces New MacBook Pro with Enhanced AI Capabilities',
     'TechCrunch',
     'https://example.com/apple-news-2',
     'The new MacBook Pro models feature next-generation chips specifically designed for artificial intelligence and machine learning tasks.',
     'POSITIVE',
     CURRENT_TIMESTAMP - INTERVAL '3 days'),

    ((SELECT id FROM stocks WHERE symbol = 'MSFT'),
     'Microsoft Cloud Revenue Surges, Exceeding Analyst Expectations',
     'Wall Street Journal',
     'https://example.com/microsoft-news-1',
     'Azure cloud services grew 35% year-over-year, driving Microsoft\'s overall revenue to beat market forecasts.',
     'POSITIVE',
     CURRENT_TIMESTAMP - INTERVAL '6 days'),

    ((SELECT id FROM stocks WHERE symbol = 'GOOGL'),
     'Google Faces New Antitrust Probe in European Union',
     'Reuters',
     'https://example.com/google-news-1',
     'European regulators announced a new investigation into Google\'s advertising technology practices, potentially leading to additional fines.',
     'NEGATIVE',
     CURRENT_TIMESTAMP - INTERVAL '2 days'),

    ((SELECT id FROM stocks WHERE symbol = 'TSLA'),
     'Tesla Delays Cybertruck Production Amid Supply Chain Challenges',
     'Bloomberg',
     'https://example.com/tesla-news-1',
     'The highly anticipated Cybertruck faces additional production delays as Tesla grapples with component shortages and manufacturing complexities.',
     'NEGATIVE',
     CURRENT_TIMESTAMP - INTERVAL '4 days'),

    (NULL,
     'Federal Reserve Signals Potential Rate Cut Later This Year',
     'CNBC',
     'https://example.com/market-news-1',
     'The Federal Reserve chair indicated that the central bank may begin reducing interest rates if inflation continues to moderate, potentially boosting equity markets.',
     'POSITIVE',
     CURRENT_TIMESTAMP - INTERVAL '1 day');

-- Добавление фундаментальных данных для некоторых акций
INSERT INTO stock_fundamentals (stock_id, eps, pe_ratio, forward_pe, dividend_yield, peg_ratio, price_to_book, price_to_sales, debt_to_equity, revenue, revenue_growth, profit_margin, beta, fifty_day_ma, two_hundred_day_ma)
VALUES
    ((SELECT id FROM stocks WHERE symbol = 'AAPL'),
     6.35, 30.32, 28.75, 0.51, 2.45, 35.75, 7.85, 1.25, 394500000000, 0.0675, 0.2485, 1.27, 188.35, 175.65),

    ((SELECT id FROM stocks WHERE symbol = 'MSFT'),
     11.25, 36.68, 33.25, 0.72, 2.15, 13.85, 12.05, 0.45, 211500000000, 0.1525, 0.3675, 0.95, 405.25, 375.85),

    ((SELECT id FROM stocks WHERE symbol = 'GOOGL'),
     5.80, 27.72, 24.50, 0.50, 1.85, 6.25, 5.75, 0.25, 307500000000, 0.1275, 0.2585, 1.15, 155.75, 142.35),

    ((SELECT id FROM stocks WHERE symbol = 'AMZN'),
     3.25, 54.14, 42.50, 0.00, 2.75, 9.85, 2.65, 0.55, 535000000000, 0.1175, 0.0685, 1.32, 171.25, 158.45),

    ((SELECT id FROM stocks WHERE symbol = 'JNJ'),
     9.75, 15.27, 14.85, 3.05, 3.25, 5.75, 4.25, 0.45, 85500000000, 0.0425, 0.2225, 0.65, 146.85, 150.25);