package com.tradingsystem.controller;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingsystem.model.dto.PortfolioDTO;
import com.tradingsystem.model.dto.PortfolioPerformanceDTO;
import com.tradingsystem.model.entity.Portfolio;
import com.tradingsystem.model.entity.User;
import com.tradingsystem.service.interfaces.PortfolioService;
import com.tradingsystem.service.interfaces.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/portfolios")
@Tag(name = "Portfolios", description = "API для работы с портфелями инвестиций")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserService userService;

    @Autowired
    public PortfolioController(PortfolioService portfolioService, UserService userService) {
        this.portfolioService = portfolioService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Получить все портфели пользователя", description = "Возвращает список всех портфелей текущего пользователя")
    public ResponseEntity<List<PortfolioDTO>> getUserPortfolios(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        List<Portfolio> portfolios = portfolioService.getPortfoliosByUserId(user.getId());
        List<PortfolioDTO> portfolioDTOs = portfolios.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(portfolioDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить портфель по ID", description = "Возвращает детальную информацию о портфеле")
    public ResponseEntity<PortfolioDTO> getPortfolioById(
            @PathVariable @Parameter(description = "ID портфеля", example = "1") Long id,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Portfolio portfolio = portfolioService.getPortfolioById(id);

        // Проверка, принадлежит ли портфель текущему пользователю
        if (!portfolio.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(convertToDTO(portfolio));
    }

    @PostMapping
    @Operation(summary = "Создать новый портфель", description = "Создает новый портфель для текущего пользователя")
    public ResponseEntity<PortfolioDTO> createPortfolio(
            @RequestBody @Valid PortfolioDTO portfolioDTO,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());

        Portfolio portfolio = convertFromDTO(portfolioDTO);
        portfolio.setUser(user);
        portfolio.setCreationDate(LocalDateTime.now());

        Portfolio createdPortfolio = portfolioService.createPortfolio(portfolio);
        return new ResponseEntity<>(convertToDTO(createdPortfolio), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить портфель", description = "Обновляет информацию о существующем портфеле")
    public ResponseEntity<PortfolioDTO> updatePortfolio(
            @PathVariable @Parameter(description = "ID портфеля", example = "1") Long id,
            @RequestBody @Valid PortfolioDTO portfolioDTO,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Portfolio existingPortfolio = portfolioService.getPortfolioById(id);

        // Проверка, принадлежит ли портфель текущему пользователю
        if (!existingPortfolio.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Portfolio portfolio = convertFromDTO(portfolioDTO);
        portfolio.setId(id);
        portfolio.setUser(user);

        Portfolio updatedPortfolio = portfolioService.updatePortfolio(id, portfolio);
        return ResponseEntity.ok(convertToDTO(updatedPortfolio));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить портфель", description = "Удаляет существующий портфель")
    public ResponseEntity<Void> deletePortfolio(
            @PathVariable @Parameter(description = "ID портфеля", example = "1") Long id,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Portfolio portfolio = portfolioService.getPortfolioById(id);

        // Проверка, принадлежит ли портфель текущему пользователю
        if (!portfolio.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        portfolioService.deletePortfolio(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/performance")
    @Operation(summary = "Получить производительность портфеля", description = "Рассчитывает и возвращает метрики эффективности портфеля")
    public ResponseEntity<PortfolioPerformanceDTO> getPortfolioPerformance(
            @PathVariable @Parameter(description = "ID портфеля", example = "1") Long id,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Portfolio portfolio = portfolioService.getPortfolioById(id);

        // Проверка, принадлежит ли портфель текущему пользователю
        if (!portfolio.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PortfolioPerformanceDTO performance = portfolioService.calculatePerformance(id);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/{id}/holdings")
    @Operation(summary = "Получить содержимое портфеля", description = "Возвращает список всех позиций в портфеле")
    public ResponseEntity<?> getPortfolioHoldings(
            @PathVariable @Parameter(description = "ID портфеля", example = "1") Long id,
            Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Portfolio portfolio = portfolioService.getPortfolioById(id);

        // Проверка, принадлежит ли портфель текущему пользователю
        if (!portfolio.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(portfolioService.getPortfolioHoldings(id));
    }

    private PortfolioDTO convertToDTO(Portfolio portfolio) {
        PortfolioDTO dto = new PortfolioDTO();
        dto.setId(portfolio.getId());
        dto.setName(portfolio.getName());
        dto.setDescription(portfolio.getDescription());
        dto.setCreationDate(portfolio.getCreationDate());
        dto.setTotalValue(portfolio.getTotalValue());
        dto.setUserId(portfolio.getUser().getId());
        return dto;
    }

    private Portfolio convertFromDTO(PortfolioDTO dto) {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(dto.getId());
        portfolio.setName(dto.getName());
        portfolio.setDescription(dto.getDescription());

        if (dto.getCreationDate() != null) {
            portfolio.setCreationDate(dto.getCreationDate());
        }

        portfolio.setTotalValue(dto.getTotalValue());

        return portfolio;
    }
}
