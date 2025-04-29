package com.tradingsystem.service.interfaces;

import java.time.LocalDateTime;
import java.util.List;

import com.tradingsystem.model.entity.Trade;

/**
 * Интерфейс сервиса для работы с торговыми операциями
 */
public interface TradeService {

    /**
     * Получает список всех сделок
     * @return Список всех сделок
     */
    List<Trade> getAllTrades();

    /**
     * Получает сделку по ID
     * @param id ID сделки
     * @return Сделка
     */
    Trade getTradeById(Long id);

    /**
     * Получает список сделок пользователя
     * @param userId ID пользователя
     * @return Список сделок пользователя
     */
    List<Trade> getTradesByUserId(Long userId);

    /**
     * Получает список сделок по портфелю
     * @param portfolioId ID портфеля
     * @return Список сделок портфеля
     */
    List<Trade> getTradesByPortfolioId(Long portfolioId);

    /**
     * Получает список сделок по акции
     * @param stockId ID акции
     * @return Список сделок с акцией
     */
    List<Trade> getTradesByStockId(Long stockId);

    /**
     * Получает список сделок за указанный период
     * @param startDate Начальная дата
     * @param endDate Конечная дата
     * @return Список сделок за период
     */
    List<Trade> getTradesByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Создает новую сделку
     * @param trade Сделка для создания
     * @return Созданная сделка
     */
    Trade createTrade(Trade trade);

    /**
     * Удаляет сделку
     * @param id ID сделки
     */
    void deleteTrade(Long id);

    /**
     * Получает последние сделки пользователя
     * @param userId ID пользователя
     * @param limit Количество сделок
     * @return Список последних сделок
     */
    List<Trade> getRecentTradesByUserId(Long userId, int limit);

    /**
     * Проверяет, принадлежит ли сделка указанному пользователю
     * @param tradeId ID сделки
     * @param userId ID пользователя
     * @return true, если сделка принадлежит пользователю
     */
    boolean isTradeOwnedByUser(Long tradeId, Long userId);
}
