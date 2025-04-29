package com.tradingsystem.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingsystem.model.dto.StockDTO;
import com.tradingsystem.model.entity.Stock;
import com.tradingsystem.model.entity.User;
import com.tradingsystem.model.entity.WatchlistItem;
import com.tradingsystem.service.interfaces.StockService;
import com.tradingsystem.service.interfaces.UserService;
import com.tradingsystem.service.interfaces.WatchlistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/watchlist")
@Tag(name = "Watchlist", description = "API для работы со списком отслеживаемых акций")
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final UserService userService;
    private final StockService stockService;

    @Autowired
    public WatchlistController(
            WatchlistService watchlistService,
            UserService userService,
            StockService stockService) {
        this.watchlistService = watchlistService;
        this.userService = userService;
        this.stockService = stockService;
    }

    @GetMapping
    @Operation(summary = "Получить список отслеживаемых акций", description = "Возвращает список акций в вотчлисте пользователя")
    public ResponseEntity<List<StockDTO>> getWatchlist(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        List<Stock> stocks = watchlistService.getWatchlistStocks(user.getId());

        List<StockDTO> stockDTOs = stocks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stockDTOs);
    }

    @PostMapping("/add/{stockId}")
    @Operation(summary = "Добавить акцию в список отслеживаемых", description = "Добавляет акцию в вотчлист пользователя")
    public ResponseEntity<Void> addToWatchlist(
            @PathVariable @Parameter(description = "ID акции", example = "1") Long stockId,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Stock stock = stockService.getStockById(stockId);

        // Проверяем, есть ли уже акция в вотчлисте
        if (watchlistService.isStockInWatchlist(user.getId(), stockId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        WatchlistItem item = new WatchlistItem();
        item.setUser(user);
        item.setStock(stock);

        watchlistService.addToWatchlist(item);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/remove/{stockId}")
    @Operation(summary = "Удалить акцию из списка отслеживаемых", description = "Удаляет акцию из вотчлиста пользователя")
    public ResponseEntity<Void> removeFromWatchlist(
            @PathVariable @Parameter(description = "ID акции", example = "1") Long stockId,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());

        // Проверяем, есть ли акция в вотчлисте
        if (!watchlistService.isStockInWatchlist(user.getId(), stockId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        watchlistService.removeFromWatchlist(user.getId(), stockId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/{stockId}")
    @Operation(summary = "Проверить, находится ли акция в списке отслеживаемых", description = "Проверяет, добавлена ли акция в вотчлист пользователя")
    public ResponseEntity<Boolean> isStockInWatchlist(
            @PathVariable @Parameter(description = "ID акции", example = "1") Long stockId,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        boolean isInWatchlist = watchlistService.isStockInWatchlist(user.getId(), stockId);

        return ResponseEntity.ok(isInWatchlist);
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
}
