package com.erg.freecuisine.models;

import androidx.annotation.Nullable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmModule;

@RealmModule(classes = {RealmRecipeModel.class})
public class RealmRecipeModel extends RealmObject {
    @PrimaryKey
    private String id = "";
    private String jsonRecipe = "";

    public static String ID_COLUMN_NAME = "id";
    public static String JSON_RECIPE_COLUMN_NAME = "jsonRecipe";

    public RealmRecipeModel() {
    }

    public RealmRecipeModel(String id, String jsonRecipe) {
        this.id = id;
        this.jsonRecipe = jsonRecipe;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJsonRecipe() {
        return jsonRecipe;
    }

    public void setJsonRecipe(String jsonRecipe) {
        this.jsonRecipe = jsonRecipe;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof RealmRecipeModel) {
            RealmRecipeModel comparable = (RealmRecipeModel) obj;
            return getId().equals(comparable.getId());
        } else {
            return false;
        }
    }
}
