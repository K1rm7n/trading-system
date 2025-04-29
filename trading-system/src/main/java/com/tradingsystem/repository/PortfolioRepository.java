package com.tradingsystem.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tradingsystem.model.entity.Portfolio;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * Находит все портфели пользователя
     * @param userId ID пользователя
     * @return Список портфелей пользователя
     */
    List<Portfolio> findByUserId(Long userId);

    /**
     * Находит портфели пользователя с указанным названием
     * @param userId ID пользователя
     * @param name Название портфеля
     * @return Список портфелей с указанным названием
     */
    List<Portfolio> findByUserIdAndName(Long userId, String name);

    /**
     * Находит портфели со стоимостью выше указанной
     * @param value Пороговая стоимость
     * @return Список портфелей с общей стоимостью выше указанной
     */
    List<Portfolio> findByTotalValueGreaterThan(BigDecimal value);

    /**
     * Находит портфели со стоимостью ниже указанной
     * @param value Пороговая стоимость
     * @return Список портфелей с общей стоимостью ниже указанной
     */
    List<Portfolio> findByTotalValueLessThan(BigDecimal value);

    /**
     * Получает общую стоимость всех портфелей пользователя
     * @param userId ID пользователя
     * @return Общая стоимость всех портфелей пользователя
     */
    @Query("SELECT SUM(p.totalValue) FROM Portfolio p WHERE p.user.id = :userId")
    BigDecimal getTotalValueByUserId(@Param("userId") Long userId);

    /**
     * Находит наиболее ценный портфель пользователя
     * @param userId ID пользователя
     * @return Портфель с наибольшей стоимостью для данного пользователя
     */
    @Query("SELECT p FROM Portfolio p WHERE p.user.id = :userId ORDER BY p.totalValue DESC")
    List<Portfolio> findMostValuablePortfolioByUserId(@Param("userId") Long userId);

    /**
     * Подсчитывает количество портфелей пользователя
     * @param userId ID пользователя
     * @return Количество портфелей пользователя
     */
    long countByUserId(Long userId);

    /**
     * Проверяет, существует ли портфель с указанным ID у данного пользователя
     * @param id ID портфеля
     * @param userId ID пользователя
     * @return true, если портфель существует у данного пользователя
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * Находит все портфели, содержащие указанную акцию
     * @param stockId ID акции
     * @return Список портфелей, содержащих указанную акцию
     */
    @Query("SELECT DISTINCT p FROM Portfolio p JOIN Holding h ON h.portfolio.id = p.id WHERE h.stock.id = :stockId")
    List<Portfolio> findPortfoliosContainingStock(@Param("stockId") Long stockId);

    /**
     * Находит портфели с названием, содержащим указанную строку
     * @param name Часть названия портфеля
     * @return Список портфелей, в названии которых содержится указанная строка
     */
    List<Portfolio> findByNameContainingIgnoreCase(String name);
}
