package com.tradingsystem.service.interfaces;

import java.util.List;

import com.tradingsystem.model.entity.Analysis;
import com.tradingsystem.model.enums.TrendType;

/**
 * Интерфейс сервиса для работы с аналитическими данными
 */
public interface AnalysisService {

    /**
     * Получает список всех анализов
     * @return Список всех анализов
     */
    List<Analysis> getAllAnalyses();

    /**
     * Получает анализ по ID
     * @param id ID анализа
     * @return Анализ
     */
    Analysis getAnalysisById(Long id);

    /**
     * Получает список анализов для акции
     * @param stockId ID акции
     * @return Список анализов
     */
    List<Analysis> getAnalysesForStock(Long stockId);

    /**
     * Получает список анализов для акции по символу
     * @param symbol Символ акции
     * @return Список анализов
     */
    List<Analysis> getAnalysesForStockBySymbol(String symbol);

    /**
     * Получает список анализов по тренду
     * @param trend Тип тренда
     * @return Список анализов
     */
    List<Analysis> getAnalysesByTrend(TrendType trend);

    /**
     * Получает список анализов по рекомендации
     * @param recommendation Рекомендация
     * @return Список анализов
     */
    List<Analysis> getAnalysesByRecommendation(String recommendation);

    /**
     * Получает последний анализ для акции
     * @param stockId ID акции
     * @return Последний анализ
     */
    Analysis getLatestAnalysisForStock(Long stockId);

    /**
     * Получает последние анализы
     * @param limit Количество анализов
     * @return Список последних анализов
     */
    List<Analysis> getLatestAnalyses(int limit);

    /**
     * Генерирует новый анализ для акции
     * @param symbol Символ акции
     * @return Созданный анализ
     */
    Analysis generateAnalysis(String symbol);

    /**
     * Удаляет анализ
     * @param id ID анализа
     */
    void deleteAnalysis(Long id);

    /**
     * Получает список анализов с наибольшим уровнем уверенности для рекомендации покупки
     * @param limit Количество анализов
     * @return Список анализов
     */
    List<Analysis> getMostConfidentBuyRecommendations(int limit);

    /**
     * Получает список анализов с наибольшим уровнем уверенности для рекомендации продажи
     * @param limit Количество анализов
     * @return Список анализов
     */
    List<Analysis> getMostConfidentSellRecommendations(int limit);
}
