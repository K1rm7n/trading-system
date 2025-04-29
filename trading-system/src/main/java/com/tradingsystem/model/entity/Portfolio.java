package com.tradingsystem.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Сущность инвестиционного портфеля
 */
@Entity
@Table(name = "portfolios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Пользователь, которому принадлежит портфель
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Название портфеля
     */
    @Column(nullable = false)
    private String name;

    /**
     * Описание портфеля
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Дата создания портфеля
     */
    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    /**
     * Общая стоимость портфеля
     */
    @Column(name = "total_value", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalValue = BigDecimal.ZERO;

    /**
     * Связь с торговыми операциями (один ко многим)
     */
    @OneToMany(mappedBy = "portfolio")
    @ToString.Exclude
    private Set<Trade> trades = new HashSet<>();

    /**
     * Связь с позициями в портфеле (один ко многим)
     */
    @OneToMany(mappedBy = "portfolio")
    @ToString.Exclude
    private Set<Holding> holdings = new HashSet<>();
}