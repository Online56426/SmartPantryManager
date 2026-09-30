package com.example.smartpantry.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * The three settings on the Settings screen, kept in SharedPreferences (the same
 * approach the study guide uses for its sort settings). Small key and value
 * settings belong here, while the pantry data belongs in the database.
 */
public final class AppPreferences {

    private static final String FILE_NAME = "smart_pantry_prefs";

    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_EXCLUDE_EXPIRED = "exclude_expired";
    private static final String KEY_SORT_BY_EXPIRY = "sort_by_expiry";

    /** Items expiring within this many days are highlighted when alerts are on. */
    public static final int EXPIRING_SOON_DAYS = 3;

    private AppPreferences() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isExpiryAlertsOn(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsOn(Context context, boolean on) {
        prefs(context).edit().putBoolean(KEY_EXPIRY_ALERTS, on).apply();
    }

    public static boolean isExcludeExpired(Context context) {
        return prefs(context).getBoolean(KEY_EXCLUDE_EXPIRED, true);
    }

    public static void setExcludeExpired(Context context, boolean on) {
        prefs(context).edit().putBoolean(KEY_EXCLUDE_EXPIRED, on).apply();
    }

    public static boolean isSortByExpiry(Context context) {
        return prefs(context).getBoolean(KEY_SORT_BY_EXPIRY, false);
    }

    public static void setSortByExpiry(Context context, boolean sortByExpiry) {
        prefs(context).edit().putBoolean(KEY_SORT_BY_EXPIRY, sortByExpiry).apply();
    }
}
