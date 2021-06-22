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

public class LoadingAdapter extends RecyclerView.Adapter<LoadingAdapter.ViewHolder> {

    private static final String TAG = "RecipesAdapter";

    private List<RecipeModel> recipes;
    private Context context;
    private LayoutInflater inflater;
    private int layout;

    public LoadingAdapter(List<RecipeModel> recipes, Context context, int layout) {
        this.recipes = recipes;
        this.context = context;
        this.layout = layout;
        inflater = LayoutInflater.from(context);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //        Empty
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

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
