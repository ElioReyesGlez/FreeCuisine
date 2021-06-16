package com.erg.freecuisine.interfaces;

import android.view.View;

import com.erg.freecuisine.models.RecipeModel;

import java.util.ArrayList;

public interface OnRecipeListener {
    void onRecipeClick(int position, View view);
    void onRecipesLoaded(ArrayList<RecipeModel> auxList);
}
