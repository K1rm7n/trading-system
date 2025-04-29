package com.tradingsystem.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tradingsystem.model.entity.Trade;
import com.tradingsystem.model.enums.TradeType;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    /**
     * Находит все сделки для указанного портфеля
     * @param portfolioId ID портфеля
     * @return Список сделок портфеля
     */
    List<Trade> findByPortfolioId(Long portfolioId);

    /**
     * Находит все сделки для указанного портфеля, отсортированные по дате
     * @param portfolioId ID портфеля
     * @param pageable Объект пагинации и сортировки
     * @return Список сделок портфеля
     */
    List<Trade> findByPortfolioId(Long portfolioId, Pageable pageable);

    /**
     * Находит все сделки с указанной акцией
     * @param stockId ID акции
     * @return Список сделок с акцией
     */
    List<Trade> findByStockId(Long stockId);

    /**
     * Находит все сделки указанного типа
     * @param type Тип сделки (BUY, SELL)
     * @return Список сделок указанного типа
     */
    List<Trade> findByType(TradeType type);

    /**
     * Находит все сделки за указанный период
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Список сделок за период
     */
    List<Trade> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Находит все сделки пользователя
     * @param userId ID пользователя
     * @return Список сделок пользователя
     */
    @Query("SELECT t FROM Trade t JOIN t.portfolio p WHERE p.user.id = :userId")
    List<Trade> findByUserId(@Param("userId") Long userId);

    /**
     * Находит все сделки пользователя, отсортированные по дате
     * @param userId ID пользователя
     * @param pageable Объект пагинации и сортировки
     * @return Список сделок пользователя
     */
    @Query("SELECT t FROM Trade t JOIN t.portfolio p WHERE p.user.id = :userId")
    List<Trade> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Находит все сделки по портфелю и типу
     * @param portfolioId ID портфеля
     * @param type Тип сделки
     * @return Список сделок
     */
    List<Trade> findByPortfolioIdAndType(Long portfolioId, TradeType type);

    /**
     * Находит все сделки по портфелю и акции
     * @param portfolioId ID портфеля
     * @param stockId ID акции
     * @return Список сделок
     */
    List<Trade> findByPortfolioIdAndStockId(Long portfolioId, Long stockId);

    /**
     * Рассчитывает общую сумму покупок для портфеля
     * @param portfolioId ID портфеля
     * @return Общая сумма покупок
     */
    @Query("SELECT SUM(t.price * t.quantity) FROM Trade t WHERE t.portfolio.id = :portfolioId AND t.type = 'BUY'")
    BigDecimal calculateTotalBuyAmount(@Param("portfolioId") Long portfolioId);

    /**
     * Рассчитывает общую сумму продаж для портфеля
     * @param portfolioId ID портфеля
     * @return Общая сумма продаж
     */
    @Query("SELECT SUM(t.price * t.quantity) FROM Trade t WHERE t.portfolio.id = :portfolioId AND t.type = 'SELL'")
    BigDecimal calculateTotalSellAmount(@Param("portfolioId") Long portfolioId);

    /**
     * Находит последние сделки пользователя
     * @param userId ID пользователя
     * @param limit Количество сделок
     * @return Список последних сделок
     */
    @Query("SELECT t FROM Trade t JOIN t.portfolio p WHERE p.user.id = :userId ORDER BY t.timestamp DESC")
    List<Trade> findRecentTradesByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Находит сделки по портфелю, акции и типу
     * @param portfolioId ID портфеля
     * @param stockId ID акции
     * @param type Тип сделки
     * @return Список сделок
     */
    List<Trade> findByPortfolioIdAndStockIdAndType(Long portfolioId, Long stockId, TradeType type);

    /**
     * Рассчитывает общее количество купленных акций в портфеле
     * @param portfolioId ID портфеля
     * @param stockId ID акции
     * @return Количество купленных акций
     */
    @Query("SELECT SUM(t.quantity) FROM Trade t WHERE t.portfolio.id = :portfolioId AND t.stock.id = :stockId AND t.type = 'BUY'")
    Integer calculateTotalBuyQuantity(@Param("portfolioId") Long portfolioId, @Param("stockId") Long stockId);

    /**
     * Рассчитывает общее количество проданных акций в портфеле
     * @param portfolioId ID портфеля
     * @param stockId ID акции
     * @return Количество проданных акций
     */
    @Query("SELECT SUM(t.quantity) FROM Trade t WHERE t.portfolio.id = :portfolioId AND t.stock.id = :stockId AND t.type = 'SELL'")
    Integer calculateTotalSellQuantity(@Param("portfolioId") Long portfolioId, @Param("stockId") Long stockId);
}
