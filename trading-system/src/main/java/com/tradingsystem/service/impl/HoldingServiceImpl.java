package com.tradingsystem.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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
import com.tradingsystem.model.dto.HoldingDTO;
import com.tradingsystem.model.entity.Holding;
import com.tradingsystem.model.entity.Portfolio;
import com.tradingsystem.model.entity.Stock;
import com.tradingsystem.repository.HoldingRepository;
import com.tradingsystem.repository.PortfolioRepository;
import com.tradingsystem.repository.StockRepository;
import com.tradingsystem.service.interfaces.HoldingService;

/**
 * Реализация сервиса для работы с позициями в портфеле
 */
@Service
public class HoldingServiceImpl implements HoldingService {

    private static final Logger logger = LoggerFactory.getLogger(HoldingServiceImpl.class);

    private final HoldingRepository holdingRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;

    @Autowired
    public HoldingServiceImpl(
            HoldingRepository holdingRepository,
            PortfolioRepository portfolioRepository,
            StockRepository stockRepository) {
        this.holdingRepository = holdingRepository;
        this.portfolioRepository = portfolioRepository;
        this.stockRepository = stockRepository;
    }

    @Override
    @Cacheable(value = "holdings")
    public List<Holding> getAllHoldings() {
        logger.debug("Getting all holdings");
        return holdingRepository.findAll();
    }

    @Override
    @Cacheable(value = "holdings", key = "#id")
    public Holding getHoldingById(Long id) {
        logger.debug("Getting holding with id: {}", id);
        return holdingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holding not found with id: " + id));
    }

    @Override
    @Cacheable(value = "holdings", key = "'portfolio_' + #portfolioId")
    public List<Holding> getHoldingsByPortfolioId(Long portfolioId) {
        logger.debug("Getting holdings for portfolio with id: {}", portfolioId);
        return holdingRepository.findByPortfolioId(portfolioId);
    }

    @Override
    @Cacheable(value = "holdings", key = "'portfolio_dto_' + #portfolioId")
    public List<HoldingDTO> getHoldingDTOsByPortfolioId(Long portfolioId) {
        logger.debug("Getting holding DTOs for portfolio with id: {}", portfolioId);

        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);

