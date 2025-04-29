package com.tradingsystem.service.interfaces;

import java.math.BigDecimal;
import java.util.List;

import com.tradingsystem.model.dto.HoldingDTO;
import com.tradingsystem.model.entity.Holding;

/**
 * Интерфейс сервиса для работы с позициями в портфеле
 */
public interface HoldingService {

    /**
     * Получает список всех позиций
     * @return Список всех позиций
     */
    List<Holding> getAllHoldings();

    /**
     * Получает позицию по ID
     * @param id ID позиции
     * @return Позиция
     */
    Holding getHoldingById(Long id);

    /**
     * Получает список позиций в портфеле
     * @param portfolioId ID портфеля
     * @return Список позиций
     */
    List<Holding> getHoldingsByPortfolioId(Long portfolioId);

    /**
     * Получает список позиций для конвертации в DTO
     * @param portfolioId ID портфеля
     * @return Список DTO позиций
     */
    List<HoldingDTO> getHoldingDTOsByPortfolioId(Long portfolioId);

    /**
     * Получает позицию по портфелю и акции
     * @param portfolioId ID портфеля
     * @param stockId ID акции
     * @return Позиция
     */
    Holding getHoldingByPortfolioAndStock(Long portfolioId, Long stockId);

    /**
     * Создает новую позицию
     * @param holding Позиция для создания
     * @return Созданная позиция
     */
    Holding createHolding(Holding holding);

    /**
     * Обновляет позицию
     * @param id ID позиции
     * @param holding Обновленная позиция
     * @return Обновленная позиция
     */
    Holding updateHolding(Long id, Holding holding);

    /**
     * Обновляет позицию при покупке акций
     * @param portfolioId ID портфеля
     * @param stockId ID акции
     * @param quantity Количество акций
     * @param price Цена за акцию
     * @return Обновленная позиция
     */
    Holding updateHoldingOnBuy(Long portfolioId, Long stockId, int quantity, BigDecimal price);

    /**
     * Обновляет позицию при продаже акций
     * @param portfolioId ID портфеля
     * @param stockId ID акции
     * @param quantity Количество акций
     * @return Обновленная позиция
     */
    Holding updateHoldingOnSell(Long portfolioId, Long stockId, int quantity);

    /**
     * Удаляет позицию
     * @param id ID позиции
     */
    void deleteHolding(Long id);

    /**
     * Удаляет позицию по портфелю и акции
     * @param portfolioId ID портфеля
     * @param stockId ID акции
     */
    void deleteHoldingByPortfolioAndStock(Long portfolioId, Long stockId);

    /**
     * Получает наиболее прибыльные позиции в портфеле
     * @param portfolioId ID портфеля
     * @param limit Количество позиций
     * @return Список наиболее прибыльных позиций
     */
    List<Holding> getMostProfitableHoldings(Long portfolioId, int limit);

    /**
     * Получает наименее прибыльные позиции в портфеле
     * @param portfolioId ID портфеля
     * @param limit Количество позиций
     * @return Список наименее прибыльных позиций
     */
    List<Holding> getLeastProfitableHoldings(Long portfolioId, int limit);

    /**
     * Рассчитывает общую стоимость всех позиций в портфеле
     * @param portfolioId ID портфеля
     * @return Общая стоимость позиций
     */
    BigDecimal calculateTotalHoldingsValue(Long portfolioId);
}