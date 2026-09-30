package com.example.smartpantry.data;

import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The starter recipe collection that is loaded into the database the first time
 * the app runs (see PantryDbHelper.onCreate). Every recipe is a mix of everyday
 * ingredients so a normal pantry can match several of them.
 *
 * Steps are written one per line. The detail screen numbers them.
 */
public final class SeedData {

    private SeedData() {
    }

    public static List<Recipe> getRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        recipes.add(recipe("Scrambled Eggs on Toast", 10,
                "Beat the eggs with the salt in a bowl.\n"
                        + "Melt the butter in a pan over low heat.\n"
                        + "Pour in the eggs and stir gently until softly set.\n"
                        + "Toast the bread and serve the eggs on top.",
                ing("eggs", 3, "pcs"), ing("bread", 2, "slices"),
                ing("butter", 10, "g"), ing("salt", 0.25, "tsp")));

        recipes.add(recipe("Cheese Omelette", 10,
                "Beat the eggs with the salt.\n"
                        + "Melt the butter in a pan and pour in the eggs.\n"
                        + "When the base is set, sprinkle the cheese over half of it.\n"
                        + "Fold the omelette over and cook for one more minute.",
                ing("eggs", 3, "pcs"), ing("cheese", 40, "g"),
                ing("butter", 10, "g"), ing("salt", 0.25, "tsp")));

        recipes.add(recipe("Tomato Pasta", 20,
                "Boil the pasta in salted water until tender, then drain.\n"
                        + "Heat the oil and fry the crushed garlic for one minute.\n"
                        + "Add the chopped tomatoes and simmer for ten minutes.\n"
                        + "Toss the pasta through the sauce and season to taste.",
                ing("pasta", 200, "g"), ing("tomatoes", 4, "pcs"), ing("garlic", 2, "cloves"),
                ing("oil", 2, "tbsp"), ing("salt", 0.5, "tsp")));

        recipes.add(recipe("Egg Fried Rice", 15,
                "Cook the rice, spread it on a plate and let it cool.\n"
                        + "Heat the oil and soften the chopped onion.\n"
                        + "Push the onion aside, scramble the eggs in the pan.\n"
                        + "Add the rice and soy sauce and stir fry until hot.",
                ing("rice", 150, "g"), ing("eggs", 2, "pcs"), ing("onion", 1, "pcs"),
                ing("oil", 1, "tbsp"), ing("soy sauce", 1, "tbsp")));

        recipes.add(recipe("Pancakes", 20,
                "Whisk the flour, sugar and salt together.\n"
                        + "Add the milk and eggs and whisk into a smooth batter.\n"
                        + "Heat a little oil in a pan.\n"
                        + "Pour in a ladle of batter and cook each side until golden.",
                ing("flour", 150, "g"), ing("milk", 250, "ml"), ing("eggs", 2, "pcs"),
                ing("sugar", 1, "tbsp"), ing("oil", 1, "tbsp"), ing("salt", 0.25, "tsp")));

        recipes.add(recipe("Creamy Mashed Potatoes", 30,
                "Peel the potatoes and cut them into chunks.\n"
                        + "Boil in salted water for about 20 minutes until soft, then drain.\n"
                        + "Add the butter and warm milk.\n"
                        + "Mash until smooth and season to taste.",
                ing("potatoes", 4, "pcs"), ing("butter", 30, "g"),
                ing("milk", 60, "ml"), ing("salt", 0.5, "tsp")));

        recipes.add(recipe("Cheese Toastie", 10,
                "Butter one side of each slice of bread.\n"
                        + "Place the cheese between the slices with the buttered sides out.\n"
                        + "Toast in a pan or sandwich press until golden and melted.",
                ing("bread", 2, "slices"), ing("cheese", 50, "g"), ing("butter", 10, "g")));

        recipes.add(recipe("Tomato and Onion Scramble", 15,
                "Heat the oil and soften the chopped onion.\n"
                        + "Add the chopped tomatoes and cook until they break down.\n"
                        + "Pour in the beaten eggs and salt.\n"
                        + "Stir gently until the eggs are set.",
                ing("eggs", 3, "pcs"), ing("tomatoes", 2, "pcs"), ing("onion", 1, "pcs"),
                ing("oil", 1, "tbsp"), ing("salt", 0.25, "tsp")));

        recipes.add(recipe("Pan Fried Potatoes", 30,
                "Cut the potatoes into small cubes and slice the onion.\n"
                        + "Heat the oil in a large pan.\n"
                        + "Fry the potatoes for about 15 minutes, turning often.\n"
                        + "Add the onion, salt and pepper and fry until golden.",
                ing("potatoes", 4, "pcs"), ing("onion", 1, "pcs"), ing("oil", 2, "tbsp"),
                ing("salt", 0.5, "tsp"), ing("black pepper", 0.25, "tsp")));

        recipes.add(recipe("Chicken and Rice Bowl", 40,
                "Season the chicken with salt and pepper.\n"
                        + "Fry the chicken in oil until cooked through, then slice it.\n"
                        + "Fry the onion and garlic in the same pan.\n"
                        + "Cook the rice, then serve topped with the chicken and onion.",
                ing("chicken breasts", 2, "pcs"), ing("rice", 200, "g"), ing("onion", 1, "pcs"),
                ing("garlic", 2, "cloves"), ing("oil", 1, "tbsp"), ing("salt", 0.5, "tsp"),
                ing("black pepper", 0.25, "tsp")));

