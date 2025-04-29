package com.tradingsystem.util;

import com.tradingsystem.model.dto.StockData;
import com.tradingsystem.model.dto.TechnicalIndicator;
import com.tradingsystem.model.enums.TrendType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for analyzing stock data and calculating technical indicators
 */
@Component
public class StockAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(StockAnalyzer.class);

    private final TrendDetector trendDetector;

    public StockAnalyzer(TrendDetector trendDetector) {
        this.trendDetector = trendDetector;
    }

    /**
     * Calculate Simple Moving Average (SMA) for a given period
     *
     * @param stockData list of stock price data points
     * @param period number of periods to calculate SMA
     * @return map of date to SMA value
     */
    public Map<String, Double> calculateSMA(List<StockData> stockData, int period) {
        if (stockData == null || stockData.isEmpty() || period <= 0 || period > stockData.size()) {
            logger.warn("Invalid input for SMA calculation. Data size: {}, Period: {}",
                    stockData != null ? stockData.size() : 0, period);
            return Collections.emptyMap();
        }

        Map<String, Double> smaValues = new LinkedHashMap<>();

        // Sort data by date (ascending)
        List<StockData> sortedData = stockData.stream()
                .sorted(Comparator.comparing(StockData::getDate))
                .collect(Collectors.toList());

        for (int i = period - 1; i < sortedData.size(); i++) {
            double sum = 0;
            for (int j = 0; j < period; j++) {
                sum += sortedData.get(i - j).getClose();
            }
            double sma = sum / period;
            smaValues.put(sortedData.get(i).getDate(), sma);
        }

        return smaValues;
    }

    /**
     * Calculate Exponential Moving Average (EMA) for a given period
     *
     * @param stockData list of stock price data points
     * @param period number of periods to calculate EMA
     * @return map of date to EMA value
     */
    public Map<String, Double> calculateEMA(List<StockData> stockData, int period) {
        if (stockData == null || stockData.isEmpty() || period <= 0 || period > stockData.size()) {
            logger.warn("Invalid input for EMA calculation. Data size: {}, Period: {}",
                    stockData != null ? stockData.size() : 0, period);
            return Collections.emptyMap();
        }

        Map<String, Double> emaValues = new LinkedHashMap<>();

        // Sort data by date (ascending)
        List<StockData> sortedData = stockData.stream()
                .sorted(Comparator.comparing(StockData::getDate))
                .collect(Collectors.toList());

        // Calculate multiplier
        double multiplier = 2.0 / (period + 1);

        // Calculate first EMA as SMA
        double sum = 0;
        for (int i = 0; i < period; i++) {
            sum += sortedData.get(i).getClose();
        }
        double ema = sum / period;
        emaValues.put(sortedData.get(period - 1).getDate(), ema);

        // Calculate EMA for remaining periods
        for (int i = period; i < sortedData.size(); i++) {
            ema = (sortedData.get(i).getClose() - ema) * multiplier + ema;
            emaValues.put(sortedData.get(i).getDate(), ema);
        }

        return emaValues;
    }

    /**
     * Calculate Relative Strength Index (RSI) for a given period
     *
     * @param stockData list of stock price data points
     * @param period number of periods to calculate RSI (typically 14)
     * @return map of date to RSI value
     */
    public Map<String, Double> calculateRSI(List<StockData> stockData, int period) {
        if (stockData == null || stockData.isEmpty() || period <= 0 || period >= stockData.size()) {
            logger.warn("Invalid input for RSI calculation. Data size: {}, Period: {}",
                    stockData != null ? stockData.size() : 0, period);
            return Collections.emptyMap();
        }

        Map<String, Double> rsiValues = new LinkedHashMap<>();

        // Sort data by date (ascending)
        List<StockData> sortedData = stockData.stream()
                .sorted(Comparator.comparing(StockData::getDate))
                .collect(Collectors.toList());

        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();

        // Calculate price changes
        for (int i = 1; i < sortedData.size(); i++) {
            double change = sortedData.get(i).getClose() - sortedData.get(i - 1).getClose();
            if (change >= 0) {
                gains.add(change);
                losses.add(0.0);
            } else {
                gains.add(0.0);
                losses.add(Math.abs(change));
            }
        }

        // Calculate first average gain and loss
        double avgGain = gains.stream().limit(period).mapToDouble(Double::doubleValue).sum() / period;
        double avgLoss = losses.stream().limit(period).mapToDouble(Double::doubleValue).sum() / period;

        // Calculate RSI for first period
        double rs = avgGain / Math.max(avgLoss, 0.001); // Avoid division by zero
        double rsi = 100 - (100 / (1 + rs));
        rsiValues.put(sortedData.get(period).getDate(), rsi);

        // Calculate RSI for remaining periods
        for (int i = period; i < gains.size(); i++) {
            avgGain = ((avgGain * (period - 1)) + gains.get(i)) / period;
            avgLoss = ((avgLoss * (period - 1)) + losses.get(i)) / period;

            rs = avgGain / Math.max(avgLoss, 0.001); // Avoid division by zero
            rsi = 100 - (100 / (1 + rs));
            rsiValues.put(sortedData.get(i + 1).getDate(), rsi);
        }

        return rsiValues;
    }

    /**
     * Calculate Moving Average Convergence Divergence (MACD)
     *
     * @param stockData list of stock price data points
     * @param fastPeriod fast EMA period (typically 12)
     * @param slowPeriod slow EMA period (typically 26)
     * @param signalPeriod signal EMA period (typically 9)
     * @return map of date to MACD values (MACD line, signal line, and histogram)
     */
    public Map<String, double[]> calculateMACD(List<StockData> stockData, int fastPeriod, int slowPeriod, int signalPeriod) {
        if (stockData == null || stockData.isEmpty() || fastPeriod <= 0 || slowPeriod <= 0 || signalPeriod <= 0) {
            logger.warn("Invalid input for MACD calculation");
            return Collections.emptyMap();
        }

        // Calculate fast and slow EMAs
        Map<String, Double> fastEMA = calculateEMA(stockData, fastPeriod);
        Map<String, Double> slowEMA = calculateEMA(stockData, slowPeriod);

        // Calculate MACD line (fast EMA - slow EMA)
        Map<String, Double> macdLine = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : slowEMA.entrySet()) {
            String date = entry.getKey();
            if (fastEMA.containsKey(date)) {
                macdLine.put(date, fastEMA.get(date) - slowEMA.get(date));
            }
        }

        // Calculate signal line (EMA of MACD line)
        List<StockData> macdData = new ArrayList<>();
        for (Map.Entry<String, Double> entry : macdLine.entrySet()) {
            StockData data = new StockData();
            data.setDate(entry.getKey());
            data.setClose(entry.getValue());
            macdData.add(data);
        }
        Map<String, Double> signalLine = calculateEMA(macdData, signalPeriod);

        // Calculate histogram (MACD line - signal line)
        Map<String, double[]> macdValues = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : signalLine.entrySet()) {
            String date = entry.getKey();
            if (macdLine.containsKey(date)) {
                double macd = macdLine.get(date);
                double signal = entry.getValue();
                double histogram = macd - signal;
                macdValues.put(date, new double[]{macd, signal, histogram});
            }
        }

        return macdValues;
    }

    /**
     * Calculate Bollinger Bands
     *
     * @param stockData list of stock price data points
     * @param period period for SMA calculation (typically 20)
     * @param stdDev number of standard deviations (typically 2)
     * @return map of date to Bollinger Bands values (middle band, upper band, lower band)
     */
    public Map<String, double[]> calculateBollingerBands(List<StockData> stockData, int period, double stdDev) {
        if (stockData == null || stockData.isEmpty() || period <= 0 || stdDev <= 0) {
            logger.warn("Invalid input for Bollinger Bands calculation");
            return Collections.emptyMap();
        }

        // Calculate SMA (middle band)
        Map<String, Double> sma = calculateSMA(stockData, period);

        // Sort data by date (ascending)
        List<StockData> sortedData = stockData.stream()
                .sorted(Comparator.comparing(StockData::getDate))
                .collect(Collectors.toList());

        Map<String, double[]> bollingerBands = new LinkedHashMap<>();

        for (int i = period - 1; i < sortedData.size(); i++) {
            String date = sortedData.get(i).getDate();
            double middleBand = sma.get(date);

            // Calculate standard deviation
            double sum = 0;
            for (int j = 0; j < period; j++) {
                double diff = sortedData.get(i - j).getClose() - middleBand;
                sum += diff * diff;
            }
            double standardDeviation = Math.sqrt(sum / period);

            // Calculate upper and lower bands
            double upperBand = middleBand + (standardDeviation * stdDev);
            double lowerBand = middleBand - (standardDeviation * stdDev);

            bollingerBands.put(date, new double[]{middleBand, upperBand, lowerBand});
        }

        return bollingerBands;
    }

    /**
     * Calculate Volume Weighted Average Price (VWAP)
     *
     * @param stockData list of stock price data points with volume
     * @param period number of periods to calculate VWAP
     * @return map of date to VWAP value
     */
    public Map<String, Double> calculateVWAP(List<StockData> stockData, int period) {
        if (stockData == null || stockData.isEmpty() || period <= 0) {
            logger.warn("Invalid input for VWAP calculation");
            return Collections.emptyMap();
        }

        // Sort data by date (ascending)
        List<StockData> sortedData = stockData.stream()
                .sorted(Comparator.comparing(StockData::getDate))
                .collect(Collectors.toList());

        Map<String, Double> vwapValues = new LinkedHashMap<>();

        for (int i = period - 1; i < sortedData.size(); i++) {
            double sumPriceVolume = 0;
            double sumVolume = 0;

            for (int j = 0; j < period; j++) {
                StockData data = sortedData.get(i - j);
                // Typical price = (High + Low + Close) / 3
                double typicalPrice = (data.getHigh() + data.getLow() + data.getClose()) / 3;
                sumPriceVolume += typicalPrice * data.getVolume();
                sumVolume += data.getVolume();
            }

            double vwap = sumPriceVolume / Math.max(sumVolume, 1); // Avoid division by zero
            vwapValues.put(sortedData.get(i).getDate(), vwap);
        }

        return vwapValues;
    }

    /**
     * Calculate Average True Range (ATR)
     *
     * @param stockData list of stock price data points
     * @param period number of periods to calculate ATR (typically 14)
     * @return map of date to ATR value
     */
    public Map<String, Double> calculateATR(List<StockData> stockData, int period) {
        if (stockData == null || stockData.isEmpty() || period <= 0) {
            logger.warn("Invalid input for ATR calculation");
            return Collections.emptyMap();
        }

        // Sort data by date (ascending)
        List<StockData> sortedData = stockData.stream()
                .sorted(Comparator.comparing(StockData::getDate))
                .collect(Collectors.toList());

        // Calculate True Range for each data point
        List<Double> trueRanges = new ArrayList<>();
        trueRanges.add(sortedData.get(0).getHigh() - sortedData.get(0).getLow()); // First TR is just high - low

        for (int i = 1; i < sortedData.size(); i++) {
            StockData current = sortedData.get(i);
            StockData previous = sortedData.get(i - 1);

            double tr1 = current.getHigh() - current.getLow(); // Current high - current low
            double tr2 = Math.abs(current.getHigh() - previous.getClose()); // Current high - previous close
            double tr3 = Math.abs(current.getLow() - previous.getClose()); // Current low - previous close

            double trueRange = Math.max(Math.max(tr1, tr2), tr3);
            trueRanges.add(trueRange);
        }

        // Calculate first ATR as simple average of first 'period' true ranges
        double firstATR = trueRanges.stream()
                .limit(period)
                .mapToDouble(Double::doubleValue)
                .sum() / period;

        // Calculate ATR using smoothing formula: ATR = [(Prior ATR * (period - 1)) + Current TR] / period
        Map<String, Double> atrValues = new LinkedHashMap<>();
        atrValues.put(sortedData.get(period - 1).getDate(), firstATR);

        double atr = firstATR;
        for (int i = period; i < sortedData.size(); i++) {
            atr = ((atr * (period - 1)) + trueRanges.get(i)) / period;
            atrValues.put(sortedData.get(i).getDate(), atr);
        }

        return atrValues;
    }

    /**
     * Calculate all technical indicators for a stock
     *
     * @param stockData list of stock price data points
     * @return technical indicators with all calculated values
     */
    public TechnicalIndicator calculateAllIndicators(List<StockData> stockData) {
        if (stockData == null || stockData.isEmpty()) {
            logger.warn("Cannot calculate indicators for empty or null stock data");
            return null;
        }

        TechnicalIndicator indicators = new TechnicalIndicator();

        // Calculate various technical indicators
        indicators.setSma20(calculateSMA(stockData, 20));
        indicators.setSma50(calculateSMA(stockData, 50));
        indicators.setSma200(calculateSMA(stockData, 200));

        indicators.setEma12(calculateEMA(stockData, 12));
        indicators.setEma26(calculateEMA(stockData, 26));

        indicators.setRsi14(calculateRSI(stockData, 14));

        indicators.setMacd(calculateMACD(stockData, 12, 26, 9));

        indicators.setBollingerBands(calculateBollingerBands(stockData, 20, 2.0));

        indicators.setVwap20(calculateVWAP(stockData, 20));

        indicators.setAtr14(calculateATR(stockData, 14));

        // Determine current trend
        if (stockData.size() >= 20) {
            TrendType trend = trendDetector.detectTrend(stockData);
            indicators.setCurrentTrend(trend);
        }

        return indicators;
    }
}
