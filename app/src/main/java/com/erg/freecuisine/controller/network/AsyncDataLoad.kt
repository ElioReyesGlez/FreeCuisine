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

    fun loadRecipesAsync(contextActivity: FragmentActivity,
                         onRecipeListener: OnRecipeListener, link: LinkModel) {
        scopeLoader.launch {
            val auxList: ArrayList<RecipeModel> = JsoupController.getRecipesByLink(link)
            if (auxList.isNotEmpty()) {
                contextActivity.runOnUiThread {
                    onRecipeListener.onRecipesLoaded(auxList)
                    if (isActive) cancel()
                }
            }
        }
    }

    fun loadSingleRecipeAsync(contextActivity: FragmentActivity,
                              onRecipeListener: OnRecipeListener,
                              link: String,
                              tags: ArrayList<TagModel>): RecipeModel {
        var recipe = RecipeModel()
        scopeLoader2.launch {
            recipe = JsoupController.getRecipe(link, tags)
            contextActivity.runOnUiThread {
                onRecipeListener.onSingleRecipeLoaded(recipe)
                if (isActive) cancel()
            }
        }
        return recipe
    }
}