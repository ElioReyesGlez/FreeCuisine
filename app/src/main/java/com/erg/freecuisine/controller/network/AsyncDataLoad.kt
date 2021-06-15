@file:JvmName("AsyncDataLoad")

package com.erg.freecuisine.controller.network

import androidx.fragment.app.FragmentActivity
import com.erg.freecuisine.models.RecipeModel
import com.erg.freecuisine.models.TagModel
import com.erg.freecuisine.ui.SingleRecipeFragment
import com.erg.freecuisine.ui.RecipesFragment
import com.erg.freecuisine.util.Util
import kotlinx.coroutines.*
import java.util.ArrayList

class AsyncDataLoad {

    private val scopeLoader = CoroutineScope(Dispatchers.IO + CoroutineName("scopeLoader"))
    private val scopeLoader2 = CoroutineScope(Dispatchers.IO + CoroutineName("scopeLoader2"))

    fun loadRecipesAsync(contextActivity: FragmentActivity, fragment: RecipesFragment, tag: String) {
        val job = scopeLoader.launch {
            val auxList: ArrayList<RecipeModel> = JsoupController.getRecipesByTag(tag)
            if (auxList.isNotEmpty()) {
                contextActivity.runOnUiThread {
                    fragment.recipes = auxList
                    fragment.recipesAdapter.refreshAdapter(auxList)
                    fragment.refreshView()
                }
            }
            if (isActive)
                cancel()
        }
    }

    fun loadSingleRecipeAsync(contextActivity: FragmentActivity,
                              singleRecipeFragment: SingleRecipeFragment,
                              link: String,
                              tags: ArrayList<TagModel>): RecipeModel {
        var recipe = RecipeModel()
        val job = scopeLoader2.launch {
            recipe = JsoupController.getRecipe(link, tags)
            contextActivity.runOnUiThread {
                singleRecipeFragment.setUpView(recipe)
            }
            if (isActive)
                cancel()
        }
        return recipe
    }
}