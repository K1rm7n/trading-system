package com.tradingsystem.service.interfaces;

import java.util.List;

import com.tradingsystem.model.dto.PortfolioPerformanceDTO;
import com.tradingsystem.model.entity.Portfolio;

/**
 * Интерфейс сервиса для работы с портфелями
 */
public interface PortfolioService {

    /**
     * Получает список всех портфелей
     * @return Список всех портфелей
     */
    List<Portfolio> getAllPortfolios();

    /**
     * Получает портфель по ID
     * @param id ID портфеля
     * @return Портфель
     */
    Portfolio getPortfolioById(Long id);

    /**
     * Получает список портфелей пользователя
     * @param userId ID пользователя
     * @return Список портфелей пользователя
     */
    List<Portfolio> getPortfoliosByUserId(Long userId);

    /**
     * Создает новый портфель
     * @param portfolio Портфель для создания
     * @return Созданный портфель
     */
    Portfolio createPortfolio(Portfolio portfolio);

    /**
     * Обновляет портфель
     * @param id ID портфеля
     * @param portfolio Обновленный портфель
     * @return Обновленный портфель
     */
    Portfolio updatePortfolio(Long id, Portfolio portfolio);

    /**
     * Удаляет портфель
     * @param id ID портфеля
     */
    void deletePortfolio(Long id);

    /**
     * Рассчитывает метрики эффективности портфеля
     * @param id ID портфеля
     * @return Метрики эффективности
     */
    PortfolioPerformanceDTO calculatePerformance(Long id);

    /**
     * Получает список позиций в портфеле
     * @param id ID портфеля
     * @return Список позиций
     */
    Object getPortfolioHoldings(Long id);

    /**
     * Обновляет стоимость портфеля
     * @param id ID портфеля
     * @return Обновленный портфель
     */
    Portfolio updatePortfolioValue(Long id);

    /**
     * Проверяет, принадлежит ли портфель указанному пользователю
     * @param portfolioId ID портфеля
     * @param userId ID пользователя
     * @return true, если портфель принадлежит пользователю
     */
    boolean isPortfolioOwnedByUser(Long portfolioId, Long userId);
}