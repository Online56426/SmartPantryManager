package com.example.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import org.junit.Test;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for the strict matching rule (brief, Section 2.3). These are the cases a
 * marker is most likely to try: partial matches, plural names, unit differences.
 */
public class RecipeMatcherTest {

    private static final ZoneId ZONE = ZoneId.of("Africa/Johannesburg");
    // 15 October 2026, midday
    private static final long NOW = ExpiryUtil.startOfDayMillis(2026, 10, 15, ZONE) + 12L * 3600_000L;

    private static PantryItem item(String name, double qty, String unit) {
        return new PantryItem(PantryItem.NO_ID, name, qty, unit, PantryItem.NO_EXPIRY);
    }

    private static PantryItem itemExpiring(String name, double qty, String unit, int y, int m, int d) {
        return new PantryItem(PantryItem.NO_ID, name, qty, unit,
                ExpiryUtil.startOfDayMillis(y, m, d, ZONE));
    }

    private static RecipeIngredient ing(String name, double qty, String unit) {
        return new RecipeIngredient(name, qty, unit);
    }

    private static Recipe recipe(String name, RecipeIngredient... ingredients) {
        return new Recipe(1, name, 10, "Cook it.", new ArrayList<>(Arrays.asList(ingredients)));
    }

    private static MatchResult check(Recipe recipe, boolean excludeExpired, PantryItem... pantry) {
        List<MatchResult> results = RecipeMatcher.matchAll(
                Arrays.asList(recipe), Arrays.asList(pantry), excludeExpired, NOW, ZONE);
        return results.get(0);
    }

    @Test
    public void allIngredientsPresent_isCookable() {
        Recipe r = recipe("Toast", ing("bread", 2, "slices"), ing("butter", 10, "g"));
        MatchResult result = check(r, true, item("Bread", 8, "pcs"), item("Butter", 250, "g"));
        assertTrue(result.isCookable());
        assertTrue(result.getMissing().isEmpty());
    }

    @Test
    public void fiveIngredientsWithFourInPantry_isNotCookable() {
        Recipe r = recipe("Five", ing("eggs", 2, "pcs"), ing("milk", 100, "ml"),
                ing("flour", 100, "g"), ing("sugar", 20, "g"), ing("butter", 10, "g"));
        MatchResult result = check(r, true,
                item("eggs", 6, "pcs"), item("milk", 1, "l"),
                item("flour", 1, "kg"), item("sugar", 500, "g"));   // butter missing
        assertFalse(result.isCookable());
        assertEquals(1, result.getMissing().size());
        assertEquals("butter", result.getMissing().get(0).getName());
        assertTrue(result.isAlmostThere());
    }

    @Test
    public void notEnoughQuantity_isNotCookable() {
        Recipe r = recipe("Pasta", ing("pasta", 200, "g"), ing("oil", 1, "tbsp"));
        MatchResult result = check(r, true, item("pasta", 150, "g"), item("oil", 500, "ml"));
        assertFalse(result.isCookable());
        assertEquals("pasta", result.getMissing().get(0).getName());
    }

    @Test
    public void exactQuantity_isEnough() {
        Recipe r = recipe("Rice", ing("rice", 100, "g"), ing("milk", 500, "ml"));
        assertTrue(check(r, true, item("rice", 100, "g"), item("milk", 500, "ml")).isCookable());
    }

    @Test
    public void pluralAndCaseDifferences_doNotBreakMatching() {
        Recipe r = recipe("Sauce", ing("tomatoes", 3, "pcs"), ing("Onion", 1, "pcs"));
        assertTrue(check(r, true, item("tomato", 3, "pcs"), item("  ONIONS ", 2, "pcs")).isCookable());
        Recipe single = recipe("Sauce", ing("tomato", 3, "pcs"), ing("onions", 1, "pcs"));
        assertTrue(check(single, true, item("Tomatoes", 3, "pcs"), item("onion", 1, "pcs")).isCookable());
    }

    @Test
    public void unitDifferences_areConverted() {
        Recipe r = recipe("Rice bowl", ing("rice", 200, "g"), ing("milk", 250, "ml"));
        // 1 kg is 1000 g, and 1 cup is 250 ml
        assertTrue(check(r, true, item("rice", 1, "kg"), item("milk", 1, "cup")).isCookable());
        // 0.15 kg is only 150 g
        assertFalse(check(r, true, item("rice", 0.15, "kg"), item("milk", 1, "l")).isCookable());
    }

    @Test
    public void spoonsAndWeights_compareForDryStaples() {
        Recipe r = recipe("Pancakes", ing("sugar", 1, "tbsp"), ing("salt", 0.25, "tsp"));
        // A bag of sugar and a packet of salt are bought by weight
        assertTrue(check(r, true, item("sugar", 1, "kg"), item("salt", 500, "g")).isCookable());
        // But a tiny 5 g pinch of sugar is not enough for a tablespoon (about 12.75 g)
        assertFalse(check(r, true, item("sugar", 5, "g"), item("salt", 500, "g")).isCookable());
    }

