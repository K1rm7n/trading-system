package com.tradingsystem.scheduler;

import com.tradingsystem.properties.ApplicationProperties;
import com.tradingsystem.service.external.AlphaVantageService;
import com.tradingsystem.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Scheduler for updating market news and company news
 */
@Component
public class MarketNewsScheduler {
    private static final Logger logger = LoggerFactory.getLogger(MarketNewsScheduler.class);

    private final AlphaVantageService alphaVantageService;
    private final DateUtils dateUtils;
    private final ApplicationProperties applicationProperties;

    // Major market indices to track news
    private static final List<String> MARKET_INDICES = Arrays.asList(
            "SPY", // S&P 500 ETF
            "QQQ", // NASDAQ-100 ETF
            "DIA", // Dow Jones Industrial Average ETF
            "IWM"  // Russell 2000 ETF
    );

    // Major sectors to track sector-specific news
    private static final List<String> SECTORS = Arrays.asList(
            "XLK", // Technology Sector ETF
            "XLF", // Financial Sector ETF
            "XLE", // Energy Sector ETF
            "XLV", // Healthcare Sector ETF
            "XLP", // Consumer Staples Sector ETF
            "XLY", // Consumer Discretionary Sector ETF
            "XLI", // Industrial Sector ETF
            "XLB", // Materials Sector ETF
            "XLU"  // Utilities Sector ETF
    );

    public MarketNewsScheduler(
            AlphaVantageService alphaVantageService,
            DateUtils dateUtils,
            ApplicationProperties applicationProperties) {
        this.alphaVantageService = alphaVantageService;
        this.dateUtils = dateUtils;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Update general market news every morning before market open
     * Scheduled to run at 8:00 AM ET on weekdays
     */
    @Scheduled(cron = "0 0 8 * * MON-FRI", zone = "America/New_York")
    public void scheduleMarketNewsUpdate() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping market news update.");
            return;
        }

        logger.info("Starting scheduled market news update");

        try {
            LocalDate today = LocalDate.now();
            if (!dateUtils.isTradingDay(today)) {
                logger.info("Today is not a trading day. Skipping market news update.");
                return;
            }

            // Update general market news
            alphaVantageService.updateMarketNews();

            // Update news for major indices
            for (String index : MARKET_INDICES) {
                alphaVantageService.updateSymbolNews(index);
                Thread.sleep(6000); // 6 seconds delay between API calls
            }

            logger.info("Scheduled market news update completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled market news update", e);
        }
    }

    /**
     * Update sector-specific news once daily
     * Scheduled to run at 7:30 AM ET on weekdays
     */
    @Scheduled(cron = "0 30 7 * * MON-FRI", zone = "America/New_York")
    public void scheduleSectorNewsUpdate() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping sector news update.");
            return;
        }

        logger.info("Starting scheduled sector news update");

        try {
            LocalDate today = LocalDate.now();
            if (!dateUtils.isTradingDay(today)) {
                logger.info("Today is not a trading day. Skipping sector news update.");
                return;
            }

            // Update news for major sector ETFs
            for (String sector : SECTORS) {
                alphaVantageService.updateSymbolNews(sector);
                Thread.sleep(6000); // 6 seconds delay between API calls
            }

            logger.info("Scheduled sector news update completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled sector news update", e);
        }
    }

    /**
     * Update news for watchlist stocks
     * Scheduled to run at 8:30 AM ET on weekdays
     */
    @Scheduled(cron = "0 30 8 * * MON-FRI", zone = "America/New_York")
    public void scheduleWatchlistNewsUpdate() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping watchlist news update.");
            return;
        }

        logger.info("Starting scheduled watchlist stocks news update");

        try {
            LocalDate today = LocalDate.now();
            if (!dateUtils.isTradingDay(today)) {
                logger.info("Today is not a trading day. Skipping watchlist news update.");
                return;
            }

            // Get list of stocks that are in users' watchlists
            List<String> watchlistSymbols = alphaVantageService.getWatchlistSymbols();

            if (watchlistSymbols.isEmpty()) {
                logger.info("No watchlist stocks found for news update");
                return;
            }

            logger.info("Found {} watchlist stocks for news update", watchlistSymbols.size());

            // Update news for each watchlist stock
            for (String symbol : watchlistSymbols) {
                try {
                    alphaVantageService.updateSymbolNews(symbol);
                    Thread.sleep(6000); // 6 seconds delay between API calls
                } catch (Exception e) {
                    logger.error("Error updating news for stock {}: {}", symbol, e.getMessage());
                }
            }

            logger.info("Scheduled watchlist news update completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled watchlist news update", e);
        }
    }

    /**
     * Update breaking market news during trading hours
     * Scheduled to run every hour from 9:00 AM to 4:00 PM ET on weekdays
     */
    @Scheduled(cron = "0 0 9-16 * * MON-FRI", zone = "America/New_York")
    public void scheduleBreakingNewsUpdate() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping breaking news update.");
            return;
        }

        // Skip if market is closed
        if (!dateUtils.isMarketOpen() && LocalDate.now().getHour() != 9) {
            // Allow 9 AM update even if market isn't technically open yet
            return;
        }

        logger.info("Starting scheduled breaking news update");

        try {
            // Update general market news for breaking news
            alphaVantageService.updateMarketNews();

            logger.info("Scheduled breaking news update completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled breaking news update", e);
        }
    }

    /**
     * Manual trigger to update news for a specific symbol
     * Can be called from API endpoints or admin controls
     *
     * @param symbol stock symbol to update news for
     * @return true if update was successful
     */
    public boolean triggerManualNewsUpdate(String symbol) {
        logger.info("Manually triggered news update for symbol: {}", symbol);

        try {
            alphaVantageService.updateSymbolNews(symbol);
            logger.info("Manual news update completed successfully for symbol: {}", symbol);
            return true;
        } catch (Exception e) {
            logger.error("Error during manual news update for symbol {}: {}", symbol, e.getMessage());
            return false;
        }
    }
}
