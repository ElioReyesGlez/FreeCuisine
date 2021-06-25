package com.erg.freecuisine.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.erg.freecuisine.R;
import com.erg.freecuisine.interfaces.OnRecipeListener;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.TagModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RecommendedRecipesAdapter extends RecyclerView.Adapter<RecommendedRecipesAdapter.ViewHolder> {

    private static final String TAG = "RecipesAdapter";

    private List<RecipeModel> recipes;
    private Context context;
    private LayoutInflater inflater;
    private OnRecipeListener onRecipeListener;

    public RecommendedRecipesAdapter(List<RecipeModel> recipes, Context context,
                                     OnRecipeListener onRecipeListener) {
        this.recipes = recipes;
        this.context = context;
        this.onRecipeListener = onRecipeListener;
        inflater = LayoutInflater.from(context);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_recommend_recipe_card, parent, false);
        return new ViewHolder(view, onRecipeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeModel recipe = recipes.get(position);

        Picasso.get()
                .load(recipe.getImage().getUrl())
                .error(R.drawable.ic_lunch_chef)
                .placeholder(R.drawable.ic_loading_icon)
                .into(holder.recipeMainImg);
        holder.recipeTitle.setText(recipe.getTitle());
        if (recipe.getTags() != null && !recipe.getTags().isEmpty()) {
            holder.tagView.setText(recipe.getTags().get(0).getText());
        } else {
            holder.tagView.setVisibility(View.GONE);
        }
    }

    public List<RecipeModel> getRecipes() {
        return this.recipes;
    }

    public void setRecipes(List<RecipeModel> recipes) {
        this.recipes = recipes;
    }

    private int getColor(int color) {
        return ContextCompat.getColor(context, color);
    }

    @Override
    public int getItemCount() {
        return this.recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ShapeableImageView recipeMainImg;
        TextView recipeTitle;
        TextView tagView;

        OnRecipeListener onRecipeListener;

        public ViewHolder(View itemView, OnRecipeListener onRecipeListener) {
            super(itemView);
            recipeMainImg = itemView.findViewById(R.id.recipe_main_image);
            recipeTitle = itemView.findViewById(R.id.recipe_title);
            tagView = itemView.findViewById(R.id.filter_first);
            this.onRecipeListener = onRecipeListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: Item clicked!");
            onRecipeListener.onRecipeClick(getAdapterPosition(), v);
        }
    }

    public void refreshAdapter(List<RecipeModel> list) {
        this.recipes = list;
        notifyDataSetChanged();
    }

}
