package com.driverental.onlinecarrental.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    public static LocalDate parseDate(String dateString) {
        return LocalDate.parse(dateString, DATE_FORMATTER);
    }
    
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
    
    public static long calculateDaysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
    
    public static boolean isDateRangeValid(LocalDate startDate, LocalDate endDate) {
        return !startDate.isAfter(endDate) && !startDate.isBefore(LocalDate.now());
    }
    
    public static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() >= 5; // Saturday = 5, Sunday = 6
    }
}