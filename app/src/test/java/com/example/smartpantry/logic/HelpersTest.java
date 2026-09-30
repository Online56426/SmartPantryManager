package com.example.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.time.ZoneId;
import java.util.Locale;

/** Tests for the smaller helper classes used by the matcher and the forms. */
public class HelpersTest {

    private static final ZoneId ZONE = ZoneId.of("Africa/Johannesburg");

    // IngredientNormalizer

    @Test
    public void normaliser_handlesCaseSpacesAndPlurals() {
        assertEquals("tomato", IngredientNormalizer.canonical("Tomatoes"));
        assertEquals("tomato", IngredientNormalizer.canonical("  TOMATO "));
        assertEquals("egg", IngredientNormalizer.canonical("Eggs"));
        assertEquals("potato", IngredientNormalizer.canonical("potatoes"));
        assertEquals("berry", IngredientNormalizer.canonical("berries"));
        assertEquals("peach", IngredientNormalizer.canonical("peaches"));
    }

    @Test
    public void normaliser_dropsFillerWordsAndPunctuation() {
        assertEquals("onion", IngredientNormalizer.canonical("Fresh, chopped onions"));
        assertEquals("cheese", IngredientNormalizer.canonical("grated cheese"));
    }

    @Test
    public void normaliser_keepsWordsThatEndInSs() {
        assertEquals("hummus", IngredientNormalizer.canonical("hummus"));
        assertEquals("couscous", IngredientNormalizer.canonical("couscous"));
    }

    @Test
    public void normaliser_mapsLocalNames() {
        assertEquals("eggplant", IngredientNormalizer.canonical("Brinjal"));
        assertEquals("zucchini", IngredientNormalizer.canonical("Baby marrow"));
        assertEquals("chili", IngredientNormalizer.canonical("chilli"));
        assertEquals("maize meal", IngredientNormalizer.canonical("Mielie meal"));
        assertEquals("black pepper", IngredientNormalizer.canonical("pepper"));
    }

    @Test
    public void normaliser_addsGeneralKeyForSpecificItems() {
        assertTrue(IngredientNormalizer.matchKeys("Sunflower oil").contains("oil"));
        assertTrue(IngredientNormalizer.matchKeys("Sunflower oil").contains("sunflower oil"));
        assertFalse(IngredientNormalizer.matchKeys("oil").contains("sunflower oil"));
        assertTrue(IngredientNormalizer.matchKeys("   ").isEmpty());
    }

    // UnitConverter

    @Test
    public void units_convertWithinFamilies() {
        assertEquals(1500, UnitConverter.toBase(1.5, "kg").amount, 1e-9);
        assertEquals(UnitConverter.FAMILY_MASS, UnitConverter.toBase(1, "KG").family);
        assertEquals(750, UnitConverter.toBase(3, "cups").amount, 1e-9);
        assertEquals(15, UnitConverter.toBase(1, "tbsp").amount, 1e-9);
        assertEquals(UnitConverter.FAMILY_COUNT, UnitConverter.toBase(2, "pcs").family);
        assertEquals(UnitConverter.FAMILY_COUNT, UnitConverter.toBase(2, "").family);
    }

    @Test
    public void units_unknownUnitOnlyMatchesItself() {
        UnitConverter.Measure a = UnitConverter.toBase(1, "bunch");
        UnitConverter.Measure b = UnitConverter.toBase(1, "Bunch");
        assertEquals(a.family, b.family);
        assertFalse(a.family.equals(UnitConverter.FAMILY_COUNT));
    }

    @Test
    public void units_densityOnlyAppliesToKnownStaples() {
        assertEquals(UnitConverter.FAMILY_MASS, UnitConverter.toBase(1, "tbsp", "sugar").family);
        assertEquals(UnitConverter.FAMILY_VOLUME, UnitConverter.toBase(1, "tbsp", "vinegar").family);
    }

    // QuantityFormat

    @Test
    public void quantity_parsesDotAndCommaDecimals() {
        assertEquals(2.0, QuantityFormat.parse("2"), 0);
        assertEquals(0.5, QuantityFormat.parse("0.5"), 0);
        assertEquals(0.5, QuantityFormat.parse("0,5"), 0);
        assertEquals(0.5, QuantityFormat.parse(" .5 "), 0);
    }

    @Test
    public void quantity_rejectsBadInput() {
        String[] bad = {"", "abc", "1e3", "-2", "1.2.3", "1 2", "NaN", "5."};
        for (String text : bad) {
            try {
                QuantityFormat.parse(text);
                fail("Should have rejected: " + text);
            } catch (NumberFormatException expected) {
                // good
            }
        }
    }

    @Test
    public void quantity_formatsWithoutTrailingZeros() {
        assertEquals("3", QuantityFormat.format(3.0));
        assertEquals("0.25", QuantityFormat.format(0.25));
        assertEquals("1.5", QuantityFormat.format(1.5));
    }

    // InputValidator

    @Test
    public void validator_checksName() {
        assertEquals(InputValidator.NameProblem.EMPTY, InputValidator.checkName(""));
        assertEquals(InputValidator.NameProblem.EMPTY, InputValidator.checkName("   "));
        assertEquals(InputValidator.NameProblem.NO_LETTERS, InputValidator.checkName("123"));
        assertEquals(InputValidator.NameProblem.TOO_LONG,
                InputValidator.checkName("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopq"));
        assertEquals(InputValidator.NameProblem.NONE, InputValidator.checkName("Baby marrow"));
    }

    @Test
    public void validator_checksQuantity() {
        assertEquals(InputValidator.QuantityProblem.EMPTY, InputValidator.checkQuantity(""));
        assertEquals(InputValidator.QuantityProblem.NOT_A_NUMBER, InputValidator.checkQuantity("two"));
        assertEquals(InputValidator.QuantityProblem.NOT_A_NUMBER, InputValidator.checkQuantity("-3"));
        assertEquals(InputValidator.QuantityProblem.NOT_POSITIVE, InputValidator.checkQuantity("0"));
        assertEquals(InputValidator.QuantityProblem.TOO_LARGE, InputValidator.checkQuantity("100001"));
        assertEquals(InputValidator.QuantityProblem.NONE, InputValidator.checkQuantity("0,5"));
    }

    // ExpiryUtil

    @Test
    public void expiry_countsCalendarDays() {
        long now = ExpiryUtil.startOfDayMillis(2026, 10, 15, ZONE) + 23L * 3600_000L;  // 23:00
        long tomorrow = ExpiryUtil.startOfDayMillis(2026, 10, 16, ZONE);
        long yesterday = ExpiryUtil.startOfDayMillis(2026, 10, 14, ZONE);
        long today = ExpiryUtil.startOfDayMillis(2026, 10, 15, ZONE);

        assertEquals(1, ExpiryUtil.daysUntil(tomorrow, now, ZONE));
        assertEquals(0, ExpiryUtil.daysUntil(today, now, ZONE));
        assertEquals(-1, ExpiryUtil.daysUntil(yesterday, now, ZONE));
        assertTrue(ExpiryUtil.isExpired(yesterday, now, ZONE));
        assertFalse(ExpiryUtil.isExpired(today, now, ZONE));
        assertFalse(ExpiryUtil.isExpired(0, now, ZONE));      // no expiry date
    }

    @Test
    public void expiry_formatsDate() {
        long day = ExpiryUtil.startOfDayMillis(2026, 3, 5, ZONE);
        assertEquals("5 Mar 2026", ExpiryUtil.formatDate(day, ZONE, Locale.ENGLISH));
    }
}
