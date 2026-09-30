package com.example.smartpantry.logic;

/**
 * Validation rules for the Add/Edit ingredient form. The rules live here, away
 * from the Activity, so they can be unit tested without an emulator. The Activity
 * turns the result into a message on screen.
 */
public final class InputValidator {

    public static final int MAX_NAME_LENGTH = 40;
    public static final double MAX_QUANTITY = 100000;

    public enum NameProblem { NONE, EMPTY, TOO_LONG, NO_LETTERS }

    public enum QuantityProblem { NONE, EMPTY, NOT_A_NUMBER, NOT_POSITIVE, TOO_LARGE }

    private InputValidator() {
    }

    public static NameProblem checkName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return NameProblem.EMPTY;
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            return NameProblem.TOO_LONG;
        }
        if (!trimmed.matches(".*\\p{L}.*")) {
            return NameProblem.NO_LETTERS;
        }
        return NameProblem.NONE;
    }

    public static QuantityProblem checkQuantity(String text) {
        if (text == null || text.trim().isEmpty()) {
            return QuantityProblem.EMPTY;
        }
        double value;
        try {
            value = QuantityFormat.parse(text);
        } catch (NumberFormatException e) {
            return QuantityProblem.NOT_A_NUMBER;
        }
        if (value <= 0) {
            return QuantityProblem.NOT_POSITIVE;
        }
        if (value > MAX_QUANTITY) {
            return QuantityProblem.TOO_LARGE;
        }
        return QuantityProblem.NONE;
    }
}
