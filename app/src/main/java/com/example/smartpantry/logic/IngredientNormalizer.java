package com.example.smartpantry.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Makes ingredient names comparable, so that "Tomatoes", "tomato " and "TOMATO"
 * all end up as the same key.
 *
 * The steps are: lower case and tidy the text, drop filler words such as "fresh",
 * turn each word into its singular form, then apply a small table of local
 * spelling and naming variants (brinjal, aubergine and eggplant are one thing).
 *
 * It is deliberately not a full language solution. The singular rules do not need
 * to be perfect English, they only need to be applied the same way to both the
 * pantry name and the recipe name.
 */
public final class IngredientNormalizer {

    private static final Set<String> FILLER_WORDS = new HashSet<>(Arrays.asList(
            "fresh", "ripe", "large", "medium", "small", "chopped", "diced",
            "sliced", "grated", "raw", "organic"));

    /** Plurals that the general rules would get wrong. */
    private static final Map<String, String> IRREGULAR_PLURALS = new HashMap<>();

    /** Different names for exactly the same ingredient. Keys are normalised at start up. */
    private static final Map<String, String> ALIASES = new HashMap<>();

    /**
     * A specific ingredient that can stand in for a more general one. A pantry item
     * called "sunflower oil" also counts as "oil", but a recipe that asks for
     * "olive oil" will not accept plain "oil".
     */
    private static final Map<String, String> PARENTS = new HashMap<>();

    static {
        IRREGULAR_PLURALS.put("loaves", "loaf");
        IRREGULAR_PLURALS.put("leaves", "leaf");
        IRREGULAR_PLURALS.put("halves", "half");
        IRREGULAR_PLURALS.put("cookies", "cookie");
        IRREGULAR_PLURALS.put("brownies", "brownie");
        IRREGULAR_PLURALS.put("smoothies", "smoothie");

        alias("eggplant", "aubergine", "brinjal");
        alias("zucchini", "courgette", "baby marrow");
        alias("bell pepper", "capsicum", "green pepper", "red pepper", "yellow pepper",
                "sweet pepper");
        alias("scallion", "spring onion", "green onion");
        alias("chili", "chilli", "chile", "chilli pepper", "chili pepper");
        alias("yogurt", "yoghurt");
        alias("maize meal", "mielie meal", "mealie meal", "super maize meal");
        alias("beef mince", "mince", "minced beef", "ground beef");
        alias("black pepper", "pepper", "ground pepper", "ground black pepper");
        alias("soy sauce", "soya sauce");
        alias("tuna", "tinned tuna", "canned tuna", "tuna fish");
        alias("chicken breast", "chicken breast fillet", "chicken fillet",
                "boneless chicken breast");

        parent("oil", "olive oil", "sunflower oil", "vegetable oil", "canola oil", "cooking oil");
        parent("sugar", "brown sugar", "white sugar", "caster sugar");
        parent("cheese", "cheddar", "cheddar cheese", "mozzarella", "mozzarella cheese",
                "gouda", "gouda cheese");
        parent("flour", "cake flour", "bread flour", "self raising flour", "plain flour",
                "all purpose flour");
        parent("milk", "full cream milk", "low fat milk", "skim milk", "long life milk");
        parent("rice", "white rice", "brown rice", "basmati rice", "jasmine rice",
                "long grain rice");
        parent("pasta", "spaghetti", "penne", "macaroni", "fusilli", "tagliatelle");
        parent("onion", "red onion", "white onion", "brown onion");
        parent("tomato", "cherry tomato", "roma tomato");
        parent("bread", "white bread", "brown bread", "whole wheat bread", "wholewheat bread");
        parent("butter", "salted butter", "unsalted butter");
        parent("salt", "sea salt", "table salt", "coarse salt");
    }

    private IngredientNormalizer() {
    }

    private static void alias(String canonicalName, String... variants) {
        String target = tidyAndSingularise(canonicalName);
        ALIASES.put(target, target);
        for (String variant : variants) {
            ALIASES.put(tidyAndSingularise(variant), target);
        }
    }

    private static void parent(String generalName, String... specificNames) {
        String general = tidyAndSingularise(generalName);
        for (String specific : specificNames) {
            PARENTS.put(tidyAndSingularise(specific), general);
        }
    }

    /** The single standard form of an ingredient name, used as the lookup key. */
    public static String canonical(String rawName) {
        String tidy = tidyAndSingularise(rawName);
        String aliased = ALIASES.get(tidy);
        return aliased != null ? aliased : tidy;
    }

    /**
     * Every key a pantry item should be filed under: its own canonical name, plus the
     * general ingredient it can stand in for (if any).
     */
    public static Set<String> matchKeys(String rawName) {
        Set<String> keys = new LinkedHashSet<>();
        String own = canonical(rawName);
        if (own.isEmpty()) {
            return keys;
        }
        keys.add(own);
        String general = PARENTS.get(own);
        if (general != null) {
            keys.add(general);
        }
        return keys;
    }

    private static String tidyAndSingularise(String rawName) {
        if (rawName == null) {
            return "";
        }
        // Keep letters and spaces only, so hyphens, commas and brackets do not matter.
        String cleaned = rawName.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L} ]", " ").trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String word : cleaned.split("\\s+")) {
            if (FILLER_WORDS.contains(word)) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(singular(word));
        }
        return result.toString();
    }

    private static String singular(String word) {
        String irregular = IRREGULAR_PLURALS.get(word);
        if (irregular != null) {
            return irregular;
        }
        int length = word.length();
        if (length <= 3) {
            return word;
        }
        if (word.endsWith("ies")) {
            return word.substring(0, length - 3) + "y";          // berries -> berry
        }
        if (word.endsWith("oes")) {
            return word.substring(0, length - 2);                // tomatoes -> tomato
        }
        if (word.endsWith("ches") || word.endsWith("shes")
                || word.endsWith("sses") || word.endsWith("xes")) {
            return word.substring(0, length - 2);                // peaches -> peach
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;                                         // hummus, couscous, swiss
        }
        if (word.endsWith("s")) {
            return word.substring(0, length - 1);                // eggs -> egg
        }
        return word;
    }
}
