package com.tradingsystem.util;

import com.tradingsystem.model.dto.StockData;
import com.tradingsystem.model.enums.TrendType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for detecting market trends in stock data
 */
@Component
public class TrendDetector {
    private static final Logger logger = LoggerFactory.getLogger(TrendDetector.class);

    /**
     * Detect the current trend for a stock based on its historical price data
     *
     * @param stockData list of stock price data points
     * @return identified trend type (UPTREND, DOWNTREND, or SIDEWAYS)
     */
    public TrendType detectTrend(List<StockData> stockData) {
        if (stockData == null || stockData.size() < 20) {
            logger.warn("Insufficient data for trend detection. At least 20 data points required.");
            return TrendType.SIDEWAYS; // Default to sideways when insufficient data
        }

        // Sort data by date (ascending)
        List<StockData> sortedData = stockData.stream()
                .sorted(Comparator.comparing(StockData::getDate))
                .collect(Collectors.toList());

        // Get last 20 days for short-term trend analysis
        List<StockData> recentData = sortedData.subList(Math.max(0, sortedData.size() - 20), sortedData.size());

        // Check if price is above both short and medium term moving averages
        boolean isAboveSMA20 = isAboveSMA(sortedData, 20);
        boolean isAboveSMA50 = isAboveSMA(sortedData, 50);

        // Calculate price momentum
        double momentum = calculateMomentum(recentData);

        // Calculate price volatility
        double volatility = calculateVolatility(recentData);

        // Determine trend based on technical factors
        if (isAboveSMA20 && isAboveSMA50 && momentum > 0.02) {
            return TrendType.UPTREND;
        } else if (!isAboveSMA20 && !isAboveSMA50 && momentum < -0.02) {
            return TrendType.DOWNTREND;
        } else if (Math.abs(momentum) < 0.01 && volatility < 0.02) {
            return TrendType.SIDEWAYS;
        } else if (momentum > 0) {
            return TrendType.UPTREND;
        } else {
            return TrendType.DOWNTREND;
        }
    }

    /**
     * Check if the current price is above the simple moving average for a given period
     *
     * @param stockData list of stock price data points
     * @param period SMA period to check
     * @return true if current price is above SMA
     */
    private boolean isAboveSMA(List<StockData> stockData, int period) {
        if (stockData.size() < period) {
            return false;
        }

        // Calculate SMA for the given period
        double sum = 0;
        for (int i = stockData.size() - period; i < stockData.size(); i++) {
            sum += stockData.get(i).getClose();
        }
        double sma = sum / period;

        // Check if current price is above SMA
        double currentPrice = stockData.get(stockData.size() - 1).getClose();
        return currentPrice > sma;
    }

    /**
     * Calculate price momentum over the given data period
     *
     * @param stockData list of stock price data points
     * @return momentum value (positive for uptrend, negative for downtrend)
     */
    private double calculateMomentum(List<StockData> stockData) {
        if (stockData.size() < 2) {
            return 0;
        }

        double startPrice = stockData.get(0).getClose();
        double endPrice = stockData.get(stockData.size() - 1).getClose();

        // Calculate price change percentage
        return (endPrice - startPrice) / startPrice;
    }

    /**
     * Calculate price volatility over the given data period
     *
     * @param stockData list of stock price data points
     * @return volatility value
     */
    private double calculateVolatility(List<StockData> stockData) {
        if (stockData.size() < 2) {
            return 0;
        }

        // Calculate average price
        double sum = 0;
        for (StockData data : stockData) {
            sum += data.getClose();
        }
        double avgPrice = sum / stockData.size();

        // Calculate standard deviation
        double varianceSum = 0;
        for (StockData data : stockData) {
            double diff = data.getClose() - avgPrice;
            varianceSum += diff * diff;
        }
        double variance = varianceSum / stockData.size();
        double stdDev = Math.sqrt(variance);

        // Return volatility as coefficient of variation (standard deviation / mean)
        return stdDev / avgPrice;
    }

    /**
     * Analyze multiple trends to detect market regime
     *
     * @param stockDataMap map of stock symbols to their price data
     * @return the dominant market trend
     */
    public TrendType detectMarketRegime(Map<String, List<StockData>> stockDataMap) {
        if (stockDataMap == null || stockDataMap.isEmpty()) {
            logger.warn("No stock data provided for market regime detection");
            return TrendType.SIDEWAYS;
        }

        int uptrendCount = 0;
        int downtrendCount = 0;
        int sidewaysCount = 0;

        // Detect trend for each stock
        for (List<StockData> stockData : stockDataMap.values()) {
            TrendType trend = detectTrend(stockData);

            switch (trend) {
                case UPTREND:
                    uptrendCount++;
                    break;
                case DOWNTREND:
                    downtrendCount++;
                    break;
                case SIDEWAYS:
                    sidewaysCount++;
                    break;
            }
        }

        // Determine dominant trend
        if (uptrendCount > downtrendCount && uptrendCount > sidewaysCount) {
            return TrendType.UPTREND;
        } else if (downtrendCount > uptrendCount && downtrendCount > sidewaysCount) {
            return TrendType.DOWNTREND;
        } else {
            return TrendType.SIDEWAYS;
        }
    }

    /**
     * Detect if a breakout is occurring based on volume and price action
     *
     * @param stockData list of stock price data points
     * @return true if a breakout is detected
     */
    public boolean detectBreakout(List<StockData> stockData) {
        if (stockData == null || stockData.size() < 10) {
            logger.warn("Insufficient data for breakout detection");
            return false;
        }

        // Sort data by date (ascending)
        List<StockData> sortedData = stockData.stream()
                .sorted(Comparator.comparing(StockData::getDate))
                .collect(Collectors.toList());

        // Calculate average volume for the previous 10 days
        double sumVolume = 0;
        for (int i = sortedData.size() - 10; i < sortedData.size() - 1; i++) {
            sumVolume += sortedData.get(i).getVolume();
        }
        double avgVolume = sumVolume / 9; // Average of previous 9 days

        // Get current day data
        StockData current = sortedData.get(sortedData.size() - 1);
        StockData previous = sortedData.get(sortedData.size() - 2);

        // Check for volume spike (50% above average)
        boolean volumeSpike = current.getVolume() > avgVolume * 1.5;

        // Check for price breakout (2% move)
        double priceChange = (current.getClose() - previous.getClose()) / previous.getClose();
        boolean priceBreakout = Math.abs(priceChange) > 0.02;

        // Check for range expansion (today's range is larger than average)
        double todayRange = current.getHigh() - current.getLow();
        double avgRange = calculateAverageRange(sortedData, 10);
        boolean rangeExpansion = todayRange > avgRange * 1.3;

        // Determine if breakout is occurring
        return volumeSpike && priceBreakout && rangeExpansion;
    }

    /**
     * Calculate average trading range for a given period
     *
     * @param stockData list of stock price data points
     * @param period number of days to calculate average range
     * @return average trading range
     */
    private double calculateAverageRange(List<StockData> stockData, int period) {
        if (stockData.size() < period) {
            return 0;
        }

        double sumRange = 0;
        for (int i = stockData.size() - period; i < stockData.size(); i++) {
            StockData data = stockData.get(i);
            sumRange += (data.getHigh() - data.getLow());
        }

        return sumRange / period;
    }
}
