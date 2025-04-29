package com.tradingsystem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tradingsystem.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по имени пользователя
     * @param username Имя пользователя
     * @return Optional с пользователем, если найден
     */
    Optional<User> findByUsername(String username);

    /**
     * Находит пользователя по email
     * @param email Email пользователя
     * @return Optional с пользователем, если найден
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверяет существование пользователя с указанным именем пользователя
     * @param username Имя пользователя
     * @return true, если пользователь существует
     */
    boolean existsByUsername(String username);

    /**
     * Проверяет существование пользователя с указанным email
     * @param email Email пользователя
     * @return true, если пользователь существует
     */
    boolean existsByEmail(String email);

    /**
     * Подсчитывает количество портфелей пользователя
     * @param userId ID пользователя
     * @return Количество портфелей
     */
    @Query("SELECT COUNT(p) FROM Portfolio p WHERE p.user.id = :userId")
    long countPortfoliosByUserId(@Param("userId") Long userId);

    /**
     * Подсчитывает количество сделок пользователя
     * @param userId ID пользователя
     * @return Количество сделок
     */
    @Query("SELECT COUNT(t) FROM Trade t JOIN t.portfolio p WHERE p.user.id = :userId")
    long countTradesByUserId(@Param("userId") Long userId);

    /**
     * Подсчитывает количество акций в списке отслеживания пользователя
     * @param userId ID пользователя
     * @return Количество акций в списке отслеживания
     */
    @Query("SELECT COUNT(w) FROM WatchlistItem w WHERE w.user.id = :userId")
    long countWatchlistItemsByUserId(@Param("userId") Long userId);

    /**
     * Получает общую стоимость всех портфелей пользователя
     * @param userId ID пользователя
     * @return Общая стоимость портфелей
     */
    @Query("SELECT SUM(p.totalValue) FROM Portfolio p WHERE p.user.id = :userId")
    double getTotalPortfolioValueByUserId(@Param("userId") Long userId);
}
