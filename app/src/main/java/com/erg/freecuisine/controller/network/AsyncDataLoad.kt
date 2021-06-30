@file:JvmName("AsyncDataLoad")

package com.erg.freecuisine.controller.network

import androidx.fragment.app.FragmentActivity
import com.erg.freecuisine.interfaces.OnRecipeListener
import com.erg.freecuisine.models.LinkModel
import com.erg.freecuisine.models.RecipeModel
import com.erg.freecuisine.models.TagModel
import kotlinx.coroutines.*
import java.util.ArrayList

class AsyncDataLoad {

    private val scopeLoader = CoroutineScope(Dispatchers.IO + CoroutineName("scopeLoader"))
    private val scopeLoader2 = CoroutineScope(Dispatchers.IO + CoroutineName("scopeLoader2"))
    private val scopeLoader3 = CoroutineScope(Dispatchers.IO + CoroutineName("scopeLoader3"))

    fun loadRecipesAsync(contextActivity: FragmentActivity,
                         onRecipeListener: OnRecipeListener,
                         links: List<LinkModel>): Job {

        return scopeLoader.launch {
            val recipes: ArrayList<RecipeModel> = ArrayList()
            for (link in links) {
                recipes.addAll(JsoupController.getRecipesByLink(link))
            }
            if (recipes.isNotEmpty()) {
                contextActivity.runOnUiThread {
                    onRecipeListener.onRecipesLoaded(recipes)
                    if (isActive) cancel()
                }
            }
        }
    }

    fun loadSingleRecipeAsync(contextActivity: FragmentActivity,
                              onRecipeListener: OnRecipeListener,
                              link: String,
                              tags: ArrayList<TagModel>): Job {
        return scopeLoader2.launch {
            val recipe = JsoupController.getRecipe(link, tags)
            contextActivity.runOnUiThread {
                onRecipeListener.onSingleRecipeLoaded(recipe)
                if (isActive) cancel()
            }
        }
    }

    fun loadRecommendRecipesAsync(contextActivity: FragmentActivity,
                                  onRecipeListener: OnRecipeListener, link: LinkModel): Job {
        return scopeLoader3.launch {
            val recipes: ArrayList<RecipeModel> = JsoupController.getRecommendedRecipe(link)
            if (recipes.isNotEmpty()) {
                contextActivity.runOnUiThread {
                    onRecipeListener.onRecipesLoaded(recipes)
                    if (isActive) cancel()
                }
            }
        }
    }
}