package com.tradingsystem.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tradingsystem.model.entity.WatchlistItem;

@Repository
public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {

    /**
     * Находит все элементы списка отслеживания пользователя
     * @param userId ID пользователя
     * @return Список элементов наблюдения
     */
    List<WatchlistItem> findByUserId(Long userId);

    /**
     * Находит элемент списка отслеживания по пользователю и акции
     * @param userId ID пользователя
     * @param stockId ID акции
     * @return Optional с элементом, если найден
     */
    Optional<WatchlistItem> findByUserIdAndStockId(Long userId, Long stockId);

    /**
     * Проверяет, существует ли элемент списка отслеживания с указанным пользователем и акцией
     * @param userId ID пользователя
     * @param stockId ID акции
     * @return true, если элемент существует
     */
    boolean existsByUserIdAndStockId(Long userId, Long stockId);

    /**
     * Удаляет элемент списка отслеживания по пользователю и акции
     * @param userId ID пользователя
     * @param stockId ID акции
     * @return Количество удаленных элементов
     */
    long deleteByUserIdAndStockId(Long userId, Long stockId);

    /**
     * Находит элементы списка отслеживания, добавленные после указанной даты
     * @param userId ID пользователя
     * @param date Дата
     * @return Список элементов наблюдения
     */
    List<WatchlistItem> findByUserIdAndAddedAtAfter(Long userId, LocalDateTime date);

    /**
     * Подсчитывает количество элементов в списке отслеживания пользователя
     * @param userId ID пользователя
     * @return Количество элементов
     */
    long countByUserId(Long userId);

    /**
     * Находит наиболее популярные акции в списках отслеживания
     * @param pageable Объект пагинации для ограничения результатов
     * @return Список ID акций и количество пользователей, отслеживающих их
     */
    @Query("SELECT w.stock.id, COUNT(w) as count FROM WatchlistItem w GROUP BY w.stock.id ORDER BY count DESC")
    List<Object[]> findMostWatchedStocks(Pageable pageable);

    /**
     * Находит последние добавленные элементы в список отслеживания пользователя
     * @param userId ID пользователя
     * @param pageable Объект пагинации для ограничения результатов
     * @return Список элементов наблюдения
     */
    @Query("SELECT w FROM WatchlistItem w WHERE w.user.id = :userId ORDER BY w.addedAt DESC")
    List<WatchlistItem> findRecentlyAddedItems(@Param("userId") Long userId, Pageable pageable);

    /**
     * Находит акции из списка отслеживания пользователя с наибольшим ростом цены
     * @param userId ID пользователя
     * @param pageable Объект пагинации для ограничения результатов
     * @return Список элементов наблюдения
     */
    @Query("SELECT w FROM WatchlistItem w JOIN w.stock s WHERE w.user.id = :userId ORDER BY s.dayChangePercent DESC")
    List<WatchlistItem> findTopGainersInWatchlist(@Param("userId") Long userId, Pageable pageable);

    /**
     * Находит акции из списка отслеживания пользователя с наибольшим падением цены
     * @param userId ID пользователя
     * @param pageable Объект пагинации для ограничения результатов
     * @return Список элементов наблюдения
     */
    @Query("SELECT w FROM WatchlistItem w JOIN w.stock s WHERE w.user.id = :userId ORDER BY s.dayChangePercent ASC")
    List<WatchlistItem> findTopLosersInWatchlist(@Param("userId") Long userId, Pageable pageable);

    /**
     * Находит общие акции в списках отслеживания двух пользователей
     * @param userId1 ID первого пользователя
     * @param userId2 ID второго пользователя
     * @return Список элементов наблюдения
     */
    @Query("SELECT w1.stock.id FROM WatchlistItem w1 JOIN WatchlistItem w2 ON w1.stock.id = w2.stock.id " +
            "WHERE w1.user.id = :userId1 AND w2.user.id = :userId2")
    List<Long> findCommonStocksBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
