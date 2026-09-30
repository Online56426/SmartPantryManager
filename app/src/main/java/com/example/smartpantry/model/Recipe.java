package com.example.smartpantry.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe: the ingredients it needs, a name, and simple preparation steps.
 * Steps are stored as one text block with one step per line.
 */
public class Recipe {

    public static final long NO_ID = -1;

    private final long id;
    private final String name;
    private final int prepMinutes;
    private final String steps;
    private final List<RecipeIngredient> ingredients;

    public Recipe(long id, String name, int prepMinutes, String steps,
                  List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.prepMinutes = prepMinutes;
        this.steps = steps;
        this.ingredients = ingredients == null ? new ArrayList<RecipeIngredient>() : ingredients;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrepMinutes() {
        return prepMinutes;
    }

    public String getSteps() {
        return steps;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    /** The method split into individual steps, ready to be numbered on screen. */
    public String[] getStepList() {
        if (steps == null || steps.trim().isEmpty()) {
            return new String[0];
        }
        return steps.trim().split("\\n");
    }
}
