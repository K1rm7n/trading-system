package com.tradingsystem.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Сущность элемента списка отслеживаемых акций
 */
@Entity
@Table(name = "watchlist_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "stock_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "user", "stock"})
public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Пользователь, которому принадлежит элемент списка отслеживания
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Акция, которая отслеживается
     */
    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    /**
     * Дата добавления акции в список отслеживания
     */
    @Column(name = "added_at")
    private LocalDateTime addedAt;

    /**
     * Заметки пользователя по акции
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Целевая цена, установленная пользователем
     */
    @Column(name = "price_target")
    private Double priceTarget;
}
