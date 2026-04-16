package com.gaston.lesbegueris.notepases.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.gaston.lesbegueris.notepases.R;

import java.util.Locale;

public final class DistanceFormatter {
    private static final float METERS_PER_MILE = 1609.344f;
    private static final String PREF_UNITS = "pref_units";
    private static final String UNITS_SYSTEM = "system";
    private static final String UNITS_METRIC = "metric";
    private static final String UNITS_IMPERIAL = "imperial";

    private DistanceFormatter() {}

    public static class Display {
        public final String value;
        public final String unit;

        public Display(String value, String unit) {
            this.value = value;
            this.unit = unit;
        }
    }

    public static boolean isImperial(Context context) {
        String pref = getUnitsPreference(context);
        if (UNITS_METRIC.equals(pref)) {
            return false;
        }
        if (UNITS_IMPERIAL.equals(pref)) {
            return true;
        }
        Locale locale = context.getResources().getConfiguration().getLocales().get(0);
        String country = locale.getCountry();
        return "US".equalsIgnoreCase(country)
                || "LR".equalsIgnoreCase(country)
                || "MM".equalsIgnoreCase(country);
    }

    public static String getUnitsPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREF_UNITS, UNITS_SYSTEM);
    }

    public static void setUnitsPreference(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(PREF_UNITS, value).apply();
    }

    public static Display formatDistance(Context context, float meters) {
        Locale locale = Locale.getDefault();
        if (isImperial(context)) {
            float miles = meters / METERS_PER_MILE;
            String value = miles < 10f
                    ? String.format(locale, "%.1f", miles)
                    : String.format(locale, "%.0f", miles);
            return new Display(value, context.getString(R.string.miles_short));
        }

        if (meters >= 1000f) {
            float km = meters / 1000f;
            String value = km < 10f
                    ? String.format(locale, "%.1f", km)
                    : String.format(locale, "%.0f", km);
            return new Display(value, context.getString(R.string.kilometers_short));
        }

        return new Display(String.format(locale, "%.0f", meters),
                context.getString(R.string.meters_short));
    }

    public static String getAlertUnit(Context context) {
        return isImperial(context)
                ? context.getString(R.string.miles_short)
                : context.getString(R.string.meters_short);
    }

    public static int toAlertMeters(Context context, String input) {
        float value;
        try {
            value = Float.parseFloat(input);
        } catch (NumberFormatException e) {
            value = 0f;
        }
        if (value <= 0f) {
            return 0;
        }
        if (isImperial(context)) {
            float meters = value * METERS_PER_MILE;
            return Math.round(meters);
        }
        return Math.round(value);
    }
}
