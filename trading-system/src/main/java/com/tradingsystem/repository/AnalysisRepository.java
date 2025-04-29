package com.tradingsystem.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tradingsystem.model.entity.Analysis;
import com.tradingsystem.model.entity.Stock;
import com.tradingsystem.model.enums.TrendType;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    /**
     * Находит анализы для указанной акции
     * @param stock Акция
     * @return Список анализов
     */
    List<Analysis> findByStock(Stock stock);

    /**
     * Находит анализы для указанной акции по ID
     * @param stockId ID акции
     * @return Список анализов
     */
    List<Analysis> findByStockId(Long stockId);

    /**
     * Находит анализы по указанному тренду
     * @param trend Тип тренда (UPTREND, DOWNTREND, SIDEWAYS)
     * @return Список анализов
     */
    List<Analysis> findByTrend(TrendType trend);

    /**
     * Находит анализы по рекомендации
     * @param recommendation Рекомендация (BUY, SELL, HOLD)
     * @return Список анализов
     */
    List<Analysis> findByRecommendation(String recommendation);

    /**
     * Находит анализы с уровнем уверенности выше указанного
     * @param confidenceScore Минимальный уровень уверенности
     * @return Список анализов
     */
    List<Analysis> findByConfidenceScoreGreaterThanEqual(BigDecimal confidenceScore);

    /**
     * Находит анализы за указанный период
     * @param startDate Начальная дата
     * @param endDate Конечная дата
     * @return Список анализов
     */
    List<Analysis> findByAnalysisDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Находит последние анализы для указанной акции
     * @param stockId ID акции
     * @param pageable Объект пагинации для ограничения результатов
     * @return Список последних анализов
     */
    @Query("SELECT a FROM Analysis a WHERE a.stock.id = :stockId ORDER BY a.analysisDate DESC")
    List<Analysis> findLatestAnalysesByStockId(@Param("stockId") Long stockId, Pageable pageable);

    /**
     * Находит последние анализы
     * @param pageable Объект пагинации для ограничения результатов
     * @return Список последних анализов
     */
    @Query("SELECT a FROM Analysis a ORDER BY a.analysisDate DESC")
    List<Analysis> findLatestAnalyses(Pageable pageable);

    /**
     * Находит акции с самым высоким уровнем уверенности в рекомендации покупки
     * @param pageable Объект пагинации для ограничения результатов
     * @return Список анализов
     */
    @Query("SELECT a FROM Analysis a WHERE a.recommendation = 'BUY' ORDER BY a.confidenceScore DESC")
    List<Analysis> findMostConfidentBuyRecommendations(Pageable pageable);

    /**
     * Находит акции с самым высоким уровнем уверенности в рекомендации продажи
     * @param pageable Объект пагинации для ограничения результатов
     * @return Список анализов
     */
    @Query("SELECT a FROM Analysis a WHERE a.recommendation = 'SELL' ORDER BY a.confidenceScore DESC")
    List<Analysis> findMostConfidentSellRecommendations(Pageable pageable);

    /**
     * Находит последний анализ для акции
     * @param stockId ID акции
     * @return Последний анализ
     */
    @Query("SELECT a FROM Analysis a WHERE a.stock.id = :stockId ORDER BY a.analysisDate DESC")
    List<Analysis> findLatestAnalysisByStockId(@Param("stockId") Long stockId, Pageable pageable);

    /**
     * Находит анализы по акции и рекомендации
     * @param stockId ID акции
     * @param recommendation Рекомендация
     * @return Список анализов
     */
    List<Analysis> findByStockIdAndRecommendation(Long stockId, String recommendation);

    /**
     * Находит анализы по акции и тренду
     * @param stockId ID акции
     * @param trend Тип тренда
     * @return Список анализов
     */
    List<Analysis> findByStockIdAndTrend(Long stockId, TrendType trend);

    /**
     * Подсчитывает количество анализов для акции
     * @param stockId ID акции
     * @return Количество анализов
     */
    long countByStockId(Long stockId);

    /**
     * Находит анализы после указанной даты
     * @param date Дата
     * @return Список анализов
     */
    List<Analysis> findByAnalysisDateAfter(LocalDateTime date);
}
