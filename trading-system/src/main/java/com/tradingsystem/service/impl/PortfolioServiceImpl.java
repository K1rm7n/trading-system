package com.tradingsystem.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.tradingsystem.model.dto.HoldingDTO;
import com.tradingsystem.model.dto.PortfolioPerformanceDTO;
import com.tradingsystem.model.dto.PortfolioPerformanceDTO.HoldingPerformanceDTO;
import com.tradingsystem.model.dto.PortfolioPerformanceDTO.PortfolioValueDataPoint;
import com.tradingsystem.model.entity.Holding;
import com.tradingsystem.model.entity.Portfolio;
import com.tradingsystem.model.entity.Stock;
import com.tradingsystem.model.entity.Trade;
import com.tradingsystem.repository.HoldingRepository;
import com.tradingsystem.repository.PortfolioRepository;
import com.tradingsystem.repository.TradeRepository;
import com.tradingsystem.service.interfaces.HoldingService;
import com.tradingsystem.service.interfaces.PortfolioService;

/**
 * Реализация сервиса для работы с портфелями
 */
@Service
public class PortfolioServiceImpl implements PortfolioService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioServiceImpl.class);

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final TradeRepository tradeRepository;
    private final HoldingService holdingService;

    @Autowired
    public PortfolioServiceImpl(
            PortfolioRepository portfolioRepository,
            HoldingRepository holdingRepository,
            TradeRepository tradeRepository,
            HoldingService holdingService) {
        this.portfolioRepository = portfolioRepository;
        this.holdingRepository = holdingRepository;
        this.tradeRepository = tradeRepository;
        this.holdingService = holdingService;
    }

    @Override
    @Cacheable(value = "portfolios")
    public List<Portfolio> getAllPortfolios() {
        logger.debug("Getting all portfolios");
        return portfolioRepository.findAll();
    }

    @Override
    @Cacheable(value = "portfolios", key = "#id")
    public Portfolio getPortfolioById(Long id) {
        logger.debug("Getting portfolio with id: {}", id);
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + id));
    }

    @Override
    @Cacheable(value = "portfolios", key = "'user_' + #userId")
    public List<Portfolio> getPortfoliosByUserId(Long userId) {
        logger.debug("Getting portfolios for user with id: {}", userId);
        return portfolioRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Portfolio createPortfolio(Portfolio portfolio) {
        logger.debug("Creating new portfolio: {}", portfolio.getName());

        if (portfolio.getCreationDate() == null) {
            portfolio.setCreationDate(LocalDateTime.now());
        }

        if (portfolio.getTotalValue() == null) {
            portfolio.setTotalValue(BigDecimal.ZERO);
        }

        return portfolioRepository.save(portfolio);
    }

    @Override
    @Transactional
    @CacheEvict(value = "portfolios", key = "#id")
    public Portfolio updatePortfolio(Long id, Portfolio portfolio) {
        logger.debug("Updating portfolio with id: {}", id);

        Portfolio existingPortfolio = getPortfolioById(id);

        existingPortfolio.setName(portfolio.getName());
        existingPortfolio.setDescription(portfolio.getDescription());

        // Не обновляем пользователя и дату создания
        // Не обновляем напрямую общую стоимость - она рассчитывается

        return portfolioRepository.save(existingPortfolio);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"portfolios", "portfolios::user_*"}, allEntries = true)
    public void deletePortfolio(Long id) {
        logger.debug("Deleting portfolio with id: {}", id);
        Portfolio portfolio = getPortfolioById(id);

        // Удаляем все связанные сущности
        portfolioRepository.delete(portfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioPerformanceDTO calculatePerformance(Long id) {
        logger.debug("Calculating performance for portfolio with id: {}", id);

        Portfolio portfolio = getPortfolioById(id);
        List<Holding> holdings = holdingRepository.findByPortfolioId(id);
        List<Trade> trades = tradeRepository.findByPortfolioId(id);

        // Инициализируем DTO
        PortfolioPerformanceDTO performanceDTO = new PortfolioPerformanceDTO();
        performanceDTO.setPortfolioId(id);
        performanceDTO.setCurrentValue(portfolio.getTotalValue());

        // Рассчитываем общую сумму инвестиций
        BigDecimal totalBuy = tradeRepository.calculateTotalBuyAmount(id);
        BigDecimal totalSell = tradeRepository.calculateTotalSellAmount(id);
        BigDecimal totalInvested = totalBuy.subtract(totalSell);
        performanceDTO.setTotalInvested(totalInvested);

        // Рассчитываем прибыль/убыток
        BigDecimal profitLoss = portfolio.getTotalValue().subtract(totalInvested);
        performanceDTO.setProfitLoss(profitLoss);

        // Рассчитываем процент прибыли/убытка
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profitLossPercent = profitLoss
                    .divide(totalInvested, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            performanceDTO.setProfitLossPercent(profitLossPercent);
        } else {
            performanceDTO.setProfitLossPercent(BigDecimal.ZERO);
        }

        // Рассчитываем распределение активов по секторам
        Map<String, BigDecimal> sectorAllocation = calculateSectorAllocation(holdings);
        performanceDTO.setSectorAllocation(sectorAllocation);

        // Рассчитываем распределение активов по акциям
        Map<String, BigDecimal> stockAllocation = calculateStockAllocation(holdings);
        performanceDTO.setStockAllocation(stockAllocation);

        // Генерируем данные для графика стоимости портфеля
        List<PortfolioValueDataPoint> historicalValues = generateHistoricalValues(trades);
        performanceDTO.setHistoricalValues(historicalValues);

        // Получаем топ прибыльных позиций
        List<Holding> topGainers = holdingService.getMostProfitableHoldings(id, 5);
        List<HoldingPerformanceDTO> topGainerDTOs = convertToHoldingPerformanceDTOs(topGainers);
        performanceDTO.setTopGainers(topGainerDTOs);

        // Получаем топ убыточных позиций
        List<Holding> topLosers = holdingService.getLeastProfitableHoldings(id, 5);
        List<HoldingPerformanceDTO> topLoserDTOs = convertToHoldingPerformanceDTOs(topLosers);
        performanceDTO.setTopLosers(topLoserDTOs);

        return performanceDTO;
    }

    @Override
    public Object getPortfolioHoldings(Long id) {
        logger.debug("Getting holdings for portfolio with id: {}", id);

        // Получаем портфель
        getPortfolioById(id); // Проверка существования

        // Получаем и конвертируем холдинги
        return holdingService.getHoldingDTOsByPortfolioId(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "portfolios", key = "#id")
    public Portfolio updatePortfolioValue(Long id) {
        logger.debug("Updating total value for portfolio with id: {}", id);

        Portfolio portfolio = getPortfolioById(id);

        // Рассчитываем общую стоимость всех позиций
        BigDecimal totalValue = holdingRepository.calculateTotalHoldingsValue(id);

        if (totalValue == null) {
            totalValue = BigDecimal.ZERO;
        }

        portfolio.setTotalValue(totalValue);

        return portfolioRepository.save(portfolio);
    }

    @Override
    public boolean isPortfolioOwnedByUser(Long portfolioId, Long userId) {
        logger.debug("Checking if portfolio with id: {} is owned by user with id: {}", portfolioId, userId);
        return portfolioRepository.existsByIdAndUserId(portfolioId, userId);
    }

    /**
     * Рассчитывает распределение активов по секторам
     * @param holdings Список позиций
     * @return Карта сектор -> процент
     */
    private Map<String, BigDecimal> calculateSectorAllocation(List<Holding> holdings) {
        Map<String, BigDecimal> sectorValues = new HashMap<>();
        BigDecimal totalValue = BigDecimal.ZERO;

        // Считаем стоимость по секторам
        for (Holding holding : holdings) {
            Stock stock = holding.getStock();
            if (stock.getSector() == null) {
                continue;
            }

            BigDecimal value = holding.getCurrentValue();
            totalValue = totalValue.add(value);

            sectorValues.merge(stock.getSector(), value, BigDecimal::add);
        }

        // Рассчитываем проценты
        Map<String, BigDecimal> sectorAllocation = new HashMap<>();
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            for (Map.Entry<String, BigDecimal> entry : sectorValues.entrySet()) {
                BigDecimal percentage = entry.getValue()
                        .divide(totalValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                sectorAllocation.put(entry.getKey(), percentage);
            }
        }

        return sectorAllocation;
    }

    /**
     * Рассчитывает распределение активов по акциям
     * @param holdings Список позиций
     * @return Карта символ акции -> процент
     */
    private Map<String, BigDecimal> calculateStockAllocation(List<Holding> holdings) {
        Map<String, BigDecimal> stockValues = new HashMap<>();
        BigDecimal totalValue = BigDecimal.ZERO;

        // Считаем стоимость по акциям
        for (Holding holding : holdings) {
            Stock stock = holding.getStock();
            BigDecimal value = holding.getCurrentValue();
            totalValue = totalValue.add(value);

            stockValues.put(stock.getSymbol(), value);
        }

        // Рассчитываем проценты
        Map<String, BigDecimal> stockAllocation = new HashMap<>();
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            for (Map.Entry<String, BigDecimal> entry : stockValues.entrySet()) {
                BigDecimal percentage = entry.getValue()
                        .divide(totalValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                stockAllocation.put(entry.getKey(), percentage);
            }
        }

        return stockAllocation;
    }

    /**
     * Генерирует исторические данные о стоимости портфеля
     * @param trades Список сделок
     * @return Список точек данных
     */
    private List<PortfolioValueDataPoint> generateHistoricalValues(List<Trade> trades) {
        // В реальном приложении эти данные могут храниться в отдельной таблице
        // Для примера мы генерируем их на основе сделок

        // Сортируем сделки по дате
        trades = trades.stream()
                .sorted((t1, t2) -> t1.getTimestamp().compareTo(t2.getTimestamp()))
                .collect(Collectors.toList());

        List<PortfolioValueDataPoint> result = new ArrayList<>();
        BigDecimal runningValue = BigDecimal.ZERO;

        for (Trade trade : trades) {
            BigDecimal tradeValue = trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));

            if (trade.getType().name().equals("BUY")) {
                runningValue = runningValue.add(tradeValue);
            } else {
                runningValue = runningValue.subtract(tradeValue);
            }

            String date = trade.getTimestamp().toLocalDate().toString();

            // Проверяем, есть ли уже запись на эту дату
            boolean dateExists = result.stream()
                    .anyMatch(dp -> dp.getDate().equals(date));

            if (dateExists) {
                // Обновляем существующую запись
                result.stream()
                        .filter(dp -> dp.getDate().equals(date))
                        .findFirst()
                        .ifPresent(dp -> dp.setValue(runningValue));
            } else {
                // Добавляем новую запись
                result.add(new PortfolioValueDataPoint(date, runningValue));
            }
        }

        return result;
    }

    /**
     * Конвертирует позиции в DTO производительности
     * @param holdings Список позиций
     * @return Список DTO
     */
    private List<HoldingPerformanceDTO> convertToHoldingPerformanceDTOs(List<Holding> holdings) {
        return holdings.stream()
                .map(holding -> {
                    Stock stock = holding.getStock();
                    HoldingPerformanceDTO dto = new HoldingPerformanceDTO();
                    dto.setSymbol(stock.getSymbol());
                    dto.setName(stock.getName());
                    dto.setQuantity(holding.getQuantity());
                    dto.setCurrentValue(holding.getCurrentValue());
                    dto.setProfitLoss(holding.getProfitLoss());
                    dto.setProfitLossPercent(holding.getProfitLossPercent());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
