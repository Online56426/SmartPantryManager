package com.example.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

/**
 * Creates and upgrades the SQLite database. As in the study guide, this class only
 * deals with the structure of the database. Reading and writing rows is done in
 * PantryDataSource.
 *
 * Three tables:
 *   pantry_item        what the user has at home (full CRUD from the app)
 *   recipe             the seeded recipe collection
 *   recipe_ingredient  the ingredients each recipe needs (many rows per recipe)
 */
public class PantryDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "PantryDbHelper";

    private static final String DATABASE_NAME = "smartpantry.db";
    private static final int DATABASE_VERSION = 1;

    // Table and column names, shared with PantryDataSource so they are only typed once.
    public static final String TABLE_PANTRY = "pantry_item";
    public static final String TABLE_RECIPE = "recipe";
    public static final String TABLE_RECIPE_INGREDIENT = "recipe_ingredient";

    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY = "expiry";
    public static final String COL_PREP_MINUTES = "prep_minutes";
    public static final String COL_STEPS = "steps";
    public static final String COL_RECIPE_ID = "recipe_id";

    private static final String CREATE_TABLE_PANTRY =
            "CREATE TABLE " + TABLE_PANTRY + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_NAME + " TEXT NOT NULL, "
                    + COL_QUANTITY + " REAL NOT NULL, "
                    + COL_UNIT + " TEXT NOT NULL, "
                    + COL_EXPIRY + " INTEGER NOT NULL DEFAULT 0);";

    private static final String CREATE_TABLE_RECIPE =
            "CREATE TABLE " + TABLE_RECIPE + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_NAME + " TEXT NOT NULL, "
                    + COL_PREP_MINUTES + " INTEGER NOT NULL, "
                    + COL_STEPS + " TEXT NOT NULL);";

    private static final String CREATE_TABLE_RECIPE_INGREDIENT =
            "CREATE TABLE " + TABLE_RECIPE_INGREDIENT + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_RECIPE_ID + " INTEGER NOT NULL, "
                    + COL_NAME + " TEXT NOT NULL, "
                    + COL_QUANTITY + " REAL NOT NULL, "
                    + COL_UNIT + " TEXT NOT NULL, "
                    + "FOREIGN KEY (" + COL_RECIPE_ID + ") REFERENCES "
                    + TABLE_RECIPE + "(" + COL_ID + ") ON DELETE CASCADE);";

    public PantryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // SQLite ignores foreign keys unless this is switched on for each connection.
        db.setForeignKeyConstraintsEnabled(true);
    }

    /** Runs only the first time the database file is created, which is when we seed the recipes. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PANTRY);
        db.execSQL(CREATE_TABLE_RECIPE);
        db.execSQL(CREATE_TABLE_RECIPE_INGREDIENT);
        seedRecipes(db);
    }

    /**
     * For version 1 an upgrade simply rebuilds the tables. That would wipe user data, so a
     * real release would use ALTER TABLE instead (the study guide makes the same point).
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    private void seedRecipes(SQLiteDatabase db) {
        int recipeCount = 0;
        for (Recipe recipe : SeedData.getRecipes()) {
            ContentValues recipeValues = new ContentValues();
            recipeValues.put(COL_NAME, recipe.getName());
            recipeValues.put(COL_PREP_MINUTES, recipe.getPrepMinutes());
            recipeValues.put(COL_STEPS, recipe.getSteps());
            long recipeId = db.insert(TABLE_RECIPE, null, recipeValues);

            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                ContentValues ingredientValues = new ContentValues();
                ingredientValues.put(COL_RECIPE_ID, recipeId);
                ingredientValues.put(COL_NAME, ingredient.getName());
                ingredientValues.put(COL_QUANTITY, ingredient.getQuantity());
                ingredientValues.put(COL_UNIT, ingredient.getUnit());
                db.insert(TABLE_RECIPE_INGREDIENT, null, ingredientValues);
            }
            recipeCount++;
        }
        Log.i(TAG, "Seeded " + recipeCount + " recipes");
    }
}
