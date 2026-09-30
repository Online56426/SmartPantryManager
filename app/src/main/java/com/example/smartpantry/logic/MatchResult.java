package com.example.smartpantry.logic;

import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/** The outcome of checking one recipe against the pantry. */
public class MatchResult {

    private final Recipe recipe;
    private final List<RecipeIngredient> missing;

    public MatchResult(Recipe recipe, List<RecipeIngredient> missing) {
        this.recipe = recipe;
        this.missing = new ArrayList<>(missing);
    }

    public Recipe getRecipe() {
        return recipe;
    }

    /** Ingredients the pantry does not cover (absent, or not enough of it). */
    public List<RecipeIngredient> getMissing() {
        return missing;
    }

    public boolean isMissing(RecipeIngredient ingredient) {
        return missing.contains(ingredient);
    }

    /**
     * The strict rule: every single ingredient is covered. A recipe with no
     * ingredients listed is never suggested, otherwise it would match an empty pantry.
     */
    public boolean isCookable() {
        return !recipe.getIngredients().isEmpty() && missing.isEmpty();
    }

    /** Exactly one ingredient short, and at least one other ingredient is already there. */
    public boolean isAlmostThere() {
        return recipe.getIngredients().size() > 1 && missing.size() == 1;
    }
}
