package com.tradingsystem.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingsystem.exception.ResourceNotFoundException;
import com.tradingsystem.model.entity.User;
import com.tradingsystem.repository.UserRepository;
import com.tradingsystem.service.interfaces.UserService;

/**
 * Реализация сервиса для работы с пользователями
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Cacheable(value = "users")
    public List<User> getAllUsers() {
        logger.debug("Getting all users");
        return userRepository.findAll();
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        logger.debug("Getting user with id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    @Cacheable(value = "users", key = "#username")
    public User getUserByUsername(String username) {
        logger.debug("Getting user with username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    @Cacheable(value = "users", key = "#email")
    public User getUserByEmail(String email) {
        logger.debug("Getting user with email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public User createUser(User user) {
        logger.debug("Creating new user: {}", user.getUsername());

        // Проверка уникальности имени пользователя
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username is already taken: " + user.getUsername());
        }

        // Проверка уникальности email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already in use: " + user.getEmail());
        }

        // Хеширование пароля
        if (user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        // Установка времени создания, если не указано
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public User updateUser(Long id, User user) {
        logger.debug("Updating user with id: {}", id);

        User existingUser = getUserById(id);

        // Обновляем только разрешенные поля
        existingUser.setEmail(user.getEmail());

        // Обновление имени пользователя только если оно изменилось и не занято
        if (!existingUser.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new IllegalArgumentException("Username is already taken: " + user.getUsername());
            }
            existingUser.setUsername(user.getUsername());
        }

        // Обновление email только если он изменился и не занят
        if (!existingUser.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("Email is already in use: " + user.getEmail());
            }
            existingUser.setEmail(user.getEmail());
        }

        existingUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        logger.debug("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        logger.debug("Checking if user exists with username: {}", username);
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        logger.debug("Checking if user exists with email: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public User changePassword(Long id, String newPassword) {
        logger.debug("Changing password for user with id: {}", id);

        User user = getUserById(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public long countPortfolios(Long userId) {
        logger.debug("Counting portfolios for user with id: {}", userId);

        // Проверка существования пользователя
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return userRepository.countPortfoliosByUserId(userId);
    }

    @Override
    public double getTotalPortfolioValue(Long userId) {
        logger.debug("Getting total portfolio value for user with id: {}", userId);

        // Проверка существования пользователя
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return userRepository.getTotalPortfolioValueByUserId(userId);
    }
}
