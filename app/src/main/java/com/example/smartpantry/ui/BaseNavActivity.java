package com.example.smartpantry.ui;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * The Pantry screen is the root. Going back to it clears everything above it, and
 * moving between the other two replaces the current screen, so the Back button
 * always leads to Pantry and then out of the app.
 *
 * Shared behaviour for the three top level screens (Pantry, Recipes, Settings):
 * they all show the same bottom navigation bar. Each subclass calls
 * setupBottomNav() with its own menu item so the right tab is highlighted.
 *
 */
public abstract class BaseNavActivity extends AppCompatActivity {

    protected void setupBottomNav(final int selectedItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Set the highlighted tab before adding the listener so this does not trigger it.
        bottomNav.setSelectedItemId(selectedItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) {
                return false;
            }

            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = PantryListActivity.class;
            } else if (id == R.id.nav_recipes) {
                target = SuggestedRecipesActivity.class;
            } else {
                target = SettingsActivity.class;
            }

            Intent intent = new Intent(this, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            if (target == PantryListActivity.class) {
                // Return to the existing Pantry screen and close the ones above it.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                startActivity(intent);
                if (!(this instanceof PantryListActivity)) {
                    finish();       // swap Recipes and Settings rather than stacking them
                }
            }
            // Returning false keeps this tab highlighted; the new screen highlights its own.
            return false;
        });
    }
}
