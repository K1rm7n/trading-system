package com.tradingsystem.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingsystem.exception.ResourceNotFoundException;
import com.tradingsystem.model.entity.Analysis;
import com.tradingsystem.model.entity.Stock;
import com.tradingsystem.model.enums.TrendType;
import com.tradingsystem.repository.AnalysisRepository;
import com.tradingsystem.repository.StockRepository;
import com.tradingsystem.service.interfaces.AnalysisService;
import com.tradingsystem.service.interfaces.StockService;

/**
 * Реализация сервиса для работы с аналитическими данными
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisServiceImpl.class);

    private final AnalysisRepository analysisRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;

    @Autowired
    public AnalysisServiceImpl(
            AnalysisRepository analysisRepository,
            StockRepository stockRepository,
            StockService stockService) {
        this.analysisRepository = analysisRepository;
        this.stockRepository = stockRepository;
        this.stockService = stockService;
    }

    @Override
    @Cacheable(value = "analyses")
    public List<Analysis> getAllAnalyses() {
        logger.debug("Getting all analyses");
        return analysisRepository.findAll();
    }

    @Override
    @Cacheable(value = "analyses", key = "#id")
    public Analysis getAnalysisById(Long id) {
        logger.debug("Getting analysis with id: {}", id);
        return analysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with id: " + id));
    }

    @Override
    @Cacheable(value = "analyses", key = "'stock_' + #stockId")
    public List<Analysis> getAnalysesForStock(Long stockId) {
        logger.debug("Getting analyses for stock with id: {}", stockId);
        return analysisRepository.findByStockId(stockId);
    }

    @Override
    @Cacheable(value = "analyses", key = "'symbol_' + #symbol")
    public List<Analysis> getAnalysesForStockBySymbol(String symbol) {
        logger.debug("Getting analyses for stock with symbol: {}", symbol);
        Stock stock = stockService.getStockBySymbol(symbol);
        return analysisRepository.findByStock(stock);
    }

    @Override
    @Cacheable(value = "analyses", key = "'trend_' + #trend")
    public List<Analysis> getAnalysesByTrend(TrendType trend) {
        logger.debug("Getting analyses with trend: {}", trend);
        return analysisRepository.findByTrend(trend);
    }

    @Override
    @Cacheable(value = "analyses", key = "'recommendation_' + #recommendation")
    public List<Analysis> getAnalysesByRecommendation(String recommendation) {
        logger.debug("Getting analyses with recommendation: {}", recommendation);
        return analysisRepository.findByRecommendation(recommendation);
    }

    @Override
    @Cacheable(value = "analyses", key = "'latest_stock_' + #stockId")
    public Analysis getLatestAnalysisForStock(Long stockId) {
        logger.debug("Getting latest analysis for stock with id: {}", stockId);
        List<Analysis> analyses = analysisRepository.findLatestAnalysisByStockId(stockId, PageRequest.of(0, 1));

        if (analyses.isEmpty()) {
            throw new ResourceNotFoundException("No analysis found for stock with id: " + stockId);
        }

        return analyses.get(0);
    }

    @Override
    @Cacheable(value = "analyses", key = "'latest_' + #limit")
    public List<Analysis> getLatestAnalyses(int limit) {
        logger.debug("Getting {} latest analyses", limit);
        return analysisRepository.findLatestAnalyses(PageRequest.of(0, limit));
    }

    @Override
    @Transactional
    public Analysis generateAnalysis(String symbol) {
        logger.debug("Generating analysis for stock with symbol: {}", symbol);
        return stockService.generateAnalysis(symbol);
    }

    @Override
    @Transactional
    @CacheEvict(value = "analyses", allEntries = true)
    public void deleteAnalysis(Long id) {
        logger.debug("Deleting analysis with id: {}", id);

        if (!analysisRepository.existsById(id)) {
            throw new ResourceNotFoundException("Analysis not found with id: " + id);
        }

        analysisRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = "analyses", key = "'confident_buy_' + #limit")
    public List<Analysis> getMostConfidentBuyRecommendations(int limit) {
        logger.debug("Getting {} most confident BUY recommendations", limit);
        return analysisRepository.findMostConfidentBuyRecommendations(PageRequest.of(0, limit));
    }

    @Override
    @Cacheable(value = "analyses", key = "'confident_sell_' + #limit")
    public List<Analysis> getMostConfidentSellRecommendations(int limit) {
        logger.debug("Getting {} most confident SELL recommendations", limit);
        return analysisRepository.findMostConfidentSellRecommendations(PageRequest.of(0, limit));
    }

    /**
     * Сохраняет анализ (внутренний метод для StockService)
     * @param analysis Анализ для сохранения
     * @return Сохраненный анализ
     */
    @Transactional
    public Analysis saveAnalysis(Analysis analysis) {
        logger.debug("Saving analysis for stock: {}", analysis.getStock().getSymbol());
        return analysisRepository.save(analysis);
    }
}
