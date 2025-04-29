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

import com.tradingsystem.model.enums.TrendType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Сущность аналитических данных по акции
 */
@Entity
@Table(name = "analyses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Акция, для которой создан анализ
     */
    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    /**
     * Тип тренда (UPTREND, DOWNTREND, SIDEWAYS)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrendType trend;

    /**
     * Рекомендация (BUY, SELL, HOLD)
     */
    @Column(length = 10)
    private String recommendation;

    /**
     * Уровень уверенности в рекомендации (от 0 до 1)
     */
    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    /**
     * Дата и время создания анализа
     */
    @Column(name = "analysis_date", nullable = false)
    private LocalDateTime analysisDate;

    /**
     * Обоснование рекомендации
     */
    @Column(columnDefinition = "TEXT")
    private String rationale;
}
