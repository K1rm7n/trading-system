package com.tradingsystem.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tradingsystem.model.entity.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * Находит акцию по символу (тикеру)
     * @param symbol Символ акции
     * @return Optional с акцией, если найдена
     */
    Optional<Stock> findBySymbol(String symbol);

    /**
     * Находит акции по сектору
     * @param sector Сектор (например, "Technology", "Healthcare")
     * @return Список акций в указанном секторе
     */
    List<Stock> findBySector(String sector);

    /**
     * Находит акции по индустрии
     * @param industry Индустрия (например, "Software", "Semiconductors")
     * @return Список акций в указанной индустрии
     */
    List<Stock> findByIndustry(String industry);

    /**
     * Находит акции с положительным изменением цены выше указанного порога
     * @param threshold Пороговое значение изменения в процентах
     * @return Список акций с изменением цены выше порога
     */
    @Query("SELECT s FROM Stock s WHERE s.dayChangePercent > :threshold")
    List<Stock> findStocksWithPositiveChange(@Param("threshold") BigDecimal threshold);

    /**
     * Находит акции с отрицательным изменением цены ниже указанного порога
     * @param threshold Пороговое значение изменения в процентах (отрицательное)
     * @return Список акций с изменением цены ниже порога
     */
    @Query("SELECT s FROM Stock s WHERE s.dayChangePercent < :threshold")
    List<Stock> findStocksWithNegativeChange(@Param("threshold") BigDecimal threshold);

    /**
     * Находит акции с изменением цены в указанном диапазоне
     * @param minChange Минимальное изменение
     * @param maxChange Максимальное изменение
     * @return Список акций с изменением цены в указанном диапазоне
     */
    @Query("SELECT s FROM Stock s WHERE s.dayChangePercent BETWEEN :minChange AND :maxChange")
    List<Stock> findStocksWithChangeInRange(
            @Param("minChange") BigDecimal minChange,
            @Param("maxChange") BigDecimal maxChange);

    /**
     * Находит акции по названию (частичное совпадение, без учета регистра)
     * @param name Часть названия компании
     * @return Список акций, в названии которых есть указанная строка
     */
    List<Stock> findByNameContainingIgnoreCase(String name);

    /**
     * Находит акции по символу (частичное совпадение, без учета регистра)
     * @param symbol Часть символа акции
     * @return Список акций, в символе которых есть указанная строка
     */
    List<Stock> findBySymbolContainingIgnoreCase(String symbol);

    /**
     * Находит топ-N акций с наибольшим положительным изменением цены
     * @param pageable Объект пагинации для ограничения результатов
     * @return Список акций с наибольшим ростом
     */
    @Query("SELECT s FROM Stock s WHERE s.dayChangePercent > 0 ORDER BY s.dayChangePercent DESC")
    List<Stock> findTopGainers(Pageable pageable);

    /**
     * Находит топ-N акций с наибольшим отрицательным изменением цены
     * @param pageable Объект пагинации для ограничения результатов
     * @return Список акций с наибольшим падением
     */
    @Query("SELECT s FROM Stock s WHERE s.dayChangePercent < 0 ORDER BY s.dayChangePercent ASC")
    List<Stock> findTopLosers(Pageable pageable);

    /**
     * Находит акции по сектору и индустрии
     * @param sector Сектор
     * @param industry Индустрия
     * @return Список акций в указанном секторе и индустрии
     */
    List<Stock> findBySectorAndIndustry(String sector, String industry);

    /**
     * Находит список всех секторов
     * @return Список уникальных секторов
     */
    @Query("SELECT DISTINCT s.sector FROM Stock s ORDER BY s.sector")
    List<String> findAllSectors();

    /**
     * Находит список всех индустрий в указанном секторе
     * @param sector Сектор
     * @return Список уникальных индустрий в секторе
     */
    @Query("SELECT DISTINCT s.industry FROM Stock s WHERE s.sector = :sector ORDER BY s.industry")
    List<String> findAllIndustriesBySector(@Param("sector") String sector);
}