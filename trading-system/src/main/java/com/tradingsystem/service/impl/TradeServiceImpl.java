package com.tradingsystem.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingsystem.exception.ResourceNotFoundException;
import com.tradingsystem.model.entity.Portfolio;
import com.tradingsystem.model.entity.Stock;
import com.tradingsystem.model.entity.Trade;
import com.tradingsystem.model.enums.TradeType;
import com.tradingsystem.repository.PortfolioRepository;
import com.tradingsystem.repository.TradeRepository;
import com.tradingsystem.service.interfaces.HoldingService;
import com.tradingsystem.service.interfaces.PortfolioService;
import com.tradingsystem.service.interfaces.TradeService;

/**
 * Реализация сервиса для работы с торговыми операциями
 */
@Service
public class TradeServiceImpl implements TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeServiceImpl.class);

    private final TradeRepository tradeRepository;
    private final PortfolioRepository portfolioRepository;
    private final HoldingService holdingService;
    private final PortfolioService portfolioService;

    @Autowired
    public TradeServiceImpl(
            TradeRepository tradeRepository,
            PortfolioRepository portfolioRepository,
            HoldingService holdingService,
            PortfolioService portfolioService) {
        this.tradeRepository = tradeRepository;
        this.portfolioRepository = portfolioRepository;
        this.holdingService = holdingService;
        this.portfolioService = portfolioService;
    }

    @Override
    public List<Trade> getAllTrades() {
        logger.debug("Getting all trades");
        return tradeRepository.findAll();
    }

    @Override
    public Trade getTradeById(Long id) {
        logger.debug("Getting trade with id: {}", id);
        return tradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade not found with id: " + id));
    }

    @Override
    public List<Trade> getTradesByUserId(Long userId) {
        logger.debug("Getting trades for user with id: {}", userId);
        return tradeRepository.findByUserId(userId);
    }

    @Override
    public List<Trade> getTradesByPortfolioId(Long portfolioId) {
        logger.debug("Getting trades for portfolio with id: {}", portfolioId);
        return tradeRepository.findByPortfolioId(portfolioId);
    }

    @Override
    public List<Trade> getTradesByStockId(Long stockId) {
        logger.debug("Getting trades for stock with id: {}", stockId);
        return tradeRepository.findByStockId(stockId);
    }

    @Override
    public List<Trade> getTradesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Getting trades between {} and {}", startDate, endDate);
        return tradeRepository.findByTimestampBetween(startDate, endDate);
    }

    @Override
    @Transactional
    public Trade createTrade(Trade trade) {
        logger.debug("Creating new trade for stock {} in portfolio {}",
                trade.getStock().getSymbol(), trade.getPortfolio().getId());

        Portfolio portfolio = trade.getPortfolio();
        Stock stock = trade.getStock();

        // Проверка достаточного количества акций при продаже
        if (trade.getType() == TradeType.SELL) {
            validateSellOperation(portfolio.getId(), stock.getId(), trade.getQuantity());
        }

        // Если не указано время сделки, устанавливаем текущее время
        if (trade.getTimestamp() == null) {
            trade.setTimestamp(LocalDateTime.now());
        }

        // Сохраняем сделку
        Trade savedTrade = tradeRepository.save(trade);

        // Обновляем позицию в портфеле
        if (trade.getType() == TradeType.BUY) {
            holdingService.updateHoldingOnBuy(
                    portfolio.getId(),
                    stock.getId(),
                    trade.getQuantity(),
                    trade.getPrice());
        } else {
            holdingService.updateHoldingOnSell(
                    portfolio.getId(),
                    stock.getId(),
                    trade.getQuantity());
        }

        // Обновляем общую стоимость портфеля
        portfolioService.updatePortfolioValue(portfolio.getId());

        return savedTrade;
    }

    @Override
    @Transactional
    public void deleteTrade(Long id) {
        logger.debug("Deleting trade with id: {}", id);

        Trade trade = getTradeById(id);
        Long portfolioId = trade.getPortfolio().getId();
        Long stockId = trade.getStock().getId();

        // Для упрощения: разрешаем удаление только последней сделки с акцией в портфеле
        List<Trade> trades = tradeRepository.findByPortfolioIdAndStockId(portfolioId, stockId);
        trades.sort((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp())); // По убыванию

        if (!trades.isEmpty() && !trades.get(0).getId().equals(id)) {
            throw new IllegalStateException("Can only delete the most recent trade for a stock in a portfolio");
        }

        // Получаем обратный тип сделки
        TradeType reverseType = (trade.getType() == TradeType.BUY) ? TradeType.SELL : TradeType.BUY;

        // Создаем "обратную" сделку для коррекции позиции
        Trade reverseTrade = new Trade();
        reverseTrade.setPortfolio(trade.getPortfolio());
        reverseTrade.setStock(trade.getStock());
        reverseTrade.setType(reverseType);
        reverseTrade.setQuantity(trade.getQuantity());
        reverseTrade.setPrice(trade.getPrice());
        reverseTrade.setTimestamp(LocalDateTime.now());

        // Обновляем позицию и портфель
        if (reverseType == TradeType.BUY) {
            holdingService.updateHoldingOnBuy(portfolioId, stockId, trade.getQuantity(), trade.getPrice());
        } else {
            holdingService.updateHoldingOnSell(portfolioId, stockId, trade.getQuantity());
        }

        // Удаляем сделку
        tradeRepository.deleteById(id);

        // Обновляем общую стоимость портфеля
        portfolioService.updatePortfolioValue(portfolioId);
    }

    @Override
    public List<Trade> getRecentTradesByUserId(Long userId, int limit) {
        logger.debug("Getting {} recent trades for user with id: {}", limit, userId);
        return tradeRepository.findRecentTradesByUserId(userId, PageRequest.of(0, limit));
    }

    @Override
    public boolean isTradeOwnedByUser(Long tradeId, Long userId) {
        logger.debug("Checking if trade with id: {} is owned by user with id: {}", tradeId, userId);

        Trade trade = getTradeById(tradeId);
        return trade.getPortfolio().getUser().getId().equals(userId);
    }

    /**
     * Проверяет, достаточно ли акций для продажи
     * @param portfolioId ID портфеля
     * @param stockId ID акции
     * @param quantity Количество для продажи
     * @throws IllegalStateException если недостаточно акций
     */
    private void validateSellOperation(Long portfolioId, Long stockId, int quantity) {
        // Получаем текущее количество акций в портфеле
        Integer buyQuantity = tradeRepository.calculateTotalBuyQuantity(portfolioId, stockId);
        Integer sellQuantity = tradeRepository.calculateTotalSellQuantity(portfolioId, stockId);

        if (buyQuantity == null) {
            buyQuantity = 0;
        }

        if (sellQuantity == null) {
            sellQuantity = 0;
        }

        int availableQuantity = buyQuantity - sellQuantity;

        if (quantity > availableQuantity) {
            throw new IllegalStateException(
                    "Not enough stocks to sell. Available: " + availableQuantity + ", requested: " + quantity);
        }
    }
}
