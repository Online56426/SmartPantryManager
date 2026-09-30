package com.example.smartpantry.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartpantry.R;
import com.example.smartpantry.data.AppPreferences;
import com.example.smartpantry.data.PantryDataSource;
import com.example.smartpantry.logic.MatchResult;
import com.example.smartpantry.logic.QuantityFormat;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Screen 4: Recipe Detail. Receives a recipe id through the Intent, loads the
 * recipe, and shows its full ingredient list and method. Each ingredient is
 * ticked or crossed depending on whether the pantry covers it right now.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Key for the recipe id passed in the Intent by SuggestedRecipesActivity. */
    public static final String EXTRA_RECIPE_ID = "com.example.smartpantry.EXTRA_RECIPE_ID";

    private static final String TICK = "✓  ";
    private static final String CROSS = "✗  ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, Recipe.NO_ID);

        Recipe recipe = null;
        List<PantryItem> pantry = new ArrayList<>();
        PantryDataSource dataSource = new PantryDataSource(this);
        try {
            dataSource.open();
            recipe = dataSource.getRecipe(recipeId);
            pantry = dataSource.getItems(false);
        } catch (Exception e) {
            recipe = null;
        } finally {
            dataSource.close();
        }

        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Use the same matcher as the suggestions list so the ticks always agree with it.
        Map<String, Map<String, Double>> totals = RecipeMatcher.buildPantryTotals(pantry,
                AppPreferences.isExcludeExpired(this), System.currentTimeMillis(),
                ZoneId.systemDefault());
        MatchResult result = RecipeMatcher.evaluate(recipe, totals);

        showRecipe(recipe, result);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showRecipe(Recipe recipe, MatchResult result) {
        setTitle(recipe.getName());

        ((TextView) findViewById(R.id.tvDetailName)).setText(recipe.getName());
        ((TextView) findViewById(R.id.tvDetailMeta)).setText(getString(R.string.recipe_meta,
                recipe.getPrepMinutes(), recipe.getIngredients().size()));

        TextView status = findViewById(R.id.tvDetailStatus);
        if (result.getMissing().isEmpty()) {
            status.setText(R.string.recipe_ready);
            status.setTextColor(ContextCompat.getColor(this, R.color.status_ok));
        } else {
            status.setText(getString(R.string.recipe_missing_some,
                    result.getMissing().size(), recipe.getIngredients().size()));
            status.setTextColor(ContextCompat.getColor(this, R.color.status_expired));
        }

        // Ingredient list: a tick for what the pantry covers, a red cross for what it does not.
        SpannableStringBuilder ingredientText = new SpannableStringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            boolean missing = result.isMissing(ingredient);
            int start = ingredientText.length();
            ingredientText.append(missing ? CROSS : TICK)
                    .append(describe(ingredient))
                    .append('\n');
            int colour = missing
                    ? ContextCompat.getColor(this, R.color.status_expired)
                    : ContextCompat.getColor(this, R.color.status_ok);
            ingredientText.setSpan(new ForegroundColorSpan(colour), start, start + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        ((TextView) findViewById(R.id.tvDetailIngredients)).setText(ingredientText);

        // Method: number each line.
        StringBuilder steps = new StringBuilder();
        String[] stepList = recipe.getStepList();
        for (int i = 0; i < stepList.length; i++) {
            if (i > 0) {
                steps.append("\n\n");
            }
            steps.append(i + 1).append(". ").append(stepList[i]);
        }
        ((TextView) findViewById(R.id.tvDetailSteps)).setText(steps.toString());
    }

    /** Reads naturally: "3 eggs", "200 g pasta", "2 tbsp oil". */
    private String describe(RecipeIngredient ingredient) {
        String quantity = QuantityFormat.format(ingredient.getQuantity());
        String unit = ingredient.getUnit();
        if (unit == null || unit.isEmpty() || unit.equalsIgnoreCase("pcs")) {
            return quantity + " " + ingredient.getName();
        }
        return quantity + " " + unit + " " + ingredient.getName();
    }
}
