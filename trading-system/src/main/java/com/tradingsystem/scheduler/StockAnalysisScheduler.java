package com.tradingsystem.scheduler;

import com.tradingsystem.model.entity.Stock;
import com.tradingsystem.properties.ApplicationProperties;
import com.tradingsystem.repository.StockRepository;
import com.tradingsystem.service.external.ChatGPTService;
import com.tradingsystem.service.interfaces.AnalysisService;
import com.tradingsystem.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler for analyzing stocks and generating investment recommendations
 */
@Component
public class StockAnalysisScheduler {
    private static final Logger logger = LoggerFactory.getLogger(StockAnalysisScheduler.class);

    private final StockRepository stockRepository;
    private final AnalysisService analysisService;
    private final ChatGPTService chatGPTService;
    private final DateUtils dateUtils;
    private final ApplicationProperties applicationProperties;

    public StockAnalysisScheduler(
            StockRepository stockRepository,
            AnalysisService analysisService,
            ChatGPTService chatGPTService,
            DateUtils dateUtils,
            ApplicationProperties applicationProperties) {
        this.stockRepository = stockRepository;
        this.analysisService = analysisService;
        this.chatGPTService = chatGPTService;
        this.dateUtils = dateUtils;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Generate daily technical analysis for all stocks at the end of the trading day
     * Scheduled to run at 6:00 PM ET on weekdays
     */
    @Scheduled(cron = "0 0 18 * * MON-FRI", zone = "America/New_York")
    public void scheduleDailyTechnicalAnalysis() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping daily technical analysis.");
            return;
        }

        logger.info("Starting scheduled daily technical analysis for all stocks");

