package com.erg.freecuisine.interfaces;

import android.view.View;

import com.erg.freecuisine.models.RecipeModel;

import java.io.IOException;
import java.util.ArrayList;

public interface OnRecipeListener {
    void onRecipeClick(int position, View view);
    void onSingleRecipeLoaded(RecipeModel recipe);
    void onRecipesLoaded(ArrayList<RecipeModel> recipes);
    void onLoaderFailed(ArrayList<RecipeModel> recipes, Exception e);
}
