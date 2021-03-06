package com.erg.freecuisine.interfaces;

import android.view.View;

import com.erg.freecuisine.models.RecipeModel;

import java.util.ArrayList;

public interface OnRecipeListener {
    void onRecipeClick(int position, View view);
    void onSingleRecipeLoaded(RecipeModel recipe);
    void onRecipesLoaded(ArrayList<RecipeModel> recipes);
    void onLoaderFailed(String url, Exception e);
    void onRecommendedRecipesLoaded(ArrayList<RecipeModel> recipes);
    void onTipsRecipesLoaded(ArrayList<RecipeModel> recipes);
}
