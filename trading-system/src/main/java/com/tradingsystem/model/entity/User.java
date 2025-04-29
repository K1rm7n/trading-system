package com.tradingsystem.model.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Сущность пользователя системы
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "username", "email"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя пользователя (логин)
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Email пользователя
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Хеш пароля
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Дата регистрации
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Дата последнего обновления
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Связь с портфелями (один ко многим)
     */
    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private Set<Portfolio> portfolios = new HashSet<>();

    /**
     * Связь с элементами списка отслеживания (один ко многим)
     */
    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private Set<WatchlistItem> watchlistItems = new HashSet<>();
}
