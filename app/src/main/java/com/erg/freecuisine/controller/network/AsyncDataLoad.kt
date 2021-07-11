@file:JvmName("AsyncDataLoad")

package com.erg.freecuisine.controller.network

import androidx.fragment.app.FragmentActivity
import com.erg.freecuisine.interfaces.OnRecipeListener
import com.erg.freecuisine.models.LinkModel
import com.erg.freecuisine.models.RecipeModel
import com.erg.freecuisine.models.TagModel
import kotlinx.coroutines.*
import java.util.*

class AsyncDataLoad {

    private val scopeLoader1 = CoroutineScope(Dispatchers.IO + CoroutineName("scopeLoader1"))
    private val scopeLoader2 = CoroutineScope(Dispatchers.IO + CoroutineName("scopeLoader2"))
    private val scopeLoader3 = CoroutineScope(Dispatchers.IO + CoroutineName("scopeLoader3"))
    private val scopeLoader4 = CoroutineScope(Dispatchers.IO + CoroutineName("scopeLoader4"))
    private val scopeLoader5 = CoroutineScope(Dispatchers.IO + CoroutineName("scopeLoader5"))

    fun loadRecipesAsync(contextActivity: FragmentActivity,
                         onRecipeListener: OnRecipeListener,
                         links: List<LinkModel>): Job {
        return scopeLoader1.launch {
            val recipes: ArrayList<RecipeModel> = ArrayList()
            for (link in links) {
                recipes.addAll(JsoupController.getRecipesByLink(link, onRecipeListener, true))
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
                    .getRecommendedRecipes(link, onRecipeListener)
            if (recipes.isNotEmpty()) {
                contextActivity.runOnUiThread {
                    onRecipeListener.onRecommendedRecipesLoaded(recipes)
                    if (isActive) cancel()
                }
            }
        }
    }

    fun loadRecommendRecipesAsync2(contextActivity: FragmentActivity,
                                  onRecipeListener: OnRecipeListener, links: List<LinkModel>): Job {
        return scopeLoader5.launch {
            val recipes: ArrayList<RecipeModel> = ArrayList()
            for (link in links) {
                val aux = JsoupController.getRecipesByLink(link, onRecipeListener, false)
                val randomPos = (0 until aux.size).random()
                recipes.add(aux[randomPos])
            }
            if (recipes.isNotEmpty()) {
                contextActivity.runOnUiThread {
                    onRecipeListener.onRecipesLoaded(recipes)
                    if (isActive) cancel()
                }
            }
        }
    }

    fun loadTipsRecipesAsync(contextActivity: FragmentActivity,
                             onRecipeListener: OnRecipeListener, link: LinkModel): Job {
        return scopeLoader4.launch {
            val recipes: ArrayList<RecipeModel> = ArrayList()
            val aux = JsoupController.getTipsRecipes(link, onRecipeListener)
            aux.shuffle()
            for (i in 0..7) {
                val recipe = aux[i]
                if (!recipes.contains(recipe)) {
                    recipes.add(recipe)
                }
            }

            if (recipes.isNotEmpty()) {
                contextActivity.runOnUiThread {
                    onRecipeListener.onTipsRecipesLoaded(recipes)
                    if (isActive) cancel()
                }
            }
        }
    }
}