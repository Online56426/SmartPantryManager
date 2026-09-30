package com.example.smartpantry.model;

/**
 * A single line of a recipe, for example "200 g pasta" or "3 eggs".
 * The matcher compares these against the user's pantry items.
 */
public class RecipeIngredient {

    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }
}
