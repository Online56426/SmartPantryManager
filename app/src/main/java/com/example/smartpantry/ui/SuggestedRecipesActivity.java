package com.example.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.AppPreferences;
import com.example.smartpantry.data.PantryDataSource;
import com.example.smartpantry.logic.MatchResult;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen 3: Suggested Recipes. Runs the strict matching rule against the current
 * pantry and lists only the recipes that can be cooked right now.
 *
 * The optional switch adds a second, clearly separate list of recipes that are
 * exactly one ingredient short. Those never appear in the strict list.
 */
public class SuggestedRecipesActivity extends BaseNavActivity
        implements RecipeAdapter.RecipeClickListener {

    private TextView tvSuggestedHeader;
    private TextView tvSuggestedEmpty;
    private RecyclerView rvSuggested;
    private LinearLayout almostSection;
    private TextView tvAlmostHeader;
    private TextView tvAlmostEmpty;
    private RecyclerView rvAlmost;
    private SwitchMaterial swAlmost;

    private RecipeAdapter suggestedAdapter;
    private RecipeAdapter almostAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        tvSuggestedHeader = findViewById(R.id.tvSuggestedHeader);
        tvSuggestedEmpty = findViewById(R.id.tvSuggestedEmpty);
        rvSuggested = findViewById(R.id.rvSuggested);
        almostSection = findViewById(R.id.almostSection);
        tvAlmostHeader = findViewById(R.id.tvAlmostHeader);
        tvAlmostEmpty = findViewById(R.id.tvAlmostEmpty);
        rvAlmost = findViewById(R.id.rvAlmost);
        swAlmost = findViewById(R.id.swAlmost);

        suggestedAdapter = new RecipeAdapter(false, this);
        rvSuggested.setLayoutManager(new LinearLayoutManager(this));
        rvSuggested.setAdapter(suggestedAdapter);

        almostAdapter = new RecipeAdapter(true, this);
        rvAlmost.setLayoutManager(new LinearLayoutManager(this));
        rvAlmost.setAdapter(almostAdapter);

        swAlmost.setOnCheckedChangeListener((button, isChecked) ->
                almostSection.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        setupBottomNav(R.id.nav_recipes);
    }

    /** Matching is redone every time the screen appears, so it always reflects the latest pantry. */
    @Override
    protected void onResume() {
        super.onResume();
        refreshSuggestions();
    }

    private void refreshSuggestions() {
        List<Recipe> recipes = new ArrayList<>();
        List<PantryItem> pantry = new ArrayList<>();

        PantryDataSource dataSource = new PantryDataSource(this);
        try {
            dataSource.open();
            recipes = dataSource.getAllRecipes();
            pantry = dataSource.getItems(false);
        } catch (Exception e) {
            Toast.makeText(this, R.string.load_failed, Toast.LENGTH_LONG).show();
        } finally {
            dataSource.close();
        }

        // The strict matching rule lives in RecipeMatcher (plain Java, unit tested).
        List<MatchResult> all = RecipeMatcher.matchAll(recipes, pantry,
                AppPreferences.isExcludeExpired(this), System.currentTimeMillis(),
                ZoneId.systemDefault());
        List<MatchResult> cookable = RecipeMatcher.cookableNow(all);
        List<MatchResult> almost = RecipeMatcher.almostThere(all);

        suggestedAdapter.setResults(cookable);
        tvSuggestedHeader.setText(getString(R.string.suggested_header, cookable.size()));
        // Feedback instead of a blank screen when nothing matches.
        tvSuggestedEmpty.setVisibility(cookable.isEmpty() ? View.VISIBLE : View.GONE);
        rvSuggested.setVisibility(cookable.isEmpty() ? View.GONE : View.VISIBLE);

        almostAdapter.setResults(almost);
        tvAlmostHeader.setText(getString(R.string.almost_header, almost.size()));
        tvAlmostEmpty.setVisibility(almost.isEmpty() ? View.VISIBLE : View.GONE);
        rvAlmost.setVisibility(almost.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
