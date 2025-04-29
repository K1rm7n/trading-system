package com.tradingsystem.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tradingsystem.model.entity.Holding;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    /**
     * Находит все позиции в портфеле
     * @param portfolioId ID портфеля
     * @return Список позиций в портфеле
     */
    List<Holding> findByPortfolioId(Long portfolioId);

    /**
     * Находит все позиции по акции
     * @param stockId ID акции
     * @return Список позиций с указанной акцией
     */
    List<Holding> findByStockId(Long stockId);

    /**
     * Находит позицию по портфелю и акции
     * @param portfolioId ID портфеля
     * @param stockId ID акции
     * @return Optional с позицией, если найдена
     */
    Optional<Holding> findByPortfolioIdAndStockId(Long portfolioId, Long stockId);

    /**
     * Находит позиции с количеством больше указанного
     * @param quantity Количество
     * @return Список позиций
     */
    List<Holding> findByQuantityGreaterThan(int quantity);

    /**
     * Находит позиции со средней ценой больше указанной
     * @param averagePrice Средняя цена
     * @return Список позиций
     */
    List<Holding> findByAveragePriceGreaterThan(BigDecimal averagePrice);

    /**
     * Находит позиции со средней ценой меньше указанной
     * @param averagePrice Средняя цена
     * @return Список позиций
     */
    List<Holding> findByAveragePriceLessThan(BigDecimal averagePrice);

    /**
     * Удаляет позицию по портфелю и акции
     * @param portfolioId ID портфеля
     * @param stockId ID акции
     */
    void deleteByPortfolioIdAndStockId(Long portfolioId, Long stockId);

    /**
     * Проверяет, существует ли позиция с указанной акцией в портфеле
     * @param portfolioId ID портфеля
     * @param stockId ID акции
     * @return true, если позиция существует
     */
    boolean existsByPortfolioIdAndStockId(Long portfolioId, Long stockId);

    /**
     * Получает общее количество позиций в портфеле
     * @param portfolioId ID портфеля
     * @return Количество позиций
     */
    long countByPortfolioId(Long portfolioId);

    /**
     * Получает общее количество портфелей, содержащих указанную акцию
     * @param stockId ID акции
     * @return Количество портфелей
     */
    long countByStockId(Long stockId);

    /**
     * Получает общую стоимость позиций в портфеле
     * @param portfolioId ID портфеля
     * @return Общая стоимость позиций
     */
    @Query("SELECT SUM(h.quantity * s.currentPrice) FROM Holding h JOIN h.stock s WHERE h.portfolio.id = :portfolioId")
    BigDecimal calculateTotalHoldingsValue(@Param("portfolioId") Long portfolioId);

    /**
     * Находит позиции с наибольшим количеством в портфеле
     * @param portfolioId ID портфеля
     * @param limit Ограничение количества результатов
     * @return Список позиций
     */
    @Query("SELECT h FROM Holding h WHERE h.portfolio.id = :portfolioId ORDER BY h.quantity DESC")
    List<Holding> findLargestHoldingsByQuantity(@Param("portfolioId") Long portfolioId, Pageable pageable);

    /**
     * Находит позиции с наибольшей стоимостью в портфеле
     * @param portfolioId ID портфеля
     * @param limit Ограничение количества результатов
     * @return Список позиций
     */
    @Query("SELECT h FROM Holding h JOIN h.stock s WHERE h.portfolio.id = :portfolioId ORDER BY (h.quantity * s.currentPrice) DESC")
    List<Holding> findLargestHoldingsByValue(@Param("portfolioId") Long portfolioId, Pageable pageable);

    /**
     * Находит позиции с наибольшей доходностью в портфеле
     * @param portfolioId ID портфеля
     * @return Список позиций
     */
    @Query("SELECT h FROM Holding h JOIN h.stock s WHERE h.portfolio.id = :portfolioId ORDER BY ((s.currentPrice - h.averagePrice) / h.averagePrice) DESC")
    List<Holding> findMostProfitableHoldings(@Param("portfolioId") Long portfolioId, Pageable pageable);

    /**
     * Находит позиции с наибольшими убытками в портфеле
     * @param portfolioId ID портфеля
     * @return Список позиций
     */
    @Query("SELECT h FROM Holding h JOIN h.stock s WHERE h.portfolio.id = :portfolioId ORDER BY ((s.currentPrice - h.averagePrice) / h.averagePrice) ASC")
    List<Holding> findLeastProfitableHoldings(@Param("portfolioId") Long portfolioId, Pageable pageable);

    /**
     * Рассчитывает общую прибыль/убыток по всем позициям в портфеле
     * @param portfolioId ID портфеля
     * @return Сумма прибыли/убытка
     */
    @Query("SELECT SUM(h.quantity * (s.currentPrice - h.averagePrice)) FROM Holding h JOIN h.stock s WHERE h.portfolio.id = :portfolioId")
    BigDecimal calculateTotalProfitLoss(@Param("portfolioId") Long portfolioId);
}
