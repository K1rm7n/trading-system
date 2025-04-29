package com.tradingsystem.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class for date operations frequently used in stock market analysis
 */
@Component
public class DateUtils {
    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    // Common date format patterns
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String ALPHAVANTAGE_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Convert string date to Date object
     *
     * @param dateStr date string
     * @param format date format
     * @return Date object
     */
    public Date parseDate(String dateStr, String format) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            logger.error("Error parsing date: {} with format: {}", dateStr, format, e);
            return null;
        }
    }

    /**
     * Format Date object to string
     *
     * @param date Date object
     * @param format date format
     * @return formatted date string
     */
    public String formatDate(Date date, String format) {
        if (date == null) {
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /**
     * Convert string date to LocalDate object
     *
     * @param dateStr date string
     * @return LocalDate object
     */
    public LocalDate parseLocalDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMAT));
        } catch (Exception e) {
            logger.error("Error parsing local date: {}", dateStr, e);
            return null;
        }
    }

    /**
     * Format LocalDate object to string
     *
     * @param date LocalDate object
     * @return formatted date string
     */
    public String formatLocalDate(LocalDate date) {
        if (date == null) {
            return null;
        }

        return date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    /**
     * Convert Date to LocalDate
     *
     * @param date Date object
     * @return LocalDate object
     */
    public LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            return null;
        }

        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Convert LocalDate to Date
     *
     * @param localDate LocalDate object
     * @return Date object
     */
    public Date convertToDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Get current date as string
     *
     * @return current date string
     */
    public String getCurrentDateAsString() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    /**
     * Get date for n days ago
     *
     * @param days number of days to go back
     * @return date n days ago
     */
    public LocalDate getDateDaysAgo(int days) {
        return LocalDate.now().minusDays(days);
    }

    /**
     * Get date for n months ago
     *
     * @param months number of months to go back
     * @return date n months ago
     */
    public LocalDate getDateMonthsAgo(int months) {
        return LocalDate.now().minusMonths(months);
    }

    /**
     * Check if a date is a trading day (Monday through Friday)
     *
     * @param date date to check
     * @return true if it's a trading day
     */
    public boolean isTradingDay(LocalDate date) {
        if (date == null) {
            return false;
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return !(dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);

        // Note: This doesn't account for market holidays
        // A more comprehensive implementation would check against a list of market holidays
    }

    /**
     * Get the previous trading day
     *
     * @param date reference date
     * @return the previous trading day
     */
    public LocalDate getPreviousTradingDay(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        LocalDate previousDay = date.minusDays(1);

        // Keep going back until we find a trading day
        while (!isTradingDay(previousDay)) {
            previousDay = previousDay.minusDays(1);
        }

        return previousDay;
    }

    /**
     * Get the next trading day
     *
     * @param date reference date
     * @return the next trading day
     */
    public LocalDate getNextTradingDay(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        LocalDate nextDay = date.plusDays(1);

        // Keep going forward until we find a trading day
        while (!isTradingDay(nextDay)) {
            nextDay = nextDay.plusDays(1);
        }

        return nextDay;
    }

    /**
     * Calculate the number of trading days between two dates
     *
     * @param startDate start date
     * @param endDate end date
     * @return number of trading days
     */
    public int countTradingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return 0;
        }

        int count = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            if (isTradingDay(currentDate)) {
                count++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return count;
    }

    /**
     * Generate a list of trading days between two dates
     *
     * @param startDate start date
     * @param endDate end date
     * @return list of trading days
     */
    public List<LocalDate> getTradingDaysBetween(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> tradingDays = new ArrayList<>();

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return tradingDays;
        }

        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            if (isTradingDay(currentDate)) {
                tradingDays.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }

        return tradingDays;
    }

    /**
     * Get the last trading day of a month
     *
     * @param year year
     * @param month month (1-12)
     * @return last trading day of the month
     */
    public LocalDate getLastTradingDayOfMonth(int year, int month) {
        LocalDate lastDayOfMonth = LocalDate.of(year, month, 1)
                .with(TemporalAdjusters.lastDayOfMonth());

        // If the last day is not a trading day, get the previous trading day
        while (!isTradingDay(lastDayOfMonth)) {
            lastDayOfMonth = lastDayOfMonth.minusDays(1);
        }

        return lastDayOfMonth;
    }

    /**
     * Get the first trading day of a month
     *
     * @param year year
     * @param month month (1-12)
     * @return first trading day of the month
     */
    public LocalDate getFirstTradingDayOfMonth(int year, int month) {
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);

        // If the first day is not a trading day, get the next trading day
        while (!isTradingDay(firstDayOfMonth)) {
            firstDayOfMonth = firstDayOfMonth.plusDays(1);
        }

        return firstDayOfMonth;
    }

    /**
     * Check if market is currently open
     * This is a simplified version and doesn't account for holidays or half-days
     *
     * @return true if market is open
     */
    public boolean isMarketOpen() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("America/New_York"));

        // Check if it's a weekday
        if (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return false;
        }

        // Check if it's between 9:30 AM and 4:00 PM ET
        int hour = now.getHour();
        int minute = now.getMinute();

        return (hour > 9 || (hour == 9 && minute >= 30)) && (hour < 16);
    }

    /**
     * Calculate time until market opens
     *
     * @return hours until market opens, or -1 if market is open
     */
    public long hoursUntilMarketOpens() {
        if (isMarketOpen()) {
            return -1; // Market is already open
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("America/New_York"));
        LocalDateTime marketOpen;

        // If it's before market open time today
        if (now.getHour() < 9 || (now.getHour() == 9 && now.getMinute() < 30)) {
            marketOpen = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 9, 30);
        } else {
            // It's after market close, get next trading day
            LocalDate nextTradingDay = getNextTradingDay(now.toLocalDate());
            marketOpen = LocalDateTime.of(nextTradingDay.getYear(), nextTradingDay.getMonth(),
                    nextTradingDay.getDayOfMonth(), 9, 30);
        }

        return ChronoUnit.HOURS.between(now, marketOpen);
    }

    /**
     * Calculate time until market closes
     *
     * @return minutes until market closes, or -1 if market is closed
     */
    public long minutesUntilMarketCloses() {
        if (!isMarketOpen()) {
            return -1; // Market is already closed
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("America/New_York"));
        LocalDateTime marketClose = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 16, 0);

        return ChronoUnit.MINUTES.between(now, marketClose);
    }