        recipes.add(recipe("Spaghetti Bolognese", 45,
                "Fry the chopped onion and garlic in the oil.\n"
                        + "Add the mince and brown it well.\n"
                        + "Add the chopped tomatoes and salt, then simmer for 25 minutes.\n"
                        + "Boil the pasta, drain and serve with the sauce.",
                ing("pasta", 250, "g"), ing("beef mince", 300, "g"), ing("onion", 1, "pcs"),
                ing("tomatoes", 4, "pcs"), ing("garlic", 2, "cloves"), ing("oil", 1, "tbsp"),
                ing("salt", 0.5, "tsp")));

        recipes.add(recipe("Macaroni Cheese", 30,
                "Boil the pasta until tender and drain.\n"
                        + "Melt the butter, stir in the flour and cook for one minute.\n"
                        + "Slowly whisk in the milk until the sauce thickens.\n"
                        + "Stir in the cheese and salt, then mix through the pasta.",
                ing("pasta", 200, "g"), ing("cheese", 150, "g"), ing("milk", 300, "ml"),
                ing("butter", 30, "g"), ing("flour", 30, "g"), ing("salt", 0.25, "tsp")));

        recipes.add(recipe("French Toast", 15,
                "Whisk the eggs, milk and sugar together in a shallow dish.\n"
                        + "Soak each slice of bread in the mixture.\n"
                        + "Melt the butter in a pan.\n"
                        + "Fry the bread on both sides until golden brown.",
                ing("bread", 4, "slices"), ing("eggs", 2, "pcs"), ing("milk", 100, "ml"),
                ing("sugar", 1, "tbsp"), ing("butter", 10, "g")));

        recipes.add(recipe("Garlic Bread", 15,
                "Heat the oven to 200 degrees Celsius.\n"
                        + "Mash the soft butter with the crushed garlic and salt.\n"
                        + "Spread the garlic butter on the bread.\n"
                        + "Bake for ten minutes until crisp and golden.",
                ing("bread", 4, "slices"), ing("butter", 40, "g"), ing("garlic", 2, "cloves"),
                ing("salt", 0.25, "tsp")));

        recipes.add(recipe("Creamy Rice Pudding", 45,
                "Put the rice, milk and sugar in a heavy pot.\n"
                        + "Bring to a gentle simmer, stirring often.\n"
                        + "Cook for about 35 minutes until thick and creamy.\n"
                        + "Serve warm.",
                ing("rice", 100, "g"), ing("milk", 500, "ml"), ing("sugar", 3, "tbsp")));

        recipes.add(recipe("Vegetable Stir Fry", 20,
                "Slice the carrots, pepper and onion into thin strips.\n"
                        + "Heat the oil in a large pan or wok.\n"
                        + "Stir fry the carrots first, then add the rest with the garlic.\n"
                        + "Add the soy sauce and toss for two more minutes.",
                ing("carrots", 2, "pcs"), ing("bell pepper", 1, "pcs"), ing("onion", 1, "pcs"),
                ing("garlic", 2, "cloves"), ing("soy sauce", 2, "tbsp"), ing("oil", 1, "tbsp")));

        recipes.add(recipe("Tuna Pasta", 20,
                "Boil the pasta until tender and drain.\n"
                        + "Heat the oil and soften the chopped onion and garlic.\n"
                        + "Add the drained tuna and pepper and warm through.\n"
                        + "Toss the pasta through the tuna mixture.",
                ing("pasta", 200, "g"), ing("tuna", 1, "tin"), ing("onion", 1, "pcs"),
                ing("garlic", 1, "cloves"), ing("oil", 1, "tbsp"), ing("black pepper", 0.25, "tsp")));

        recipes.add(recipe("Baked Beans on Toast", 10,
                "Warm the baked beans in a small pot.\n"
                        + "Toast the bread and spread it with butter.\n"
                        + "Spoon the beans over the toast.",
                ing("baked beans", 1, "tin"), ing("bread", 2, "slices"), ing("butter", 10, "g")));

        recipes.add(recipe("Peanut Butter and Banana Toast", 5,
                "Toast the bread.\n"
                        + "Spread the peanut butter on the warm toast.\n"
                        + "Slice the banana on top.",
                ing("bread", 2, "slices"), ing("peanut butter", 2, "tbsp"),
                ing("banana", 1, "pcs")));

        recipes.add(recipe("Pap with Tomato and Onion Gravy", 30,
                "Bring water to the boil and stir in the maize meal to make a stiff pap.\n"
                        + "Cover and cook on low heat for 20 minutes.\n"
                        + "Fry the onion in oil, then add the chopped tomatoes and salt.\n"
                        + "Simmer the gravy until thick and serve over the pap.",
                ing("maize meal", 250, "g"), ing("tomatoes", 3, "pcs"), ing("onion", 1, "pcs"),
                ing("oil", 1, "tbsp"), ing("salt", 1, "tsp")));

        return recipes;
    }

    private static Recipe recipe(String name, int prepMinutes, String steps,
                                 RecipeIngredient... ingredients) {
        return new Recipe(Recipe.NO_ID, name, prepMinutes, steps,
                new ArrayList<>(Arrays.asList(ingredients)));
    }

    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }
}
