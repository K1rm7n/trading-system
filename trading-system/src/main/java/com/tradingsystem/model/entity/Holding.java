package com.tradingsystem.model.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Сущность позиции в портфеле (холдинг)
 */
@Entity
@Table(name = "holdings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"portfolio_id", "stock_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "portfolio", "stock"})
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Портфель, в котором находится позиция
     */
    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    /**
     * Акция, которая находится в портфеле
     */
    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    /**
     * Количество акций в портфеле
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Средняя цена покупки
     */
    @Column(name = "average_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal averagePrice;

    /**
     * Получение текущей стоимости позиции
     * @return Текущая стоимость позиции
     */
    public BigDecimal getCurrentValue() {
        if (stock != null && stock.getCurrentPrice() != null) {
            return stock.getCurrentPrice().multiply(new BigDecimal(quantity));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Получение прибыли/убытка в абсолютном выражении
     * @return Сумма прибыли/убытка
     */
    public BigDecimal getProfitLoss() {
        if (stock != null && stock.getCurrentPrice() != null) {
            return getCurrentValue().subtract(averagePrice.multiply(new BigDecimal(quantity)));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Получение прибыли/убытка в процентном выражении
     * @return Процент прибыли/убытка
     */
    public BigDecimal getProfitLossPercent() {
        if (averagePrice != null && averagePrice.compareTo(BigDecimal.ZERO) > 0 && stock != null && stock.getCurrentPrice() != null) {
            return stock.getCurrentPrice().subtract(averagePrice)
                    .divide(averagePrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }
}
