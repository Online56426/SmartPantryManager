package com.example.smartpantry.logic;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The strict matching rule from Section 2.3 of the brief, and the most important
 * piece of logic in the app.
 *
 * A recipe is only "cookable" if EVERY ingredient it needs is in the pantry in at
 * least the required quantity. If a recipe needs five ingredients and the pantry
 * covers four, it is not cookable. There is no scoring and no percentage match.
 *
 * How it works:
 *  1. Add up the pantry. Each item is filed under its normalised name(s) and its
 *     quantity is converted to a base unit, so two entries of the same ingredient
 *     are combined and units such as kg and g compare correctly.
 *  2. For each recipe ingredient, look up the same key and compare amounts.
 *  3. Anything not covered goes into the recipe's "missing" list.
 *
 * This class has no Android dependencies, so it is covered by plain JUnit tests.
 */
public final class RecipeMatcher {

    /** Guards against floating point noise, for example 0.1 + 0.2 not equalling 0.3. */
    private static final double EPSILON = 1e-9;

    private RecipeMatcher() {
    }

    /**
     * Totals the pantry as: ingredient key, then unit family, then amount in base units.
     *
     * @param excludeExpired when true, items past their expiry date are ignored
     */
    public static Map<String, Map<String, Double>> buildPantryTotals(
            List<PantryItem> pantry, boolean excludeExpired, long nowMillis, ZoneId zone) {

        Map<String, Map<String, Double>> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            if (item.getQuantity() <= 0) {
                continue;
            }
            if (excludeExpired && ExpiryUtil.isExpired(item.getExpiryMillis(), nowMillis, zone)) {
                continue;
            }
            for (String key : IngredientNormalizer.matchKeys(item.getName())) {
                UnitConverter.Measure measure =
                        UnitConverter.toBase(item.getQuantity(), item.getUnit(), key);
                Map<String, Double> byFamily = totals.get(key);
                if (byFamily == null) {
                    byFamily = new HashMap<>();
                    totals.put(key, byFamily);
                }
                Double sofar = byFamily.get(measure.family);
                byFamily.put(measure.family, (sofar == null ? 0.0 : sofar) + measure.amount);
            }
        }
        return totals;
    }

    /** Checks one recipe against a pantry that has already been totalled. */
    public static MatchResult evaluate(Recipe recipe, Map<String, Map<String, Double>> pantryTotals) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient needed : recipe.getIngredients()) {
            if (!isCovered(needed, pantryTotals)) {
                missing.add(needed);
            }
        }
        return new MatchResult(recipe, missing);
    }

    private static boolean isCovered(RecipeIngredient needed,
                                     Map<String, Map<String, Double>> pantryTotals) {
        String key = IngredientNormalizer.canonical(needed.getName());
        Map<String, Double> byFamily = pantryTotals.get(key);
        if (byFamily == null) {
            return false;                       // ingredient is not in the pantry at all
        }
        UnitConverter.Measure required =
                UnitConverter.toBase(needed.getQuantity(), needed.getUnit(), key);
        Double have = byFamily.get(required.family);
        if (have == null || have <= 0) {
            return false;                       // only held in a unit we cannot compare
        }
        return have + EPSILON >= required.amount;   // "at least the required quantity"
    }

    /** Checks every recipe against the pantry. */
    public static List<MatchResult> matchAll(List<Recipe> recipes, List<PantryItem> pantry,
                                             boolean excludeExpired, long nowMillis, ZoneId zone) {
        Map<String, Map<String, Double>> totals =
                buildPantryTotals(pantry, excludeExpired, nowMillis, zone);
        List<MatchResult> results = new ArrayList<>();
        for (Recipe recipe : recipes) {
            results.add(evaluate(recipe, totals));
        }
        return results;
    }

    /** Only the recipes that can be cooked right now, in alphabetical order. */
    public static List<MatchResult> cookableNow(List<MatchResult> all) {
        List<MatchResult> cookable = new ArrayList<>();
        for (MatchResult result : all) {
            if (result.isCookable()) {
                cookable.add(result);
            }
        }
        sortByName(cookable);
        return cookable;
    }

    /** Optional extra list: recipes exactly one ingredient short. Never mixed into the strict list. */
    public static List<MatchResult> almostThere(List<MatchResult> all) {
        List<MatchResult> almost = new ArrayList<>();
        for (MatchResult result : all) {
            if (result.isAlmostThere()) {
                almost.add(result);
            }
        }
        sortByName(almost);
        return almost;
    }

    private static void sortByName(List<MatchResult> results) {
        Collections.sort(results, new Comparator<MatchResult>() {
            @Override
            public int compare(MatchResult a, MatchResult b) {
                return a.getRecipe().getName().compareToIgnoreCase(b.getRecipe().getName());
            }
        });
    }
}
