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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Сущность акции
 */
@Entity
@Table(name = "stocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "symbol"})
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Символ (тикер) акции, уникальный идентификатор на бирже
     */
    @Column(unique = true, nullable = false, length = 20)
    private String symbol;

    /**
     * Название компании
     */
    @Column(nullable = false)
    private String name;

    /**
     * Сектор экономики (например, Technology, Healthcare)
     */
    @Column(length = 100)
    private String sector;

    /**
     * Индустрия (например, Software, Semiconductors)
     */
    @Column(length = 100)
    private String industry;

    /**
     * Текущая цена акции
     */
    @Column(name = "current_price", precision = 19, scale = 4)
    private BigDecimal currentPrice;

    /**
     * Цена закрытия предыдущего торгового дня
     */
    @Column(name = "previous_close", precision = 19, scale = 4)
    private BigDecimal previousClose;

    /**
     * Процент изменения цены за день
     */
    @Column(name = "day_change_percent", precision = 19, scale = 4)
    private BigDecimal dayChangePercent;

    /**
     * Время последнего обновления данных
     */
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    /**
     * Связь с анализами (один ко многим)
     */
    @OneToMany(mappedBy = "stock")
    @ToString.Exclude
    private Set<Analysis> analyses = new HashSet<>();

    /**
     * Связь с торговыми операциями (один ко многим)
     */
    @OneToMany(mappedBy = "stock")
    @ToString.Exclude
    private Set<Trade> trades = new HashSet<>();

    /**
     * Связь с позициями в портфелях (один ко многим)
     */
    @OneToMany(mappedBy = "stock")
    @ToString.Exclude
    private Set<Holding> holdings = new HashSet<>();

    /**
     * Связь с элементами списка отслеживания (один ко многим)
     */
    @OneToMany(mappedBy = "stock")
    @ToString.Exclude
    private Set<WatchlistItem> watchlistItems = new HashSet<>();
}
