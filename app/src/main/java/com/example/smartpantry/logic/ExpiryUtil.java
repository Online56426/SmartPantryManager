package com.example.smartpantry.logic;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Small helpers for expiry dates. Dates are stored as epoch milliseconds for the
 * start of the chosen day, and compared as calendar days so the time of day never
 * causes an item to expire "early".
 */
public final class ExpiryUtil {

    private ExpiryUtil() {
    }

    public static long startOfDayMillis(int year, int month, int dayOfMonth, ZoneId zone) {
        return LocalDate.of(year, month, dayOfMonth)
                .atStartOfDay(zone).toInstant().toEpochMilli();
    }

    public static LocalDate toLocalDate(long millis, ZoneId zone) {
        return Instant.ofEpochMilli(millis).atZone(zone).toLocalDate();
    }

    /**
     * Whole days from today until the expiry date. Negative means already expired,
     * 0 means it expires today. Only call this for items that have an expiry date.
     */
    public static long daysUntil(long expiryMillis, long nowMillis, ZoneId zone) {
        LocalDate today = toLocalDate(nowMillis, zone);
        LocalDate expiry = toLocalDate(expiryMillis, zone);
        return ChronoUnit.DAYS.between(today, expiry);
    }

    /** An item with no expiry date never expires. An item is fine on the day it expires. */
    public static boolean isExpired(long expiryMillis, long nowMillis, ZoneId zone) {
        return expiryMillis != 0 && daysUntil(expiryMillis, nowMillis, zone) < 0;
    }

    public static String formatDate(long millis, ZoneId zone, Locale locale) {
        return toLocalDate(millis, zone).format(DateTimeFormatter.ofPattern("d MMM yyyy", locale));
    }
}
