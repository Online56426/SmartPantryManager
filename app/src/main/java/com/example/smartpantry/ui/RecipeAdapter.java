package com.example.smartpantry.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.logic.MatchResult;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Feeds both recipe lists on the Suggested Recipes screen. The strict list uses
 * it as is, and the optional "almost there" list turns on showMissing so each
 * row also names the one ingredient that is missing.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface RecipeClickListener {
        void onRecipeClicked(Recipe recipe);
    }

    private List<MatchResult> results = new ArrayList<>();
    private final boolean showMissing;
    private final RecipeClickListener listener;

    public RecipeAdapter(boolean showMissing, RecipeClickListener listener) {
        this.showMissing = showMissing;
        this.listener = listener;
    }

    public void setResults(List<MatchResult> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvMeta;
        final TextView tvMissing;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvMeta = itemView.findViewById(R.id.tvRecipeMeta);
            tvMissing = itemView.findViewById(R.id.tvRecipeMissing);
        }
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        MatchResult result = results.get(position);
        final Recipe recipe = result.getRecipe();
        Context context = holder.itemView.getContext();

        holder.tvName.setText(recipe.getName());
        holder.tvMeta.setText(context.getString(R.string.recipe_meta,
                recipe.getPrepMinutes(), recipe.getIngredients().size()));

        if (showMissing && !result.getMissing().isEmpty()) {
            StringBuilder names = new StringBuilder();
            for (RecipeIngredient missing : result.getMissing()) {
                if (names.length() > 0) {
                    names.append(", ");
                }
                names.append(missing.getName());
            }
            holder.tvMissing.setText(context.getString(R.string.missing_label, names));
            holder.tvMissing.setVisibility(View.VISIBLE);
        } else {
            holder.tvMissing.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClicked(recipe));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }
}
