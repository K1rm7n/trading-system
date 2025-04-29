package com.tradingsystem.service.interfaces;

import java.util.List;

import com.tradingsystem.model.entity.Analysis;
import com.tradingsystem.model.entity.Stock;

/**
 * Интерфейс сервиса для работы с акциями
 */
public interface StockService {

    /**
     * Получает список всех акций
     * @return Список всех акций
     */
    List<Stock> getAllStocks();

    /**
     * Получает акцию по ID
     * @param id ID акции
     * @return Акция
     */
    Stock getStockById(Long id);

    /**
     * Получает акцию по символу (тикеру)
     * @param symbol Символ акции
     * @return Акция
     */
    Stock getStockBySymbol(String symbol);

    /**
     * Добавляет новую акцию
     * @param stock Акция для добавления
     * @return Добавленная акция
     */
    Stock addStock(Stock stock);

    /**
     * Обновляет данные по акции
     * @param symbol Символ акции
     * @return Обновленная акция
     */
    Stock updateStockData(String symbol);

    /**
     * Поиск акций по различным критериям
     * @param sector Сектор (может быть null)
     * @param industry Индустрия (может быть null)
     * @param minChangePercent Минимальный процент изменения (может быть null)
     * @param maxChangePercent Максимальный процент изменения (может быть null)
     * @return Список акций, соответствующих критериям
     */
    List<Stock> searchStocks(String sector, String industry, Double minChangePercent, Double maxChangePercent);

    /**
     * Получает список трендовых акций (с наибольшим изменением цены)
     * @param limit Количество акций
     * @return Список трендовых акций
     */
    List<Stock> getTrendingStocks(int limit);

    /**
     * Получает список акций со значительным изменением цены
     * @param threshold Пороговое значение (в процентах)
     * @return Список акций со значительным изменением
     */
    List<Stock> getStocksWithSignificantChanges(double threshold);

    /**
     * Получает список всех секторов
     * @return Список секторов
     */
    List<String> getAllSectors();

    /**
     * Получает список всех индустрий в указанном секторе
     * @param sector Сектор
     * @return Список индустрий
     */
    List<String> getIndustriesBySector(String sector);

    /**
     * Генерирует анализ для указанной акции
     * @param symbol Символ акции
     * @return Созданный анализ
     */
    Analysis generateAnalysis(String symbol);

    /**
     * Удаляет акцию по ID
     * @param id ID акции
     */
    void deleteStock(Long id);
}
