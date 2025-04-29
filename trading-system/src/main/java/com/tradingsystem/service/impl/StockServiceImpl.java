package com.tradingsystem.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingsystem.exception.ResourceNotFoundException;
import com.tradingsystem.model.dto.StockData;
import com.tradingsystem.model.dto.TechnicalIndicator;
import com.tradingsystem.model.entity.Analysis;
import com.tradingsystem.model.entity.Stock;
import com.tradingsystem.model.enums.TrendType;
import com.tradingsystem.repository.StockRepository;
import com.tradingsystem.service.external.AlphaVantageService;
import com.tradingsystem.service.external.ChatGPTService;
import com.tradingsystem.service.interfaces.AnalysisService;
import com.tradingsystem.service.interfaces.StockService;
import com.tradingsystem.util.TrendDetector;

/**
 * Реализация сервиса для работы с акциями
 */
@Service
public class StockServiceImpl implements StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockServiceImpl.class);

    private final StockRepository stockRepository;
    private final AlphaVantageService alphaVantageService;
    private final ChatGPTService chatGPTService;
    private final AnalysisService analysisService;
    private final TrendDetector trendDetector;

    @Autowired
    public StockServiceImpl(
            StockRepository stockRepository,
            AlphaVantageService alphaVantageService,
            ChatGPTService chatGPTService,
            AnalysisService analysisService,
            TrendDetector trendDetector) {
        this.stockRepository = stockRepository;
        this.alphaVantageService = alphaVantageService;
        this.chatGPTService = chatGPTService;
        this.analysisService = analysisService;
        this.trendDetector = trendDetector;
    }

    @Override
    @Cacheable(value = "stocks")
    public List<Stock> getAllStocks() {
        logger.debug("Getting all stocks");
        return stockRepository.findAll();
    }

    @Override
    @Cacheable(value = "stocks", key = "#id")
    public Stock getStockById(Long id) {
        logger.debug("Getting stock with id: {}", id);
        return stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with id: " + id));
    }

    @Override
    @Cacheable(value = "stocks", key = "#symbol")
    public Stock getStockBySymbol(String symbol) {
        logger.debug("Getting stock with symbol: {}", symbol);
        return stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with symbol: " + symbol));
    }

    @Override
    @Transactional
    public Stock addStock(Stock stock) {
        logger.debug("Adding new stock: {}", stock.getSymbol());

        // Проверка наличия акции по символу
        if (stockRepository.findBySymbol(stock.getSymbol()).isPresent()) {
            throw new IllegalArgumentException("Stock with symbol " + stock.getSymbol() + " already exists");
        }

        stock.setSymbol(stock.getSymbol().toUpperCase());
        stock.setLastUpdated(LocalDateTime.now());

        // Получение данных из AlphaVantage при наличии API ключа
        try {
            updateStockWithExternalData(stock);
        } catch (Exception e) {
            logger.warn("Failed to get initial data from AlphaVantage for stock: {}", stock.getSymbol(), e);
        }

        return stockRepository.save(stock);
    }

    @Override
    @Transactional
    @CacheEvict(value = "stocks", key = "#symbol")
    public Stock updateStockData(String symbol) {
        logger.debug("Updating data for stock with symbol: {}", symbol);
        Stock stock = getStockBySymbol(symbol);

        try {
            updateStockWithExternalData(stock);
        } catch (Exception e) {
            logger.error("Failed to update stock data from AlphaVantage for stock: {}", symbol, e);
            throw new RuntimeException("Failed to update stock data: " + e.getMessage(), e);
        }

        stock.setLastUpdated(LocalDateTime.now());
        Stock updatedStock = stockRepository.save(stock);

        // Если изменение цены значительное, генерируем анализ
        if (updatedStock.getDayChangePercent() != null &&
                Math.abs(updatedStock.getDayChangePercent().doubleValue()) >= 2.0) {
            logger.debug("Generating analysis for stock with significant price change: {}", symbol);
            try {
                generateAnalysis(symbol);
            } catch (Exception e) {
                logger.error("Failed to generate analysis for stock: {}", symbol, e);
            }
        }

        return updatedStock;
    }

    @Override
    public List<Stock> searchStocks(String sector, String industry, Double minChangePercent, Double maxChangePercent) {
        logger.debug("Searching stocks with criteria: sector={}, industry={}, minChange={}, maxChange={}",
                sector, industry, minChangePercent, maxChangePercent);

        // Если указаны и сектор, и индустрия
        if (sector != null && !sector.isEmpty() && industry != null && !industry.isEmpty()) {
            return stockRepository.findBySectorAndIndustry(sector, industry);
        }

        // Если указан только сектор
        if (sector != null && !sector.isEmpty()) {
            return stockRepository.findBySector(sector);
        }

        // Если указана только индустрия
        if (industry != null && !industry.isEmpty()) {
            return stockRepository.findByIndustry(industry);
        }

        // Если указан диапазон изменения цены
        if (minChangePercent != null && maxChangePercent != null) {
            return stockRepository.findStocksWithChangeInRange(
                    BigDecimal.valueOf(minChangePercent),
                    BigDecimal.valueOf(maxChangePercent));
        }

        // Если указано только минимальное изменение
        if (minChangePercent != null) {
            return stockRepository.findStocksWithPositiveChange(BigDecimal.valueOf(minChangePercent));
        }

        // Если указано только максимальное изменение
        if (maxChangePercent != null) {
            return stockRepository.findStocksWithNegativeChange(BigDecimal.valueOf(maxChangePercent));
        }

        // Если критерии не указаны, возвращаем все акции
        return getAllStocks();
    }

    @Override
    public List<Stock> getTrendingStocks(int limit) {
        logger.debug("Getting top {} trending stocks", limit);

        // Получаем топ акций с наибольшим ростом
        List<Stock> topGainers = stockRepository.findTopGainers(PageRequest.of(0, limit / 2));

        // Получаем топ акций с наибольшим падением
        List<Stock> topLosers = stockRepository.findTopLosers(PageRequest.of(0, limit / 2));

        // Объединяем списки
        List<Stock> trendingStocks = new ArrayList<>(topGainers);
        trendingStocks.addAll(topLosers);

        return trendingStocks;
    }

    @Override
    public List<Stock> getStocksWithSignificantChanges(double threshold) {
        logger.debug("Getting stocks with significant changes (threshold: {}%)", threshold);

        BigDecimal positiveThreshold = BigDecimal.valueOf(threshold);
        BigDecimal negativeThreshold = BigDecimal.valueOf(-threshold);

        List<Stock> stocksWithPositiveChange = stockRepository.findStocksWithPositiveChange(positiveThreshold);
        List<Stock> stocksWithNegativeChange = stockRepository.findStocksWithNegativeChange(negativeThreshold);

        List<Stock> result = new ArrayList<>(stocksWithPositiveChange);
        result.addAll(stocksWithNegativeChange);

        return result;
    }

    @Override
    @Cacheable(value = "sectors")
    public List<String> getAllSectors() {
        logger.debug("Getting all sectors");
        return stockRepository.findAllSectors();
    }

    @Override
    @Cacheable(value = "industries", key = "#sector")
    public List<String> getIndustriesBySector(String sector) {
        logger.debug("Getting industries for sector: {}", sector);
        return stockRepository.findAllIndustriesBySector(sector);
    }

    @Override
    @Transactional
    public Analysis generateAnalysis(String symbol) {
        logger.debug("Generating analysis for stock: {}", symbol);
        Stock stock = getStockBySymbol(symbol);

        try {
            // Получаем технические индикаторы
            TechnicalIndicator rsi = alphaVantageService.getRSI(symbol, "daily", 14);
            TechnicalIndicator macd = alphaVantageService.getMACD(symbol, "daily", 12, 26, 9);

            // Определяем тренд
            TrendType trend = trendDetector.detectTrend(stock, rsi, macd);

            // Собираем данные для ChatGPT
            List<Map<String, Object>> technicalData = new ArrayList<>();
            technicalData.add(Map.of("name", "RSI (14)", "value", rsi.getValue()));
            technicalData.add(Map.of("name", "MACD", "value", macd.getValue()));
            technicalData.add(Map.of("name", "MACD Signal", "value", macd.getSignal()));
            technicalData.add(Map.of("name", "MACD Histogram", "value", macd.getHistogram()));

            // Получаем рекомендацию от ChatGPT
            String adviceText = chatGPTService.getInvestmentAdvice(stock, technicalData, trend);

            // Извлекаем рекомендацию из текста
            String recommendation = chatGPTService.extractRecommendation(adviceText);

            // Определяем уровень уверенности в рекомендации
            double confidence = chatGPTService.calculateConfidence(adviceText);

            // Создаем объект анализа
            Analysis analysis = new Analysis();
            analysis.setStock(stock);
            analysis.setTrend(trend);
            analysis.setRecommendation(recommendation);
            analysis.setRationale(adviceText);
            analysis.setAnalysisDate(LocalDateTime.now());
            analysis.setConfidenceScore(BigDecimal.valueOf(confidence).setScale(2, RoundingMode.HALF_UP));

            // Сохраняем анализ
            return analysisService.saveAnalysis(analysis);
        } catch (Exception e) {
            logger.error("Failed to generate analysis for stock: {}", symbol, e);
            throw new RuntimeException("Failed to generate analysis: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "stocks", key = "#id")
    public void deleteStock(Long id) {
        logger.debug("Deleting stock with id: {}", id);
        Stock stock = getStockById(id);

        // Проверка, можно ли удалить акцию (например, если она используется в портфелях)
        if (!stock.getHoldings().isEmpty()) {
            throw new IllegalStateException("Cannot delete stock with id " + id + " because it is used in portfolios");
        }

        stockRepository.deleteById(id);
    }

    /**
     * Обновляет данные об акции из внешнего API
     * @param stock Акция для обновления
     */
    private void updateStockWithExternalData(Stock stock) {
        // Получаем данные котировок
        StockData stockData = alphaVantageService.getQuote(stock.getSymbol());

        // Обновляем цены
        stock.setCurrentPrice(stockData.getCurrentPrice());
        stock.setPreviousClose(stockData.getPreviousClose());

        // Рассчитываем процент изменения
        stock.setDayChangePercent(calculateDayChangePercent(stockData));

        // Если это новая акция без названия, получаем информацию о компании
        if (stock.getName() == null || stock.getName().isEmpty()) {
            Map<String, Object> companyInfo = alphaVantageService.getCompanyOverview(stock.getSymbol());
            if (companyInfo != null) {
                stock.setName((String) companyInfo.get("Name"));
                stock.setSector((String) companyInfo.get("Sector"));
                stock.setIndustry((String) companyInfo.get("Industry"));
            }
        }
    }

    /**
     * Рассчитывает процент изменения цены
     * @param stockData Данные котировки
     * @return Процент изменения
     */
    private BigDecimal calculateDayChangePercent(StockData stockData) {
        if (stockData.getPreviousClose() == null ||
                stockData.getPreviousClose().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return stockData.getCurrentPrice()
                .subtract(stockData.getPreviousClose())
                .divide(stockData.getPreviousClose(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
