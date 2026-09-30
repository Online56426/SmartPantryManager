package com.example.smartpantry.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantry.logic.UnitConverter;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Checks the seeded recipe collection meets the brief (15 to 20 recipes, sensible content). */
public class SeedDataTest {

    @Test
    public void hasBetweenFifteenAndTwentyRecipes() {
        int count = SeedData.getRecipes().size();
        assertTrue("Recipe count was " + count, count >= 15 && count <= 20);
    }

    @Test
    public void everyRecipeHasNameStepsAndIngredients() {
        Set<String> names = new HashSet<>();
        for (Recipe recipe : SeedData.getRecipes()) {
            assertFalse(recipe.getName().trim().isEmpty());
            assertTrue("Duplicate recipe name: " + recipe.getName(), names.add(recipe.getName()));
            assertTrue(recipe.getName() + " needs steps", recipe.getStepList().length >= 2);
            assertTrue(recipe.getName() + " needs 2+ ingredients", recipe.getIngredients().size() >= 2);
            assertTrue(recipe.getPrepMinutes() > 0);
        }
    }

    @Test
    public void everyIngredientHasAKnownUnitAndPositiveQuantity() {
        List<Recipe> recipes = SeedData.getRecipes();
        for (Recipe recipe : recipes) {
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                assertTrue(recipe.getName() + ": " + ingredient.getName(),
                        ingredient.getQuantity() > 0);
                String family = UnitConverter.toBase(ingredient.getQuantity(), ingredient.getUnit()).family;
                assertFalse(recipe.getName() + " uses an unknown unit: " + ingredient.getUnit(),
                        family.startsWith("other:"));
            }
        }
    }
}
