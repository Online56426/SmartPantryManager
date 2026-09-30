package com.example.smartpantry.logic;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Reading and showing quantities. In South Africa, our phones often use a comma as the
 * decimal separator, so input accepts both "0.5" and "0,5", while output always
 * uses a full stop so the edit screen and the list agree with each other.
 */
public final class QuantityFormat {

    private QuantityFormat() {
    }

    /** Whole numbers show without decimals: 3 not 3.0. Others show up to two decimals. */
    public static String format(double quantity) {
        if (Math.abs(quantity - Math.rint(quantity)) < 1e-9) {
            return String.valueOf((long) Math.rint(quantity));
        }
        DecimalFormat formatter = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));
        return formatter.format(quantity);
    }

    /**
     * Parses a plain positive number such as 2, 0.5 or 0,5.
     *
     * @throws NumberFormatException for empty text, letters, signs or exponents
     */
    public static double parse(String text) {
        if (text == null) {
            throw new NumberFormatException("No text to parse");
        }
        String cleaned = text.trim().replace(',', '.');
        if (!cleaned.matches("\\d*\\.?\\d+")) {
            throw new NumberFormatException("Not a plain number: " + text);
        }
        return Double.parseDouble(cleaned);
    }
}
