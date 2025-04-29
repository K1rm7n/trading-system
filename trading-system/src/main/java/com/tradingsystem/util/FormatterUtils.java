package com.tradingsystem.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for formatting financial data
 */
@Component
public class FormatterUtils {

    // Currency formatter for USD
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

    // Percentage formatter
    private final NumberFormat percentFormatter = NumberFormat.getPercentInstance(Locale.US);

    // Number formatter with 2 decimal places
    private final DecimalFormat decimalFormatter = new DecimalFormat("#,##0.00");

    // Number formatter with variable decimal places
    private final DecimalFormat flexDecimalFormatter = new DecimalFormat("#,##0.######");

    public FormatterUtils() {
        // Configure percentage formatter to show 2 decimal places
        percentFormatter.setMinimumFractionDigits(2);
        percentFormatter.setMaximumFractionDigits(2);
    }

    /**
     * Format a number as currency (USD)
     *
     * @param amount the amount to format
     * @return formatted currency string
     */
    public String formatCurrency(double amount) {
        return currencyFormatter.format(amount);
    }

    /**
     * Format a number as currency (USD) with specified precision
     *
     * @param amount the amount to format
     * @param scale the number of decimal places
     * @return formatted currency string
     */
    public String formatCurrency(double amount, int scale) {
        BigDecimal bd = BigDecimal.valueOf(amount).setScale(scale, RoundingMode.HALF_UP);
        return currencyFormatter.format(bd);
    }

    /**
     * Format a number as percentage
     *
     * @param value the decimal value to format (e.g., 0.15 for 15%)
     * @return formatted percentage string
     */
    public String formatPercentage(double value) {
        return percentFormatter.format(value);
    }

    /**
     * Format a number as percentage with specified precision
     *
     * @param value the decimal value to format
     * @param scale the number of decimal places
     * @return formatted percentage string
     */
    public String formatPercentage(double value, int scale) {
        BigDecimal bd = BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
        NumberFormat customFormat = NumberFormat.getPercentInstance(Locale.US);
        customFormat.setMinimumFractionDigits(scale);
        customFormat.setMaximumFractionDigits(scale);
        return customFormat.format(bd);
    }

    /**
     * Format a number with 2 decimal places
     *
     * @param value the value to format
     * @return formatted number string
     */
    public String formatDecimal(double value) {
        return decimalFormatter.format(value);
    }

    /**
     * Format a number with specified precision
     *
     * @param value the value to format
     * @param scale the number of decimal places
     * @return formatted number string
     */
    public String formatDecimal(double value, int scale) {
        BigDecimal bd = BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
        return bd.toString();
    }

    /**
     * Format a number with variable precision (up to 6 decimal places)
     *
     * @param value the value to format
     * @return formatted number string
     */
    public String formatFlexibleDecimal(double value) {
        return flexDecimalFormatter.format(value);
    }

    /**
     * Format a large number in a readable format (K, M, B)
     *
     * @param value the value to format
     * @return formatted number string
     */
    public String formatLargeNumber(double value) {
        if (value == 0) {
            return "0";
        }

        if (Math.abs(value) < 1000) {
            return formatDecimal(value);
        }

        int exp = (int) (Math.log(Math.abs(value)) / Math.log(1000));
        char suffix = "KMBTQ".charAt(Math.min(exp - 1, 4));
        double scaled = value / Math.pow(1000, exp);

        return String.format("%.1f%c", scaled, suffix);
    }

    /**
     * Format a volume number (typically large)
     *
     * @param volume the volume to format
     * @return formatted volume string
     */
    public String formatVolume(long volume) {
        return formatLargeNumber(volume);
    }

    /**
     * Format a stock price
     *
     * @param price the price to format
     * @return formatted price string
     */
    public String formatStockPrice(double price) {
        if (price < 1) {
            // For penny stocks, show 4 decimal places
            return formatDecimal(price, 4);
        } else if (price < 100) {
            // For most stocks, show 2 decimal places
            return formatDecimal(price, 2);
        } else {
            // For high-priced stocks, show no decimal places
            return formatDecimal(price, 0);
        }
    }

    /**
     * Format price change with color indicator
     *
     * @param change the price change
     * @param percentChange the percentage change
     * @return formatted price change string with indicator
     */
    public String formatPriceChange(double change, double percentChange) {
        String changeStr = formatDecimal(Math.abs(change));
        String percentStr = formatPercentage(Math.abs(percentChange));

        if (change > 0) {
            return "+" + changeStr + " (+" + percentStr + ")";
        } else if (change < 0) {
            return "-" + changeStr + " (-" + percentStr + ")";
        } else {
            return changeStr + " (" + percentStr + ")";
        }
    }

    /**
     * Format a number as compact string
     *
     * @param value the value to format
     * @return formatted compact string
     */
    public String formatCompact(double value) {
        if (Math.abs(value) < 1000) {
            return String.valueOf((int) value);
        }

        // Convert to K, M, B, T
        String[] suffixes = new String[] { "", "K", "M", "B", "T" };
        int suffixIndex = 0;

        while (Math.abs(value) >= 1000 && suffixIndex < suffixes.length - 1) {
            value /= 1000;
            suffixIndex++;
        }

        return String.format("%.1f%s", value, suffixes[suffixIndex]);
    }
}
