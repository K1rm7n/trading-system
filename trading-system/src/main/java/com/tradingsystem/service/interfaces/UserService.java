package com.tradingsystem.service.interfaces;

import java.util.List;

import com.tradingsystem.model.entity.User;

/**
 * Интерфейс сервиса для работы с пользователями
 */
public interface UserService {

    /**
     * Получает список всех пользователей
     * @return Список всех пользователей
     */
    List<User> getAllUsers();

    /**
     * Получает пользователя по ID
     * @param id ID пользователя
     * @return Пользователь
     */
    User getUserById(Long id);

    /**
     * Получает пользователя по имени пользователя
     * @param username Имя пользователя
     * @return Пользователь
     */
    User getUserByUsername(String username);

    /**
     * Получает пользователя по email
     * @param email Email пользователя
     * @return Пользователь
     */
    User getUserByEmail(String email);

    /**
     * Создает нового пользователя
     * @param user Пользователь для создания
     * @return Созданный пользователь
     */
    User createUser(User user);

    /**
     * Обновляет пользователя
     * @param id ID пользователя
     * @param user Обновленный пользователь
     * @return Обновленный пользователь
     */
    User updateUser(Long id, User user);

    /**
     * Удаляет пользователя
     * @param id ID пользователя
     */
    void deleteUser(Long id);

    /**
     * Проверяет существование пользователя с указанным именем
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
     * Изменяет пароль пользователя
     * @param id ID пользователя
     * @param newPassword Новый пароль
     * @return Обновленный пользователь
     */
    User changePassword(Long id, String newPassword);

    /**
     * Получает количество портфелей пользователя
     * @param userId ID пользователя
     * @return Количество портфелей
     */
    long countPortfolios(Long userId);

    /**
     * Получает общую стоимость всех портфелей пользователя
     * @param userId ID пользователя
     * @return Общая стоимость портфелей
     */
    double getTotalPortfolioValue(Long userId);
}
