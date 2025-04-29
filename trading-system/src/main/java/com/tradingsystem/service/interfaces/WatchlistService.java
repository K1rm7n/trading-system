package com.tradingsystem.service.interfaces;

import java.util.List;

import com.tradingsystem.model.entity.Stock;
import com.tradingsystem.model.entity.WatchlistItem;

/**
 * Интерфейс сервиса для работы со списком отслеживаемых акций
 */
public interface WatchlistService {

    /**
     * Получает список всех элементов отслеживания пользователя
     * @param userId ID пользователя
     * @return Список элементов отслеживания
     */
    List<WatchlistItem> getWatchlistItemsByUserId(Long userId);

    /**
     * Получает список акций в списке отслеживания пользователя
     * @param userId ID пользователя
     * @return Список акций
     */
    List<Stock> getWatchlistStocks(Long userId);

    /**
     * Проверяет, находится ли акция в списке отслеживания пользователя
     * @param userId ID пользователя
     * @param stockId ID акции
     * @return true, если акция в списке отслеживания
     */
    boolean isStockInWatchlist(Long userId, Long stockId);

    /**
     * Получает элемент отслеживания по пользователю и акции
     * @param userId ID пользователя
     * @param stockId ID акции
     * @return Элемент отслеживания
     */
    WatchlistItem getWatchlistItem(Long userId, Long stockId);

    /**
     * Добавляет акцию в список отслеживания
     * @param watchlistItem Элемент отслеживания для добавления
     * @return Добавленный элемент отслеживания
     */
    WatchlistItem addToWatchlist(WatchlistItem watchlistItem);

    /**
     * Добавляет акцию в список отслеживания
     * @param userId ID пользователя
     * @param stockId ID акции
     * @return Добавленный элемент отслеживания
     */
    WatchlistItem addToWatchlist(Long userId, Long stockId);

    /**
     * Обновляет элемент отслеживания
     * @param userId ID пользователя
     * @param stockId ID акции
     * @param notes Заметки
     * @param priceTarget Целевая цена
     * @return Обновленный элемент отслеживания
     */
    WatchlistItem updateWatchlistItem(Long userId, Long stockId, String notes, Double priceTarget);

    /**
     * Удаляет акцию из списка отслеживания
     * @param userId ID пользователя
     * @param stockId ID акции
     */
    void removeFromWatchlist(Long userId, Long stockId);

    /**
     * Получает количество акций в списке отслеживания пользователя
     * @param userId ID пользователя
     * @return Количество акций
     */
    long countWatchlistItems(Long userId);

    /**
     * Получает список акций, наиболее популярных в списках отслеживания
     * @param limit Количество акций
     * @return Список наиболее популярных акций
     */
    List<Object[]> getMostWatchedStocks(int limit);

    /**
     * Получает акции из списка отслеживания с наибольшим ростом цены
     * @param userId ID пользователя
     * @param limit Количество акций
     * @return Список элементов отслеживания
     */
    List<WatchlistItem> getTopGainersInWatchlist(Long userId, int limit);

    /**
     * Получает акции из списка отслеживания с наибольшим падением цены
     * @param userId ID пользователя
     * @param limit Количество акций
     * @return Список элементов отслеживания
     */
    List<WatchlistItem> getTopLosersInWatchlist(Long userId, int limit);
}
