package com.erg.freecuisine.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.erg.freecuisine.R;
import com.erg.freecuisine.controller.network.helpers.RealmHelper;
import com.erg.freecuisine.interfaces.OnRecipeListener;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.TagModel;
import com.erg.freecuisine.util.Util;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.ViewHolder> {

    private static final String TAG = "RecipesAdapter";

    private List<RecipeModel> recipes;
    private final Context context;
    private final OnRecipeListener onRecipeListener;
    private final RealmHelper realmHelper;

    public RecipesAdapter(List<RecipeModel> recipes, Context context,
                          OnRecipeListener onRecipeListener) {
        this.recipes = recipes;
        this.context = context;
        this.onRecipeListener = onRecipeListener;
        realmHelper = new RealmHelper();
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_card, parent, false);
        return new ViewHolder(view, onRecipeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        RecipeModel recipe = recipes.get(position);

        Picasso.get()
                .load(recipe.getImage().getUrl())
                .error(R.drawable.ic_lunch_chef_mini)
                .placeholder(R.drawable.ic_loading_icon)
                .into(holder.recipeMainImg);
        holder.recipeTitle.setText(recipe.getTitle());
        holder.recipeDescription.setText(recipe.getDescription());
        holder.cockingTime.setText(recipe.getTime());
        holder.peopleAmount.setText(String.valueOf(recipe.getDiners()));

        checkBookmark(realmHelper.exists(recipe), holder.ivBookmark);

        if (recipe.getTags() != null && !recipe.getTags().isEmpty()) {
            List<TagModel> tags = recipe.getTags();
            if (tags.get(0) != null) {
                TagModel firstTag = recipe.getTags().get(0);
                holder.firstFilter.setText(firstTag.getText());
            }
        }
    }

    private void checkBookmark(boolean flag, ImageView ivBookmark) {
        if (flag) {
            Util.showView(null, ivBookmark);
        } else {
            Util.hideView(null, ivBookmark);
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

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ShapeableImageView recipeMainImg;
        public ImageView ivBookmark;
        public TextView recipeTitle;
        public TextView recipeDescription;
        public TextView cockingTime;
        public TextView peopleAmount;
        public TextView type;
        public TextView firstFilter;

        OnRecipeListener onRecipeListener;

        public ViewHolder(View itemView, OnRecipeListener onRecipeListener) {
            super(itemView);
            recipeMainImg = itemView.findViewById(R.id.recipe_main_image);
            ivBookmark = itemView.findViewById(R.id.iv_bookmark);
            recipeTitle = itemView.findViewById(R.id.recipe_title);
            recipeDescription = itemView.findViewById(R.id.recipe_description);
            cockingTime = itemView.findViewById(R.id.tv_cocking_time);
            peopleAmount = itemView.findViewById(R.id.tv_people_amount);
            type = itemView.findViewById(R.id.tv_type);
            firstFilter = itemView.findViewById(R.id.filter_first);
            this.onRecipeListener = onRecipeListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: Item clicked!");
            onRecipeListener.onRecipeClick(getAdapterPosition(), v);
        }
    }

    public void clearAll() {
        this.recipes.clear();
        notifyDataSetChanged();
    }

    public void refreshAdapter(List<RecipeModel> list) {
        this.recipes = list;
        notifyDataSetChanged();
    }

}
