package com.tradingsystem.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingsystem.model.dto.TradeDTO;
import com.tradingsystem.model.entity.Portfolio;
import com.tradingsystem.model.entity.Stock;
import com.tradingsystem.model.entity.Trade;
import com.tradingsystem.model.entity.User;
import com.tradingsystem.model.enums.TradeType;
import com.tradingsystem.service.interfaces.PortfolioService;
import com.tradingsystem.service.interfaces.StockService;
import com.tradingsystem.service.interfaces.TradeService;
import com.tradingsystem.service.interfaces.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/trades")
@Tag(name = "Trades", description = "API для работы с торговыми операциями")
public class TradeController {

    private final TradeService tradeService;
    private final PortfolioService portfolioService;
    private final StockService stockService;
    private final UserService userService;

    @Autowired
    public TradeController(
            TradeService tradeService,
            PortfolioService portfolioService,
            StockService stockService,
            UserService userService) {
        this.tradeService = tradeService;
        this.portfolioService = portfolioService;
        this.stockService = stockService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Получить все сделки пользователя", description = "Возвращает список всех торговых операций текущего пользователя")
    public ResponseEntity<List<TradeDTO>> getUserTrades(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        List<Trade> trades = tradeService.getTradesByUserId(user.getId());
        List<TradeDTO> tradeDTOs = trades.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tradeDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить сделку по ID", description = "Возвращает детальную информацию о торговой операции")
    public ResponseEntity<TradeDTO> getTradeById(
            @PathVariable @Parameter(description = "ID сделки", example = "1") Long id,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Trade trade = tradeService.getTradeById(id);

        // Проверка, принадлежит ли сделка портфелю пользователя
        if (!trade.getPortfolio().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(convertToDTO(trade));
    }

    @GetMapping("/portfolio/{portfolioId}")
    @Operation(summary = "Получить сделки портфеля", description = "Возвращает список сделок для конкретного портфеля")
    public ResponseEntity<List<TradeDTO>> getTradesByPortfolio(
            @PathVariable @Parameter(description = "ID портфеля", example = "1") Long portfolioId,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);

        // Проверка, принадлежит ли портфель пользователю
        if (!portfolio.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Trade> trades = tradeService.getTradesByPortfolioId(portfolioId);
        List<TradeDTO> tradeDTOs = trades.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tradeDTOs);
    }

    @PostMapping
    @Operation(summary = "Создать новую сделку", description = "Регистрирует новую торговую операцию в системе")
    public ResponseEntity<TradeDTO> createTrade(
            @RequestBody @Valid TradeDTO tradeDTO,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());

        // Получаем портфель и проверяем его принадлежность пользователю
        Portfolio portfolio = portfolioService.getPortfolioById(tradeDTO.getPortfolioId());
        if (!portfolio.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Получаем акцию
        Stock stock = stockService.getStockById(tradeDTO.getStockId());

        // Создаем объект сделки
        Trade trade = new Trade();
        trade.setType(TradeType.valueOf(tradeDTO.getType()));
        trade.setQuantity(tradeDTO.getQuantity());
        trade.setPrice(tradeDTO.getPrice());
        trade.setTimestamp(LocalDateTime.now());
        trade.setPortfolio(portfolio);
        trade.setStock(stock);

        // Сохраняем сделку и обновляем портфель
        Trade createdTrade = tradeService.createTrade(trade);

        return new ResponseEntity<>(convertToDTO(createdTrade), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить сделку", description = "Удаляет существующую торговую операцию")
    public ResponseEntity<Void> deleteTrade(
            @PathVariable @Parameter(description = "ID сделки", example = "1") Long id,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Trade trade = tradeService.getTradeById(id);

        // Проверка, принадлежит ли сделка портфелю пользователя
        if (!trade.getPortfolio().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        tradeService.deleteTrade(id);
        return ResponseEntity.noContent().build();
    }

    private TradeDTO convertToDTO(Trade trade) {
        TradeDTO dto = new TradeDTO();
        dto.setId(trade.getId());
        dto.setStockId(trade.getStock().getId());
        dto.setStockSymbol(trade.getStock().getSymbol());
        dto.setPortfolioId(trade.getPortfolio().getId());
        dto.setType(trade.getType().name());
        dto.setQuantity(trade.getQuantity());
        dto.setPrice(trade.getPrice());
        dto.setTimestamp(trade.getTimestamp());
        dto.setTotalValue(trade.getPrice().multiply(new BigDecimal(trade.getQuantity())));
        return dto;
    }
}
