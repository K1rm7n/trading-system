package com.tradingsystem.scheduler;

import com.tradingsystem.model.entity.Stock;
import com.tradingsystem.properties.ApplicationProperties;
import com.tradingsystem.repository.StockRepository;
import com.tradingsystem.service.external.AlphaVantageService;
import com.tradingsystem.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler for regularly updating stock data from external API
 */
@Component
public class StockDataUpdateScheduler {
    private static final Logger logger = LoggerFactory.getLogger(StockDataUpdateScheduler.class);

    private final StockRepository stockRepository;
    private final AlphaVantageService alphaVantageService;
    private final DateUtils dateUtils;
    private final ApplicationProperties applicationProperties;

    public StockDataUpdateScheduler(
            StockRepository stockRepository,
            AlphaVantageService alphaVantageService,
            DateUtils dateUtils,
            ApplicationProperties applicationProperties) {
        this.stockRepository = stockRepository;
        this.alphaVantageService = alphaVantageService;
        this.dateUtils = dateUtils;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Update stock data daily after market close
     * Scheduled to run at 5:30 PM ET on weekdays
     */
    @Scheduled(cron = "0 30 17 * * MON-FRI", zone = "America/New_York")
    public void scheduleDailyStockUpdate() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping daily stock update.");
            return;
        }

        logger.info("Starting scheduled daily stock data update");

        try {
            List<Stock> stocks = stockRepository.findAll();

            if (stocks.isEmpty()) {
                logger.warn("No stocks found in database for update");
                return;
            }

            logger.info("Found {} stocks to update", stocks.size());

            // Check if market was open today
            LocalDate today = LocalDate.now();
            if (!dateUtils.isTradingDay(today)) {
                logger.info("Today is not a trading day. Skipping update.");
                return;
            }

            updateStocksWithRateLimit(stocks);

            logger.info("Scheduled daily stock update completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled stock update", e);
        }
    }

    /**
     * Update watchlist stocks more frequently during trading hours
     * Scheduled to run every 15 minutes from 9:30 AM to 4:00 PM ET on weekdays
     */
    @Scheduled(cron = "0 */15 9-16 * * MON-FRI", zone = "America/New_York")
    public void scheduleWatchlistUpdate() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping watchlist update.");
            return;
        }

        // Skip if outside market hours (after 9:30 AM and before 4:00 PM)
        if (!dateUtils.isMarketOpen()) {
            return;
        }

        logger.info("Starting scheduled watchlist stock data update");

        try {
            // Get only stocks that are in users' watchlists (frequently viewed)
            List<Stock> watchlistStocks = stockRepository.findStocksInWatchlists();

            if (watchlistStocks.isEmpty()) {
                logger.info("No watchlist stocks found for update");
                return;
            }

            logger.info("Found {} watchlist stocks to update", watchlistStocks.size());

            updateStocksWithRateLimit(watchlistStocks);

            logger.info("Scheduled watchlist update completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled watchlist update", e);
        }
    }

    /**
     * Update the most active stocks more frequently
     * Scheduled to run every 5 minutes during trading hours
     */
    @Scheduled(cron = "0 */5 9-16 * * MON-FRI", zone = "America/New_York")
    public void scheduleActiveStocksUpdate() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping active stocks update.");
            return;
        }

        // Skip if outside market hours
        if (!dateUtils.isMarketOpen()) {
            return;
        }

        logger.info("Starting scheduled update for most active stocks");

        try {
            // Get top 10 most active stocks (by trading volume or user activity)
            List<Stock> activeStocks = stockRepository.findTopActiveStocks(10);

            if (activeStocks.isEmpty()) {
                logger.info("No active stocks found for update");
                return;
            }

            logger.info("Found {} active stocks to update", activeStocks.size());

            // Higher priority update, use shorter delay between API calls
            updateStocksWithRateLimit(activeStocks, 6000); // 6 seconds between requests

            logger.info("Scheduled active stocks update completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled active stocks update", e);
        }
    }

    /**
     * Refresh all stock data during weekend (more extensive update)
     * Scheduled to run at 10:00 AM ET on Saturday
     */
    @Scheduled(cron = "0 0 10 * * SAT", zone = "America/New_York")
    public void scheduleWeekendFullUpdate() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping weekend full update.");
            return;
        }

        logger.info("Starting weekend full stock data update");

        try {
            List<Stock> allStocks = stockRepository.findAll();

            if (allStocks.isEmpty()) {
                logger.warn("No stocks found in database for weekend update");
                return;
            }

            logger.info("Found {} stocks for weekend update", allStocks.size());

            // Update all stocks with full historical data
            for (Stock stock : allStocks) {
                try {
                    // Get extended historical data (up to 2 years)
                    alphaVantageService.updateStockHistoricalData(stock.getSymbol(), "full");

                    // Also update fundamental data during weekend
                    alphaVantageService.updateStockFundamentalData(stock.getSymbol());

                    // Rate limiting - slower but more comprehensive updates
                    TimeUnit.SECONDS.sleep(15); // 15 seconds between requests for weekend batch
                } catch (Exception e) {
                    logger.error("Error updating stock {}: {}", stock.getSymbol(), e.getMessage());
                }
            }

            logger.info("Weekend full stock update completed successfully");
        } catch (Exception e) {
            logger.error("Error during weekend full stock update", e);
        }
    }

    /**
     * Update stocks with rate limiting to avoid API throttling
     *
     * @param stocks list of stocks to update
     */
    private void updateStocksWithRateLimit(List<Stock> stocks) {
        updateStocksWithRateLimit(stocks, 12000); // Default 12 seconds between requests
    }

    /**
     * Update stocks with custom rate limiting
     *
     * @param stocks list of stocks to update
     * @param delayMillis delay between API calls in milliseconds
     */
    private void updateStocksWithRateLimit(List<Stock> stocks, long delayMillis) {
        int count = 0;

        for (Stock stock : stocks) {
            try {
                // Update intraday data during market hours, or daily data after market
                if (dateUtils.isMarketOpen()) {
                    alphaVantageService.updateStockIntradayData(stock.getSymbol());
                } else {
                    alphaVantageService.updateStockDailyData(stock.getSymbol());
                }

                count++;
                logger.debug("Updated stock {}/{}: {}", count, stocks.size(), stock.getSymbol());

                // Rate limiting to avoid API throttling
                if (count < stocks.size()) {
                    TimeUnit.MILLISECONDS.sleep(delayMillis);
                }
            } catch (Exception e) {
                logger.error("Error updating stock {}: {}", stock.getSymbol(), e.getMessage());
            }
        }
    }

    /**
     * Manual trigger to update specific stock data
     * Can be called from API endpoints or admin controls
     *
     * @param symbol stock symbol to update
     * @return true if update was successful
     */
    public boolean triggerManualUpdate(String symbol) {
        logger.info("Manually triggered update for stock: {}", symbol);

        try {
            // Update daily and intraday data
            alphaVantageService.updateStockDailyData(symbol);

            if (dateUtils.isMarketOpen()) {
                alphaVantageService.updateStockIntradayData(symbol);
            }

            logger.info("Manual update completed successfully for stock: {}", symbol);
            return true;
        } catch (Exception e) {
            logger.error("Error during manual update for stock {}: {}", symbol, e.getMessage());
            return false;
        }
    }
}
