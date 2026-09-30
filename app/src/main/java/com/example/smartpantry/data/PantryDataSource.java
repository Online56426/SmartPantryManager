package com.example.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * All database reads and writes go through this class. Activities open it, run
 * one or two calls, and close it again straight away, following the pattern in
 * the study guide (ContactDataSource).
 *
 * Pantry items support full CRUD. Recipes are read only.
 */
public class PantryDataSource {

    private SQLiteDatabase database;
    private final PantryDbHelper dbHelper;

    public PantryDataSource(Context context) {
        dbHelper = new PantryDbHelper(context.getApplicationContext());
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // ---------------------------------------------------------------------
    // Pantry items: Create, Read, Update, Delete
    // ---------------------------------------------------------------------

    /** Create. Returns true if a new row was inserted. */
    public boolean insertItem(PantryItem item) {
        try {
            long rowId = database.insert(PantryDbHelper.TABLE_PANTRY, null, toValues(item));
            if (rowId != -1) {
                item.setId(rowId);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /** Update. Returns true if exactly one existing row was changed. */
    public boolean updateItem(PantryItem item) {
        try {
            int changed = database.update(PantryDbHelper.TABLE_PANTRY, toValues(item),
                    PantryDbHelper.COL_ID + " = ?", new String[]{String.valueOf(item.getId())});
            return changed > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** Delete. Returns true if a row was removed. */
    public boolean deleteItem(long id) {
        try {
            int removed = database.delete(PantryDbHelper.TABLE_PANTRY,
                    PantryDbHelper.COL_ID + " = ?", new String[]{String.valueOf(id)});
            return removed > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Read all. Sorted by name, or by soonest expiry with items that have no
     * expiry date at the end. The ORDER BY text is chosen here, never taken from
     * user input.
     */
    public List<PantryItem> getItems(boolean sortByExpiry) {
        List<PantryItem> items = new ArrayList<>();
        String orderBy = sortByExpiry
                ? "CASE WHEN " + PantryDbHelper.COL_EXPIRY + " = 0 THEN 1 ELSE 0 END, "
                + PantryDbHelper.COL_EXPIRY + " ASC, " + PantryDbHelper.COL_NAME + " COLLATE NOCASE ASC"
                : PantryDbHelper.COL_NAME + " COLLATE NOCASE ASC";

        Cursor cursor = database.query(PantryDbHelper.TABLE_PANTRY, null, null, null,
                null, null, orderBy);
        try {
            while (cursor.moveToNext()) {
                items.add(itemFromCursor(cursor));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    /** Read one, used to fill the edit form. Returns null if the id no longer exists. */
    public PantryItem getItem(long id) {
        Cursor cursor = database.query(PantryDbHelper.TABLE_PANTRY, null,
                PantryDbHelper.COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
        try {
            return cursor.moveToFirst() ? itemFromCursor(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    // ---------------------------------------------------------------------
    // Recipes (read only)
    // ---------------------------------------------------------------------

    /** Every recipe with its ingredients attached, in alphabetical order. */
    public List<Recipe> getAllRecipes() {
        Map<Long, List<RecipeIngredient>> ingredientsByRecipe = loadIngredients(-1);
        List<Recipe> recipes = new ArrayList<>();

        Cursor cursor = database.query(PantryDbHelper.TABLE_RECIPE, null, null, null,
                null, null, PantryDbHelper.COL_NAME + " COLLATE NOCASE ASC");
        try {
            while (cursor.moveToNext()) {
                recipes.add(recipeFromCursor(cursor, ingredientsByRecipe));
            }
        } finally {
            cursor.close();
        }
        return recipes;
    }

    /** One recipe with its ingredients, or null if it does not exist. */
    public Recipe getRecipe(long id) {
        Map<Long, List<RecipeIngredient>> ingredientsByRecipe = loadIngredients(id);
        Cursor cursor = database.query(PantryDbHelper.TABLE_RECIPE, null,
                PantryDbHelper.COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
        try {
            return cursor.moveToFirst() ? recipeFromCursor(cursor, ingredientsByRecipe) : null;
        } finally {
            cursor.close();
        }
    }

    // ---------------------------------------------------------------------
    // Helpers that convert between rows and objects
    // ---------------------------------------------------------------------

    private ContentValues toValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(PantryDbHelper.COL_NAME, item.getName());
        values.put(PantryDbHelper.COL_QUANTITY, item.getQuantity());
        values.put(PantryDbHelper.COL_UNIT, item.getUnit());
        values.put(PantryDbHelper.COL_EXPIRY, item.getExpiryMillis());
        return values;
    }

    private PantryItem itemFromCursor(Cursor cursor) {
        return new PantryItem(
                cursor.getLong(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_UNIT)),
                cursor.getLong(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_EXPIRY)));
    }

    private Recipe recipeFromCursor(Cursor cursor, Map<Long, List<RecipeIngredient>> ingredients) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_ID));
        List<RecipeIngredient> list = ingredients.get(id);
        return new Recipe(
                id,
                cursor.getString(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_NAME)),
                cursor.getInt(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_PREP_MINUTES)),
                cursor.getString(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_STEPS)),
                list == null ? new ArrayList<RecipeIngredient>() : list);
    }

    /**
     * Loads recipe ingredients in one query and groups them by recipe id.
     *
     * @param onlyRecipeId a recipe id to load, or -1 to load the ingredients of every recipe
     */
    private Map<Long, List<RecipeIngredient>> loadIngredients(long onlyRecipeId) {
        Map<Long, List<RecipeIngredient>> grouped = new HashMap<>();
        String selection = onlyRecipeId == -1 ? null : PantryDbHelper.COL_RECIPE_ID + " = ?";
        String[] args = onlyRecipeId == -1 ? null : new String[]{String.valueOf(onlyRecipeId)};

        Cursor cursor = database.query(PantryDbHelper.TABLE_RECIPE_INGREDIENT, null,
                selection, args, null, null, PantryDbHelper.COL_ID + " ASC");
        try {
            while (cursor.moveToNext()) {
                long recipeId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(PantryDbHelper.COL_RECIPE_ID));
                RecipeIngredient ingredient = new RecipeIngredient(
                        cursor.getString(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_UNIT)));
                List<RecipeIngredient> list = grouped.get(recipeId);
                if (list == null) {
                    list = new ArrayList<>();
                    grouped.put(recipeId, list);
                }
                list.add(ingredient);
            }
        } finally {
            cursor.close();
        }
        return grouped;
    }
}
