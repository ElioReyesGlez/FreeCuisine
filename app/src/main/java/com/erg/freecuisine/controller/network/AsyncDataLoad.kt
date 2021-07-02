@file:JvmName("AsyncDataLoad")

package com.erg.freecuisine.controller.network

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.erg.freecuisine.interfaces.OnRecipeListener
import com.erg.freecuisine.models.LinkModel
import com.erg.freecuisine.models.RecipeModel
import com.erg.freecuisine.models.TagModel
import kotlinx.coroutines.*
import java.util.ArrayList

class AsyncDataLoad {

    private val scopeLoader1 = CoroutineScope(Dispatchers.IO + CoroutineName("scopeLoader1"))
    private val scopeLoader2 = CoroutineScope(Dispatchers.IO + CoroutineName("scopeLoader2"))
    private val scopeLoader3 = CoroutineScope(Dispatchers.IO + CoroutineName("scopeLoader3"))
    private val scopeLoader4 = CoroutineScope(Dispatchers.IO + CoroutineName("scopeLoader4"))

    fun loadRecipesAsync(contextActivity: FragmentActivity,
                         onRecipeListener: OnRecipeListener,
                         links: List<LinkModel>): Job {
        return scopeLoader1.launch {
            val recipes: ArrayList<RecipeModel> = ArrayList()
            for (link in links) {
                recipes.addAll(JsoupController.getRecipesByLink(link, onRecipeListener))
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
            val recipe = JsoupController.getRecipe(link, tags, onRecipeListener)
            contextActivity.runOnUiThread {
                onRecipeListener.onSingleRecipeLoaded(recipe)
                if (isActive) cancel()
            }
        }
    }

    fun loadRecommendRecipesAsync(contextActivity: FragmentActivity,
                                  onRecipeListener: OnRecipeListener, link: LinkModel): Job {
        return scopeLoader3.launch {
            val recipes: ArrayList<RecipeModel> = JsoupController
                    .getRecommendedRecipe(link, onRecipeListener)
            if (recipes.isNotEmpty()) {
                contextActivity.runOnUiThread {
                    onRecipeListener.onRecipesLoaded(recipes)
                    if (isActive) cancel()
                }
            }
        }
    }

    fun loadTipRecipesAsync(contextActivity: FragmentActivity,
                            onRecipeListener: OnRecipeListener, link: LinkModel): Job {
        return scopeLoader4.launch {
            val recipes: ArrayList<RecipeModel> = JsoupController.getTipRecipes(link)
            if (recipes.isNotEmpty()) {
                contextActivity.runOnUiThread {
                    onRecipeListener.onRecipesLoaded(recipes)
                    if (isActive) cancel()
                }
            }
        }
    }
}