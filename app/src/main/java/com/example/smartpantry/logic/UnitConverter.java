package com.example.smartpantry.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Turns "a quantity plus a unit" into a comparable amount.
 *
 * Every unit belongs to a family (mass, volume or count) and has a factor that
 * converts it to the family's base unit: grams, millilitres or pieces. That way
 * 0.5 kg and 500 g compare as equal, and 2 cups compares correctly with 500 ml.
 *
 * Mass and volume cannot normally be compared, but real pantries mix them
 * ("1 kg of sugar" against a recipe that wants "3 tbsp"). For a short list of
 * common dry staples we therefore know roughly how many grams fit in a
 * millilitre, and convert volume to mass for those ingredients only.
 */
public final class UnitConverter {

    public static final String FAMILY_MASS = "mass";
    public static final String FAMILY_VOLUME = "volume";
    public static final String FAMILY_COUNT = "count";

    /** A quantity expressed in the base unit of its family. */
    public static final class Measure {
        public final String family;
        public final double amount;

        Measure(String family, double amount) {
            this.family = family;
            this.amount = amount;
        }
    }

    private static final Map<String, String> FAMILY_OF_UNIT = new HashMap<>();
    private static final Map<String, Double> FACTOR_OF_UNIT = new HashMap<>();

    /** Approximate grams per millilitre for ingredients that are usually bought by weight. */
    private static final Map<String, Double> GRAMS_PER_ML = new HashMap<>();

    static {
        // Mass, base unit: gram
        register(FAMILY_MASS, 1, "g", "gram", "grams", "gr");
        register(FAMILY_MASS, 1000, "kg", "kgs", "kilogram", "kilograms");
        register(FAMILY_MASS, 28.3495, "oz", "ounce", "ounces");
        register(FAMILY_MASS, 453.592, "lb", "lbs", "pound", "pounds");

        // Volume, base unit: millilitre
        register(FAMILY_VOLUME, 1, "ml", "millilitre", "millilitres", "milliliter", "milliliters");
        register(FAMILY_VOLUME, 1000, "l", "litre", "litres", "liter", "liters");
        register(FAMILY_VOLUME, 5, "tsp", "teaspoon", "teaspoons");
        register(FAMILY_VOLUME, 15, "tbsp", "tbs", "tablespoon", "tablespoons");
        register(FAMILY_VOLUME, 250, "cup", "cups");

        // Count, base unit: one piece. A blank unit is treated as a plain count.
        register(FAMILY_COUNT, 1, "", "pcs", "pc", "piece", "pieces", "whole", "unit", "units",
                "each", "tin", "tins", "can", "cans", "clove", "cloves", "slice", "slices");

        GRAMS_PER_ML.put("salt", 1.2);
        GRAMS_PER_ML.put("sugar", 0.85);
        GRAMS_PER_ML.put("flour", 0.53);
        GRAMS_PER_ML.put("rice", 0.8);
        GRAMS_PER_ML.put("maize meal", 0.6);
        GRAMS_PER_ML.put("butter", 0.95);
        GRAMS_PER_ML.put("black pepper", 0.45);
        GRAMS_PER_ML.put("peanut butter", 1.05);
        GRAMS_PER_ML.put("oil", 0.92);
        GRAMS_PER_ML.put("milk", 1.03);
    }

    private UnitConverter() {
    }

    private static void register(String family, double factor, String... aliases) {
        for (String alias : aliases) {
            FAMILY_OF_UNIT.put(alias, family);
            FACTOR_OF_UNIT.put(alias, factor);
        }
    }

    private static String cleanUnit(String unit) {
        if (unit == null) {
            return "";
        }
        return unit.trim().toLowerCase(Locale.ROOT).replace(".", "");
    }

    /** Converts to the base unit of the unit's family, with no ingredient specific rules. */
    public static Measure toBase(double quantity, String unit) {
        String key = cleanUnit(unit);
        String family = FAMILY_OF_UNIT.get(key);
        if (family == null) {
            // A unit we have never heard of only ever matches the same unit.
            return new Measure("other:" + key, quantity);
        }
        return new Measure(family, quantity * FACTOR_OF_UNIT.get(key));
    }

    /**
     * Converts for a specific ingredient. If we know the ingredient's density and the
     * quantity is a volume, it is converted to grams so it can be compared with weights.
     *
     * @param ingredientKey the canonical ingredient name, see IngredientNormalizer
     */
    public static Measure toBase(double quantity, String unit, String ingredientKey) {
        Measure measure = toBase(quantity, unit);
        Double density = GRAMS_PER_ML.get(ingredientKey);
        if (density != null && FAMILY_VOLUME.equals(measure.family)) {
            return new Measure(FAMILY_MASS, measure.amount * density);
        }
        return measure;
    }
}
