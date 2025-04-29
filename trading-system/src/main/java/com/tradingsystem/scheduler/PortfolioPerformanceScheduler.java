package com.tradingsystem.scheduler;

import com.tradingsystem.model.entity.Portfolio;
import com.tradingsystem.properties.ApplicationProperties;
import com.tradingsystem.repository.PortfolioRepository;
import com.tradingsystem.service.interfaces.PortfolioService;
import com.tradingsystem.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler for calculating and updating portfolio performance metrics
 */
@Component
public class PortfolioPerformanceScheduler {
    private static final Logger logger = LoggerFactory.getLogger(PortfolioPerformanceScheduler.class);

    private final PortfolioRepository portfolioRepository;
    private final PortfolioService portfolioService;
    private final DateUtils dateUtils;
    private final ApplicationProperties applicationProperties;

    public PortfolioPerformanceScheduler(
            PortfolioRepository portfolioRepository,
            PortfolioService portfolioService,
            DateUtils dateUtils,
            ApplicationProperties applicationProperties) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioService = portfolioService;
        this.dateUtils = dateUtils;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Update all portfolio performances at the end of each trading day
     * Scheduled to run at 4:30 PM ET on weekdays
     */
    @Scheduled(cron = "0 30 16 * * MON-FRI", zone = "America/New_York")
    public void scheduleDailyPerformanceUpdate() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping daily portfolio performance update.");
            return;
        }

        logger.info("Starting scheduled daily portfolio performance update");

        try {
            LocalDate today = LocalDate.now();
            if (!dateUtils.isTradingDay(today)) {
                logger.info("Today is not a trading day. Skipping portfolio performance update.");
                return;
            }

            List<Portfolio> portfolios = portfolioRepository.findAll();

            if (portfolios.isEmpty()) {
                logger.info("No portfolios found for performance update");
                return;
            }

            logger.info("Found {} portfolios to update performance metrics", portfolios.size());

            int count = 0;
            for (Portfolio portfolio : portfolios) {
                try {
                    // Calculate and save daily performance metrics
                    portfolioService.calculateAndSavePerformanceMetrics(portfolio.getId());

                    count++;
                    logger.debug("Updated portfolio performance {}/{}: {}",
                            count, portfolios.size(), portfolio.getName());

                    // Small delay to avoid overwhelming the database
                    if (count < portfolios.size()) {
                        TimeUnit.MILLISECONDS.sleep(200);
                    }
                } catch (Exception e) {
                    logger.error("Error updating portfolio performance for ID {}: {}",
                            portfolio.getId(), e.getMessage());
                }
            }

            logger.info("Scheduled daily portfolio performance update completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled portfolio performance update", e);
        }
    }

    /**
     * Update active portfolio performances during trading hours
     * Scheduled to run every 30 minutes from 10:00 AM to 4:00 PM ET on weekdays
     */
    @Scheduled(cron = "0 */30 10-16 * * MON-FRI", zone = "America/New_York")
    public void scheduleIntradayPerformanceUpdate() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping intraday portfolio performance update.");
            return;
        }

        // Skip if market is closed
        if (!dateUtils.isMarketOpen()) {
            return;
        }

        logger.info("Starting scheduled intraday portfolio performance update");

        try {
            // Get only active portfolios (recently viewed or modified)
            List<Portfolio> activePortfolios = portfolioRepository.findActivePortfolios();

            if (activePortfolios.isEmpty()) {
                logger.info("No active portfolios found for intraday performance update");
                return;
            }

            logger.info("Found {} active portfolios for intraday performance update", activePortfolios.size());

            int count = 0;
            for (Portfolio portfolio : activePortfolios) {
                try {
                    // Calculate and update intraday performance metrics
                    portfolioService.calculateIntradayPerformance(portfolio.getId());

                    count++;
                    logger.debug("Updated intraday portfolio performance {}/{}: {}",
                            count, activePortfolios.size(), portfolio.getName());

                    // Small delay to avoid overwhelming the database
                    if (count < activePortfolios.size()) {
                        TimeUnit.MILLISECONDS.sleep(200);
                    }
                } catch (Exception e) {
                    logger.error("Error updating intraday portfolio performance for ID {}: {}",
                            portfolio.getId(), e.getMessage());
                }
            }

            logger.info("Scheduled intraday portfolio performance update completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled intraday portfolio performance update", e);
        }
    }

    /**
     * Calculate monthly portfolio performance metrics
     * Scheduled to run at 12:00 AM on the first day of each month
     */
    @Scheduled(cron = "0 0 0 1 * ?", zone = "America/New_York")
    public void scheduleMonthlyPerformanceCalculation() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping monthly portfolio performance calculation.");
            return;
        }

        logger.info("Starting scheduled monthly portfolio performance calculation");

        try {
            List<Portfolio> portfolios = portfolioRepository.findAll();

            if (portfolios.isEmpty()) {
                logger.info("No portfolios found for monthly performance calculation");
                return;
            }

            // Get previous month
            LocalDate now = LocalDate.now();
            LocalDate prevMonth = now.minusMonths(1);
            int year = prevMonth.getYear();
            int month = prevMonth.getMonthValue();

            logger.info("Calculating monthly performance for {}-{} for {} portfolios",
                    year, month, portfolios.size());

            for (Portfolio portfolio : portfolios) {
                try {
                    // Calculate and save monthly performance metrics
                    portfolioService.calculateMonthlyPerformance(portfolio.getId(), year, month);

                    // Small delay to avoid overwhelming the database
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (Exception e) {
                    logger.error("Error calculating monthly performance for portfolio ID {}: {}",
                            portfolio.getId(), e.getMessage());
                }
            }

            logger.info("Scheduled monthly portfolio performance calculation completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled monthly portfolio performance calculation", e);
        }
    }

    /**
     * Calculate quarterly portfolio performance metrics
     * Scheduled to run at 1:00 AM on the first day of each quarter (Jan, Apr, Jul, Oct)
     */
    @Scheduled(cron = "0 0 1 1 1,4,7,10 ?", zone = "America/New_York")
    public void scheduleQuarterlyPerformanceCalculation() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping quarterly portfolio performance calculation.");
            return;
        }

        logger.info("Starting scheduled quarterly portfolio performance calculation");

        try {
            List<Portfolio> portfolios = portfolioRepository.findAll();

            if (portfolios.isEmpty()) {
                logger.info("No portfolios found for quarterly performance calculation");
                return;
            }

            // Get previous quarter
            LocalDate now = LocalDate.now();
            LocalDate firstDayOfMonth = LocalDate.of(now.getYear(), now.getMonthValue(), 1);
            LocalDate firstDayOfPrevQuarter = firstDayOfMonth.minusMonths(3);
            int year = firstDayOfPrevQuarter.getYear();
            int quarter = (firstDayOfPrevQuarter.getMonthValue() - 1) / 3 + 1;

            logger.info("Calculating quarterly performance for {} Q{} for {} portfolios",
                    year, quarter, portfolios.size());

            for (Portfolio portfolio : portfolios) {
                try {
                    // Calculate and save quarterly performance metrics
                    portfolioService.calculateQuarterlyPerformance(portfolio.getId(), year, quarter);

                    // Small delay to avoid overwhelming the database
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (Exception e) {
                    logger.error("Error calculating quarterly performance for portfolio ID {}: {}",
                            portfolio.getId(), e.getMessage());
                }
            }

            logger.info("Scheduled quarterly portfolio performance calculation completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled quarterly portfolio performance calculation", e);
        }
    }

    /**
     * Calculate annual portfolio performance metrics
     * Scheduled to run at 2:00 AM on January 1st each year
     */
    @Scheduled(cron = "0 0 2 1 1 ?", zone = "America/New_York")
    public void scheduleAnnualPerformanceCalculation() {
        if (!applicationProperties.isScheduledUpdatesEnabled()) {
            logger.info("Scheduled updates are disabled. Skipping annual portfolio performance calculation.");
            return;
        }

        logger.info("Starting scheduled annual portfolio performance calculation");

        try {
            List<Portfolio> portfolios = portfolioRepository.findAll();

            if (portfolios.isEmpty()) {
                logger.info("No portfolios found for annual performance calculation");
                return;
            }

            // Get previous year
            int prevYear = LocalDate.now().getYear() - 1;

            logger.info("Calculating annual performance for year {} for {} portfolios",
                    prevYear, portfolios.size());

            for (Portfolio portfolio : portfolios) {
                try {
                    // Calculate and save annual performance metrics
                    portfolioService.calculateAnnualPerformance(portfolio.getId(), prevYear);

                    // Small delay to avoid overwhelming the database
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (Exception e) {
                    logger.error("Error calculating annual performance for portfolio ID {}: {}",
                            portfolio.getId(), e.getMessage());
                }
            }

            logger.info("Scheduled annual portfolio performance calculation completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled annual portfolio performance calculation", e);
        }
    }

    /**
     * Manual trigger to update performance for a specific portfolio
     * Can be called from API endpoints or admin controls
     *
     * @param portfolioId ID of the portfolio to update
     * @return true if update was successful
     */
    public boolean triggerManualPerformanceUpdate(Long portfolioId) {
        logger.info("Manually triggered performance update for portfolio ID: {}", portfolioId);

        try {
            portfolioService.calculateAndSavePerformanceMetrics(portfolioId);
            logger.info("Manual performance update completed successfully for portfolio ID: {}", portfolioId);
            return true;
        } catch (Exception e) {
            logger.error("Error during manual performance update for portfolio ID {}: {}",
                    portfolioId, e.getMessage());
            return false;
        }
    }
}
