package com.tradingsystem.controller;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tradingsystem.model.dto.AnalysisDTO;
import com.tradingsystem.model.dto.StockDTO;
import com.tradingsystem.model.entity.Analysis;
import com.tradingsystem.model.entity.Stock;
import com.tradingsystem.service.interfaces.AnalysisService;
import com.tradingsystem.service.interfaces.StockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Stocks", description = "API для работы с акциями")
public class StockController {

    private final StockService stockService;
    private final AnalysisService analysisService;

    @Autowired
    public StockController(StockService stockService, AnalysisService analysisService) {
        this.stockService = stockService;
        this.analysisService = analysisService;
    }

    @GetMapping
    @Operation(summary = "Получить список всех акций", description = "Возвращает список всех акций в системе")
    public ResponseEntity<List<StockDTO>> getAllStocks() {
        List<Stock> stocks = stockService.getAllStocks();
        List<StockDTO> stockDTOs = stocks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(stockDTOs);
    }

    @GetMapping("/{symbol}")
    @Operation(summary = "Получить информацию по акции", description = "Возвращает детальную информацию по акции по её символу")
    public ResponseEntity<StockDTO> getStockBySymbol(
            @PathVariable @Parameter(description = "Символ акции (тикер)", example = "AAPL") String symbol) {
        Stock stock = stockService.getStockBySymbol(symbol);
        return ResponseEntity.ok(convertToDTO(stock));
    }

    @PostMapping("/{symbol}/refresh")
    @Operation(summary = "Обновить данные по акции", description = "Обновляет рыночные данные по акции из AlphaVantage")
    public ResponseEntity<StockDTO> refreshStockData(
            @PathVariable @Parameter(description = "Символ акции (тикер)", example = "AAPL") String symbol) {
        Stock stock = stockService.updateStockData(symbol);
        return ResponseEntity.ok(convertToDTO(stock));
    }

    @GetMapping("/{symbol}/analysis")
    @Operation(summary = "Получить анализ акции", description = "Возвращает последние аналитические данные по акции")
    public ResponseEntity<List<AnalysisDTO>> getStockAnalysis(
            @PathVariable @Parameter(description = "Символ акции (тикер)", example = "AAPL") String symbol) {
        Stock stock = stockService.getStockBySymbol(symbol);
        List<Analysis> analyses = analysisService.getAnalysesForStock(stock.getId());
        List<AnalysisDTO> analysisDTOs = analyses.stream()
                .map(this::convertToAnalysisDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(analysisDTOs);
    }

    @PostMapping("/{symbol}/analyze")
    @Operation(summary = "Создать новый анализ акции", description = "Генерирует новый анализ на основе текущих данных и рекомендации ChatGPT")
    public ResponseEntity<AnalysisDTO> createAnalysis(
            @PathVariable @Parameter(description = "Символ акции (тикер)", example = "AAPL") String symbol) {
        Analysis analysis = analysisService.generateAnalysis(symbol);
        return ResponseEntity.ok(convertToAnalysisDTO(analysis));
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск акций", description = "Поиск акций по различным критериям")
    public ResponseEntity<List<StockDTO>> searchStocks(
            @RequestParam(required = false) @Parameter(description = "Сектор") String sector,
            @RequestParam(required = false) @Parameter(description = "Индустрия") String industry,
            @RequestParam(required = false) @Parameter(description = "Минимальный процент изменения цены") Double minChangePercent,
            @RequestParam(required = false) @Parameter(description = "Максимальный процент изменения цены") Double maxChangePercent) {

        List<Stock> stocks = stockService.searchStocks(sector, industry, minChangePercent, maxChangePercent);
        List<StockDTO> stockDTOs = stocks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(stockDTOs);
    }

    @GetMapping("/trending")
    @Operation(summary = "Получить трендовые акции", description = "Возвращает список акций с наибольшим изменением цены")
    public ResponseEntity<List<StockDTO>> getTrendingStocks(
            @RequestParam(defaultValue = "10") @Parameter(description = "Количество акций", example = "10") int limit) {
        List<Stock> stocks = stockService.getTrendingStocks(limit);
        List<StockDTO> stockDTOs = stocks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(stockDTOs);
    }

    private StockDTO convertToDTO(Stock stock) {
        StockDTO dto = new StockDTO();
        dto.setId(stock.getId());
        dto.setSymbol(stock.getSymbol());
        dto.setName(stock.getName());
        dto.setSector(stock.getSector());
        dto.setIndustry(stock.getIndustry());
        dto.setCurrentPrice(stock.getCurrentPrice());
        dto.setPreviousClose(stock.getPreviousClose());
        dto.setDayChangePercent(stock.getDayChangePercent());
        dto.setLastUpdated(stock.getLastUpdated());
        return dto;
    }

    private AnalysisDTO convertToAnalysisDTO(Analysis analysis) {
        AnalysisDTO dto = new AnalysisDTO();
        dto.setId(analysis.getId());
        dto.setStockId(analysis.getStock().getId());
        dto.setStockSymbol(analysis.getStock().getSymbol());
        dto.setTrend(analysis.getTrend().name());
        dto.setRecommendation(analysis.getRecommendation());
        dto.setConfidenceScore(analysis.getConfidenceScore());
        dto.setAnalysisDate(analysis.getAnalysisDate());
        dto.setRationale(analysis.getRationale());
        return dto;
    }
}