        return holdings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "holdings", key = "'portfolio_' + #portfolioId + '_stock_' + #stockId")
    public Holding getHoldingByPortfolioAndStock(Long portfolioId, Long stockId) {
        logger.debug("Getting holding for portfolio id: {} and stock id: {}", portfolioId, stockId);

        return holdingRepository.findByPortfolioIdAndStockId(portfolioId, stockId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Holding not found for portfolio id: " + portfolioId + " and stock id: " + stockId));
    }

    @Override
    @Transactional
    public Holding createHolding(Holding holding) {
        logger.debug("Creating new holding for portfolio id: {} and stock id: {}",
                holding.getPortfolio().getId(), holding.getStock().getId());

        // Проверка, что такой позиции еще нет
        if (holdingRepository.existsByPortfolioIdAndStockId(
                holding.getPortfolio().getId(), holding.getStock().getId())) {
            throw new IllegalArgumentException("Holding already exists for this portfolio and stock");
        }

        return holdingRepository.save(holding);
    }

    @Override
    @Transactional
    @CacheEvict(value = "holdings", key = "#id")
    public Holding updateHolding(Long id, Holding holding) {
        logger.debug("Updating holding with id: {}", id);

        Holding existingHolding = getHoldingById(id);

        // Обновляем только количество и среднюю цену
        existingHolding.setQuantity(holding.getQuantity());
        existingHolding.setAveragePrice(holding.getAveragePrice());

        return holdingRepository.save(existingHolding);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"holdings", "holdings::portfolio_*"}, allEntries = true)
    public Holding updateHoldingOnBuy(Long portfolioId, Long stockId, int quantity, BigDecimal price) {
        logger.debug("Updating holding on BUY: portfolio id: {}, stock id: {}, quantity: {}, price: {}",
                portfolioId, stockId, quantity, price);

        // Получаем портфель и акцию
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with id: " + stockId));

        // Проверяем, существует ли уже позиция для этой акции в портфеле
        Holding holding = holdingRepository.findByPortfolioIdAndStockId(portfolioId, stockId)
                .orElse(null);

        if (holding == null) {
            // Создаем новую позицию
            holding = new Holding();
            holding.setPortfolio(portfolio);
            holding.setStock(stock);
            holding.setQuantity(quantity);
            holding.setAveragePrice(price);
        } else {
            // Обновляем существующую позицию
            int newQuantity = holding.getQuantity() + quantity;

            // Рассчитываем новую среднюю цену
            BigDecimal oldValue = holding.getAveragePrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
            BigDecimal newValue = price.multiply(BigDecimal.valueOf(quantity));
            BigDecimal totalValue = oldValue.add(newValue);
            BigDecimal newAveragePrice = totalValue.divide(BigDecimal.valueOf(newQuantity), 4, RoundingMode.HALF_UP);

            holding.setQuantity(newQuantity);
            holding.setAveragePrice(newAveragePrice);
        }

        return holdingRepository.save(holding);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"holdings", "holdings::portfolio_*"}, allEntries = true)
    public Holding updateHoldingOnSell(Long portfolioId, Long stockId, int quantity) {
        logger.debug("Updating holding on SELL: portfolio id: {}, stock id: {}, quantity: {}",
                portfolioId, stockId, quantity);

        // Получаем позицию
        Holding holding = holdingRepository.findByPortfolioIdAndStockId(portfolioId, stockId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Holding not found for portfolio id: " + portfolioId + " and stock id: " + stockId));

        // Проверяем, достаточно ли акций для продажи
        if (holding.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Not enough stocks to sell. Available: " + holding.getQuantity() + ", requested: " + quantity);
        }

        // Обновляем количество
        int newQuantity = holding.getQuantity() - quantity;
        holding.setQuantity(newQuantity);

        // Если количество стало нулевым, удаляем позицию
        if (newQuantity == 0) {
            holdingRepository.delete(holding);
            return null;
        }

        return holdingRepository.save(holding);
    }

    @Override
    @Transactional
    @CacheEvict(value = "holdings", key = "#id")
    public void deleteHolding(Long id) {
        logger.debug("Deleting holding with id: {}", id);

        if (!holdingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Holding not found with id: " + id);
        }

        holdingRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"holdings", "holdings::portfolio_*"}, allEntries = true)
    public void deleteHoldingByPortfolioAndStock(Long portfolioId, Long stockId) {
        logger.debug("Deleting holding for portfolio id: {} and stock id: {}", portfolioId, stockId);

        if (!holdingRepository.existsByPortfolioIdAndStockId(portfolioId, stockId)) {
            throw new ResourceNotFoundException(
                    "Holding not found for portfolio id: " + portfolioId + " and stock id: " + stockId);
        }

        holdingRepository.deleteByPortfolioIdAndStockId(portfolioId, stockId);
    }

    @Override
    public List<Holding> getMostProfitableHoldings(Long portfolioId, int limit) {
        logger.debug("Getting {} most profitable holdings for portfolio id: {}", limit, portfolioId);
        return holdingRepository.findMostProfitableHoldings(portfolioId, PageRequest.of(0, limit));
    }

    @Override
    public List<Holding> getLeastProfitableHoldings(Long portfolioId, int limit) {
        logger.debug("Getting {} least profitable holdings for portfolio id: {}", limit, portfolioId);
        return holdingRepository.findLeastProfitableHoldings(portfolioId, PageRequest.of(0, limit));
    }

    @Override
    public BigDecimal calculateTotalHoldingsValue(Long portfolioId) {
        logger.debug("Calculating total holdings value for portfolio id: {}", portfolioId);
        return holdingRepository.calculateTotalHoldingsValue(portfolioId);
    }

    /**
     * Конвертирует сущность позиции в DTO
     * @param holding Позиция
     * @return DTO позиции
     */
    private HoldingDTO convertToDTO(Holding holding) {
        Stock stock = holding.getStock();

        HoldingDTO dto = new HoldingDTO();
        dto.setId(holding.getId());
        dto.setPortfolioId(holding.getPortfolio().getId());
        dto.setStockId(stock.getId());
        dto.setStockSymbol(stock.getSymbol());
        dto.setStockName(stock.getName());
        dto.setQuantity(holding.getQuantity());
        dto.setAveragePrice(holding.getAveragePrice());
        dto.setCurrentPrice(stock.getCurrentPrice());
        dto.setCurrentValue(holding.getCurrentValue());
        dto.setProfitLoss(holding.getProfitLoss());
        dto.setProfitLossPercent(holding.getProfitLossPercent());

        // Расчет доли позиции в портфеле
        BigDecimal totalValue = holding.getPortfolio().getTotalValue();
        if (totalValue != null && totalValue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal allocationPercent = holding.getCurrentValue()
                    .divide(totalValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            dto.setAllocationPercent(allocationPercent);
        } else {
            dto.setAllocationPercent(BigDecimal.ZERO);
        }

        return dto;
    }
}
