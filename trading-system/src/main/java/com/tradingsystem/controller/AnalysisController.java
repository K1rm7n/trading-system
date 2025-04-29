package com.tradingsystem.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingsystem.model.dto.AnalysisDTO;
import com.tradingsystem.model.entity.Analysis;
import com.tradingsystem.model.enums.TrendType;
import com.tradingsystem.service.interfaces.AnalysisService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/analyses")
@Tag(name = "Analyses", description = "API для работы с аналитическими данными")
public class AnalysisController {

    private final AnalysisService analysisService;

    @Autowired
    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping
    @Operation(summary = "Получить все анализы", description = "Возвращает список всех аналитических данных")
    public ResponseEntity<List<AnalysisDTO>> getAllAnalyses() {
        List<Analysis> analyses = analysisService.getAllAnalyses();
        List<AnalysisDTO> analysisDTOs = analyses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(analysisDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить анализ по ID", description = "Возвращает детальную информацию об анализе")
    public ResponseEntity<AnalysisDTO> getAnalysisById(
            @PathVariable @Parameter(description = "ID анализа", example = "1") Long id) {
        Analysis analysis = analysisService.getAnalysisById(id);
        return ResponseEntity.ok(convertToDTO(analysis));
    }

    @GetMapping("/trends/{trend}")
    @Operation(summary = "Получить анализы по тренду", description = "Возвращает список анализов с указанным трендом")
    public ResponseEntity<List<AnalysisDTO>> getAnalysesByTrend(
            @PathVariable @Parameter(description = "Тип тренда (UPTREND, DOWNTREND, SIDEWAYS)", example = "UPTREND") String trend) {
        TrendType trendType = TrendType.valueOf(trend.toUpperCase());
        List<Analysis> analyses = analysisService.getAnalysesByTrend(trendType);
        List<AnalysisDTO> analysisDTOs = analyses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(analysisDTOs);
    }

    @GetMapping("/recommendations/{recommendation}")
    @Operation(summary = "Получить анализы по рекомендации", description = "Возвращает список анализов с указанной рекомендацией")
    public ResponseEntity<List<AnalysisDTO>> getAnalysesByRecommendation(
            @PathVariable @Parameter(description = "Тип рекомендации (BUY, SELL, HOLD)", example = "BUY") String recommendation) {
        List<Analysis> analyses = analysisService.getAnalysesByRecommendation(recommendation.toUpperCase());
        List<AnalysisDTO> analysisDTOs = analyses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(analysisDTOs);
    }

    @GetMapping("/latest")
    @Operation(summary = "Получить последние анализы", description = "Возвращает список последних анализов")
    public ResponseEntity<List<AnalysisDTO>> getLatestAnalyses() {
        List<Analysis> analyses = analysisService.getLatestAnalyses(10);
        List<AnalysisDTO> analysisDTOs = analyses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(analysisDTOs);
    }

    @GetMapping("/stock/{stockId}")
    @Operation(summary = "Получить анализы по акции", description = "Возвращает список анализов для конкретной акции")
    public ResponseEntity<List<AnalysisDTO>> getAnalysesByStock(
            @PathVariable @Parameter(description = "ID акции", example = "1") Long stockId) {
        List<Analysis> analyses = analysisService.getAnalysesForStock(stockId);
        List<AnalysisDTO> analysisDTOs = analyses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(analysisDTOs);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить анализ", description = "Удаляет существующий анализ")
    public ResponseEntity<Void> deleteAnalysis(
            @PathVariable @Parameter(description = "ID анализа", example = "1") Long id) {
        analysisService.deleteAnalysis(id);
        return ResponseEntity.noContent().build();
    }

    private AnalysisDTO convertToDTO(Analysis analysis) {
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