        try {
            LocalDate today = LocalDate.now();
            if (!dateUtils.isTradingDay(today)) {
                logger.info("Today is not a trading day. Skipping technical analysis.");
                return;
            }

            List<Stock> stocks = stockRepository.findAll();

            if (stocks.isEmpty()) {
                logger.warn("No stocks found in database for technical analysis");
                return;
            }

            logger.info("Found {} stocks for technical analysis", stocks.size());

            int count = 0;
            for (Stock stock : stocks) {
                try {
                    // Generate technical analysis
                    analysisService.generateTechnicalAnalysis(stock.getSymbol());

                    count++;
                    logger.debug("Generated technical analysis {}/{}: {}",
                            count, stocks.size(), stock.getSymbol());

                    // Small delay to avoid overwhelming the system
                    if (count < stocks.size()) {
                        TimeUnit.MILLISECONDS.sleep(500);
                    }
                } catch (Exception e) {
                    logger.error("Error generating technical analysis for stock {}: {}",
                            stock.getSymbol(), e.getMessage());
                }
            }

            logger.info("Scheduled daily technical analysis completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled daily technical analysis", e);
        }
    }

    /**
     * Generate AI-powered stock recommendations daily
     * Scheduled to run at 7:00 PM ET on weekdays
     */
    @Scheduled(cron = "0 0 19 * * MON-FRI", zone = "America/New_York")
    public void scheduleDailyAIRecommendations() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping daily AI recommendations.");
            return;
        }

        logger.info("Starting scheduled daily AI recommendations for stocks");

        try {
            LocalDate today = LocalDate.now();
            if (!dateUtils.isTradingDay(today)) {
                logger.info("Today is not a trading day. Skipping AI recommendations.");
                return;
            }

            // Get most active or popular stocks for more focused analysis
            List<Stock> popularStocks = stockRepository.findTopActiveStocks(30);

            if (popularStocks.isEmpty()) {
                logger.warn("No popular stocks found for AI recommendations");
                return;
            }

            logger.info("Found {} popular stocks for AI recommendations", popularStocks.size());

            int count = 0;
            for (Stock stock : popularStocks) {
                try {
                    // Generate AI recommendations
                    chatGPTService.generateStockRecommendation(stock.getSymbol());

                    count++;
                    logger.debug("Generated AI recommendation {}/{}: {}",
                            count, popularStocks.size(), stock.getSymbol());

                    // Larger delay for AI API calls
                    if (count < popularStocks.size()) {
                        TimeUnit.SECONDS.sleep(3);
                    }
                } catch (Exception e) {
                    logger.error("Error generating AI recommendation for stock {}: {}",
                            stock.getSymbol(), e.getMessage());
                }
            }

            logger.info("Scheduled daily AI recommendations completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled daily AI recommendations", e);
        }
    }

    /**
     * Generate market sentiment analysis
     * Scheduled to run at 6:30 PM ET on weekdays
     */
    @Scheduled(cron = "0 30 18 * * MON-FRI", zone = "America/New_York")
    public void scheduleMarketSentimentAnalysis() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping market sentiment analysis.");
            return;
        }

        logger.info("Starting scheduled market sentiment analysis");

        try {
            LocalDate today = LocalDate.now();
            if (!dateUtils.isTradingDay(today)) {
                logger.info("Today is not a trading day. Skipping market sentiment analysis.");
                return;
            }

            // Generate overall market sentiment analysis
            analysisService.generateMarketSentimentAnalysis();

            // Generate sentiment analysis for major sectors
            String[] sectors = {"Technology", "Financial", "Healthcare", "Energy", "Consumer"};
            for (String sector : sectors) {
                analysisService.generateSectorSentimentAnalysis(sector);
                TimeUnit.SECONDS.sleep(2);
            }

            logger.info("Scheduled market sentiment analysis completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled market sentiment analysis", e);
        }
    }

    /**
     * Generate weekly market outlook and recommendations
     * Scheduled to run at 9:00 AM ET on Mondays
     */
    @Scheduled(cron = "0 0 9 * * MON", zone = "America/New_York")
    public void scheduleWeeklyMarketOutlook() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping weekly market outlook.");
            return;
        }

        logger.info("Starting scheduled weekly market outlook");

        try {
            LocalDate today = LocalDate.now();
            if (!dateUtils.isTradingDay(today)) {
                logger.info("Today is not a trading day. Skipping weekly market outlook.");
                return;
            }

            // Generate weekly market outlook
            chatGPTService.generateWeeklyMarketOutlook();

            // Generate major index forecasts
            String[] indices = {"S&P 500", "NASDAQ", "Dow Jones", "Russell 2000"};
            for (String index : indices) {
                chatGPTService.generateIndexForecast(index);
                TimeUnit.SECONDS.sleep(5);
            }

            logger.info("Scheduled weekly market outlook completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled weekly market outlook", e);
        }
    }

    /**
     * Update trend analysis for watchlist stocks
     * Scheduled to run at 8:00 AM ET on weekdays
     */
    @Scheduled(cron = "0 0 8 * * MON-FRI", zone = "America/New_York")
    public void scheduleWatchlistTrendAnalysis() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping watchlist trend analysis.");
            return;
        }

        logger.info("Starting scheduled watchlist trend analysis");

        try {
            LocalDate today = LocalDate.now();
            if (!dateUtils.isTradingDay(today)) {
                logger.info("Today is not a trading day. Skipping watchlist trend analysis.");
                return;
            }

            // Get stocks in watchlists
            List<Stock> watchlistStocks = stockRepository.findStocksInWatchlists();

            if (watchlistStocks.isEmpty()) {
                logger.info("No watchlist stocks found for trend analysis");
                return;
            }

            logger.info("Found {} watchlist stocks for trend analysis", watchlistStocks.size());

            for (Stock stock : watchlistStocks) {
                try {
                    // Generate trend analysis
                    analysisService.generateTrendAnalysis(stock.getSymbol());

                    // Small delay
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (Exception e) {
                    logger.error("Error generating trend analysis for stock {}: {}",
                            stock.getSymbol(), e.getMessage());
                }
            }

            logger.info("Scheduled watchlist trend analysis completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled watchlist trend analysis", e);
        }
    }

    /**
     * Generate monthly investment themes and ideas
     * Scheduled to run at 10:00 AM ET on the first day of each month
     */
    @Scheduled(cron = "0 0 10 1 * ?", zone = "America/New_York")
    public void scheduleMonthlyInvestmentIdeas() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping monthly investment ideas.");
            return;
        }

        logger.info("Starting scheduled monthly investment ideas generation");

        try {
            // Only run if it's a trading day
            LocalDate today = LocalDate.now();
            if (!dateUtils.isTradingDay(today)) {
                logger.info("Today is not a trading day. Will run on next trading day.");
                // Reschedule for next trading day
                return;
            }

            // Generate monthly investment themes
            chatGPTService.generateMonthlyInvestmentThemes();

            // Generate long-term stock picks
            chatGPTService.generateLongTermStockPicks();

            // Wait a bit to avoid rate limits
            TimeUnit.SECONDS.sleep(10);

            // Generate sector rotation recommendations
            chatGPTService.generateSectorRotationRecommendations();

            logger.info("Scheduled monthly investment ideas completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled monthly investment ideas generation", e);
        }
    }

    /**
     * Manual trigger to generate analysis for a specific stock
     * Can be called from API endpoints or admin controls
     *
     * @param symbol stock symbol to analyze
     * @return true if analysis was successful
     */
    public boolean triggerManualStockAnalysis(String symbol) {
        logger.info("Manually triggered stock analysis for: {}", symbol);

        try {
            // Generate technical analysis
            analysisService.generateTechnicalAnalysis(symbol);

            // Generate AI recommendation
            chatGPTService.generateStockRecommendation(symbol);

            // Generate trend analysis
            analysisService.generateTrendAnalysis(symbol);

            logger.info("Manual stock analysis completed successfully for: {}", symbol);
            return true;
        } catch (Exception e) {
            logger.error("Error during manual stock analysis for {}: {}", symbol, e.getMessage());
            return false;
        }
    }
}
