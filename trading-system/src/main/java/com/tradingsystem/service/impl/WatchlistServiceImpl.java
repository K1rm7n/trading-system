package com.tradingsystem.service.impl;

import java.time.LocalDateTime;
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
import com.tradingsystem.model.entity.Stock;
import com.tradingsystem.model.entity.User;
import com.tradingsystem.model.entity.WatchlistItem;
import com.tradingsystem.repository.StockRepository;
import com.tradingsystem.repository.UserRepository;
import com.tradingsystem.repository.WatchlistItemRepository;
import com.tradingsystem.service.interfaces.WatchlistService;

/**
 * Реализация сервиса для работы со списком отслеживаемых акций
 */
@Service
public class WatchlistServiceImpl implements WatchlistService {

    private static final Logger logger = LoggerFactory.getLogger(WatchlistServiceImpl.class);

    private final WatchlistItemRepository watchlistItemRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    @Autowired
    public WatchlistServiceImpl(
            WatchlistItemRepository watchlistItemRepository,
            UserRepository userRepository,
            StockRepository stockRepository) {
        this.watchlistItemRepository = watchlistItemRepository;
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
    }

    @Override
    @Cacheable(value = "watchlist", key = "#userId")
    public List<WatchlistItem> getWatchlistItemsByUserId(Long userId) {
        logger.debug("Getting watchlist items for user id: {}", userId);

        // Проверка существования пользователя
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return watchlistItemRepository.findByUserId(userId);
    }

    @Override
    @Cacheable(value = "watchlist_stocks", key = "#userId")
    public List<Stock> getWatchlistStocks(Long userId) {
        logger.debug("Getting watchlist stocks for user id: {}", userId);

        List<WatchlistItem> watchlistItems = getWatchlistItemsByUserId(userId);

        return watchlistItems.stream()
                .map(WatchlistItem::getStock)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isStockInWatchlist(Long userId, Long stockId) {
        logger.debug("Checking if stock id: {} is in watchlist of user id: {}", stockId, userId);
        return watchlistItemRepository.existsByUserIdAndStockId(userId, stockId);
    }

    @Override
    @Cacheable(value = "watchlist_item", key = "#userId + '_' + #stockId")
    public WatchlistItem getWatchlistItem(Long userId, Long stockId) {
        logger.debug("Getting watchlist item for user id: {} and stock id: {}", userId, stockId);

        return watchlistItemRepository.findByUserIdAndStockId(userId, stockId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "WatchlistItem not found for user id: " + userId + " and stock id: " + stockId));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"watchlist", "watchlist_stocks"}, key = "#watchlistItem.user.id")
    public WatchlistItem addToWatchlist(WatchlistItem watchlistItem) {
        logger.debug("Adding stock id: {} to watchlist of user id: {}",
                watchlistItem.getStock().getId(), watchlistItem.getUser().getId());

        // Проверка, что акция не уже в списке
        if (watchlistItemRepository.existsByUserIdAndStockId(
                watchlistItem.getUser().getId(), watchlistItem.getStock().getId())) {
            throw new IllegalArgumentException("Stock already in watchlist");
        }

        // Установка времени добавления, если не указано
        if (watchlistItem.getAddedAt() == null) {
            watchlistItem.setAddedAt(LocalDateTime.now());
        }

        return watchlistItemRepository.save(watchlistItem);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"watchlist", "watchlist_stocks"}, key = "#userId")
    public WatchlistItem addToWatchlist(Long userId, Long stockId) {
        logger.debug("Adding stock id: {} to watchlist of user id: {}", stockId, userId);

        // Проверка, что акция не уже в списке
        if (watchlistItemRepository.existsByUserIdAndStockId(userId, stockId)) {
            throw new IllegalArgumentException("Stock already in watchlist");
        }

        // Получаем пользователя и акцию
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with id: " + stockId));

        // Создаем новый элемент списка
        WatchlistItem watchlistItem = new WatchlistItem();
        watchlistItem.setUser(user);
        watchlistItem.setStock(stock);
        watchlistItem.setAddedAt(LocalDateTime.now());

        return watchlistItemRepository.save(watchlistItem);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"watchlist", "watchlist_stocks", "watchlist_item"},
            key = {"#userId", "#userId", "#userId + '_' + #stockId"})
    public WatchlistItem updateWatchlistItem(Long userId, Long stockId, String notes, Double priceTarget) {
        logger.debug("Updating watchlist item for user id: {} and stock id: {}", userId, stockId);

        WatchlistItem watchlistItem = getWatchlistItem(userId, stockId);

        watchlistItem.setNotes(notes);
        watchlistItem.setPriceTarget(priceTarget);

        return watchlistItemRepository.save(watchlistItem);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"watchlist", "watchlist_stocks", "watchlist_item"},
            key = {"#userId", "#userId", "#userId + '_' + #stockId"})
    public void removeFromWatchlist(Long userId, Long stockId) {
        logger.debug("Removing stock id: {} from watchlist of user id: {}", stockId, userId);

        // Проверка существования элемента
        if (!watchlistItemRepository.existsByUserIdAndStockId(userId, stockId)) {
            throw new ResourceNotFoundException(
                    "WatchlistItem not found for user id: " + userId + " and stock id: " + stockId);
        }

        watchlistItemRepository.deleteByUserIdAndStockId(userId, stockId);
    }

    @Override
    public long countWatchlistItems(Long userId) {
        logger.debug("Counting watchlist items for user id: {}", userId);

        // Проверка существования пользователя
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return watchlistItemRepository.countByUserId(userId);
    }

    @Override
    @Cacheable(value = "most_watched", key = "#limit")
    public List<Object[]> getMostWatchedStocks(int limit) {
        logger.debug("Getting {} most watched stocks", limit);
        return watchlistItemRepository.findMostWatchedStocks(PageRequest.of(0, limit));
    }

    @Override
    public List<WatchlistItem> getTopGainersInWatchlist(Long userId, int limit) {
        logger.debug("Getting {} top gainers in watchlist for user id: {}", limit, userId);
        return watchlistItemRepository.findTopGainersInWatchlist(userId, PageRequest.of(0, limit));
    }

    @Override
    public List<WatchlistItem> getTopLosersInWatchlist(Long userId, int limit) {
        logger.debug("Getting {} top losers in watchlist for user id: {}", limit, userId);
        return watchlistItemRepository.findTopLosersInWatchlist(userId, PageRequest.of(0, limit));
    }
}
