# Smart Pantry Manager

A Java Android app that suggests recipes based only on the ingredients you already have at home, to help cut food waste.

Built for Mobile App Development 700 at Richfield Graduate Institute of Technology.

## What it does

You keep a list of the ingredients in your pantry. The app shows only the recipes you can cook right now. A recipe is suggested only if every ingredient is in the pantry, in at least the quantity the recipe needs.

* Add, edit and delete pantry items, with input validation
* 20 recipes stored in the database, with ingredients and steps
* Suggested Recipes screen, with an optional "Almost there" list for recipes missing one ingredient
* Recipe detail screen with ticks and crosses against your pantry
* Settings for expiry highlights, ignoring expired items and sort order

## Database choice: SQLite

The app uses a local SQLite database through SQLiteOpenHelper. I chose it because the pantry is personal data that must work offline, matching against a local database is instant, and no accounts or servers are needed. The data is also relational, because one recipe has many ingredients.

The tables are pantry_item, recipe and recipe_ingredient. The recipes are added the first time the database is created.

## How the matching works

Ingredient names are tidied up (lower case, singular form), units are converted (for example 1 kg against 200 g), and duplicate entries are added together. A recipe is suggested only when nothing is missing. The code is in `logic/RecipeMatcher.java`.

## Setup and run

You need Android Studio with Android SDK 34. Use the JDK that comes with Android Studio.

1. Clone the repository with `git clone <repository link>`
2. In Android Studio choose File > Open, select the project folder and wait for Gradle to sync (it needs internet the first time).
3. Create an emulator (Android 8.0, API 26 or newer) or connect a phone with USB debugging switched on.
4. Press Run.

To run the unit tests, right click `app/src/test` and choose Run Tests, or use `./gradlew test`.

## Author

Logan Carolus, 402312980