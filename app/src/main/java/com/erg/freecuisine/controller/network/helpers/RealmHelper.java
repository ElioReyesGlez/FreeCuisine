package com.erg.freecuisine.controller.network.helpers;

import com.erg.freecuisine.models.JsonRecipeModel;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.erg.freecuisine.util.Constants.REALM_DATA_BASE_NAME;

public class RealmHelper {
    private Realm realm;

    private final RealmConfiguration config;

    public RealmHelper() {
        config = new RealmConfiguration.Builder()
                .name(REALM_DATA_BASE_NAME)
                .allowQueriesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .compactOnLaunch()
                .inMemory()
                .build();

        realm = Realm.getInstance(config);
    }

    public JsonRecipeModel getRecipeById(String _id) {
        return realm
                .where(JsonRecipeModel.class)
                .equalTo("_id", _id)
                .findFirst();
    }

    public void insertOrUpdate(JsonRecipeModel jsonRecipe) {
        realm.executeTransaction(r -> r.insertOrUpdate(jsonRecipe));
    }

    public void addAll(ArrayList<JsonRecipeModel> jsonRecipes) {
        for (JsonRecipeModel jsonRecipe : jsonRecipes) {
            insertOrUpdate(jsonRecipe);
        }
    }

    public ArrayList<JsonRecipeModel> getRecipes() {
        RealmResults<JsonRecipeModel> results = realm.where(JsonRecipeModel.class).findAll();
        return new ArrayList<>(realm.copyFromRealm(results));
    }

    public void deleteAll() {
        realm.executeTransaction(r -> r.delete(JsonRecipeModel.class));
    }

    public void deleteRecipe(JsonRecipeModel jsonRecipe) {
        realm.executeTransaction(realm -> {
            RealmResults<JsonRecipeModel> result = realm.where(JsonRecipeModel.class)
                    .equalTo(JsonRecipeModel.Companion.getID_COLUMN_NAME(), jsonRecipe.get_id())
                    .findAll();
            result.deleteFirstFromRealm();
        });
    }
}