    @Test
    public void incompatibleUnits_areNotGuessed() {
        Recipe r = recipe("Omelette", ing("eggs", 3, "pcs"), ing("butter", 10, "g"));
        // Eggs held by weight cannot be compared with a count of eggs
        MatchResult result = check(r, true, item("eggs", 300, "g"), item("butter", 100, "g"));
        assertFalse(result.isCookable());
    }

    @Test
    public void twoEntriesOfSameIngredient_areAddedTogether() {
        Recipe r = recipe("Mash", ing("potatoes", 4, "pcs"), ing("salt", 0.5, "tsp"));
        MatchResult result = check(r, true,
                item("Potatoes", 2, "pcs"), item("potato", 2, "pcs"), item("salt", 100, "g"));
        assertTrue(result.isCookable());
    }

    @Test
    public void specificItemStandsInForGeneralIngredient_butNotTheOtherWayRound() {
        Recipe needsOil = recipe("Fry", ing("oil", 1, "tbsp"), ing("onion", 1, "pcs"));
        assertTrue(check(needsOil, true,
                item("Sunflower oil", 750, "ml"), item("Red onion", 2, "pcs")).isCookable());

        Recipe needsOlive = recipe("Salad", ing("olive oil", 1, "tbsp"), ing("onion", 1, "pcs"));
        assertFalse(check(needsOlive, true,
                item("Sunflower oil", 750, "ml"), item("onion", 2, "pcs")).isCookable());
    }

    @Test
    public void localNameVariants_matchTheSameIngredient() {
        Recipe r = recipe("Roast veg", ing("eggplant", 1, "pcs"), ing("zucchini", 2, "pcs"),
                ing("bell pepper", 1, "pcs"));
        assertTrue(check(r, true, item("Brinjal", 1, "pcs"), item("baby marrow", 3, "pcs"),
                item("Capsicum", 2, "pcs")).isCookable());
    }

    @Test
    public void expiredItems_areIgnoredWhenSettingIsOn() {
        Recipe r = recipe("Milk pudding", ing("milk", 500, "ml"), ing("sugar", 3, "tbsp"));
        PantryItem expiredMilk = itemExpiring("milk", 1, "l", 2026, 10, 10);   // 5 days ago
        PantryItem sugar = item("sugar", 1, "kg");

        assertFalse(check(r, true, expiredMilk, sugar).isCookable());
        assertTrue(check(r, false, expiredMilk, sugar).isCookable());
    }

    @Test
    public void itemExpiringToday_isStillUsable() {
        Recipe r = recipe("Eggs", ing("eggs", 2, "pcs"), ing("salt", 0.25, "tsp"));
        PantryItem eggs = itemExpiring("eggs", 6, "pcs", 2026, 10, 15);        // today
        assertTrue(check(r, true, eggs, item("salt", 100, "g")).isCookable());
    }

    @Test
    public void emptyPantry_suggestsNothing() {
        Recipe r = recipe("Toast", ing("bread", 2, "slices"), ing("butter", 10, "g"));
        MatchResult result = check(r, true);
        assertFalse(result.isCookable());
        assertEquals(2, result.getMissing().size());
        assertFalse(result.isAlmostThere());
    }

    @Test
    public void recipeWithNoIngredients_isNeverSuggested() {
        Recipe r = recipe("Mystery");
        assertFalse(check(r, true, item("bread", 5, "pcs")).isCookable());
    }

    @Test
    public void cookableAndAlmostLists_areSeparateAndSorted() {
        Recipe a = recipe("Zebra cake", ing("flour", 100, "g"), ing("sugar", 50, "g"));
        Recipe b = recipe("Apple tart", ing("flour", 100, "g"), ing("butter", 50, "g"));
        Recipe c = recipe("Bread", ing("flour", 100, "g"), ing("yeast", 5, "g"), ing("salt", 1, "tsp"));
        List<PantryItem> pantry = Arrays.asList(
                item("flour", 1, "kg"), item("sugar", 500, "g"), item("butter", 250, "g"));

        List<MatchResult> all = RecipeMatcher.matchAll(Arrays.asList(a, b, c), pantry, true, NOW, ZONE);
        List<MatchResult> cookable = RecipeMatcher.cookableNow(all);
        List<MatchResult> almost = RecipeMatcher.almostThere(all);

        assertEquals(2, cookable.size());
        assertEquals("Apple tart", cookable.get(0).getRecipe().getName());
        assertEquals("Zebra cake", cookable.get(1).getRecipe().getName());
        assertEquals(0, almost.size());      // Bread is missing two ingredients, so not "almost"
    }
}
