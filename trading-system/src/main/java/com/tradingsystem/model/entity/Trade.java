package com.tradingsystem.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.tradingsystem.model.enums.TradeType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Сущность торговой операции (сделки)
 */
@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Портфель, в котором совершена сделка
     */
    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    /**
     * Акция, с которой совершена сделка
     */
    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    /**
     * Тип сделки (BUY, SELL)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeType type;

    /**
     * Количество акций в сделке
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Цена за одну акцию в сделке
     */
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal price;

    /**
     * Дата и время совершения сделки
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;
}