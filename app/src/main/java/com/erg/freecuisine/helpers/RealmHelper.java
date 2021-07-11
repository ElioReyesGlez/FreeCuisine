package com.erg.freecuisine.helpers;

import com.erg.freecuisine.models.RealmRecipeModel;
import com.erg.freecuisine.models.RecipeModel;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

import static com.erg.freecuisine.util.Constants.REALM_DATA_BASE_NAME;

public class RealmHelper {
    private final Realm realm;

    public RealmHelper() {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(REALM_DATA_BASE_NAME)
                .modules(new RealmRecipeModel())
                .allowQueriesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .deleteRealmIfMigrationNeeded()
                .build();

        realm = Realm.getInstance(config);
    }

    public RealmRecipeModel getRecipeById(String id) {
        return realm
                .where(RealmRecipeModel.class)
                .equalTo("id", id)
                .findFirst();
    }

    public void insertOrUpdate(RealmRecipeModel jsonRecipe) {
        realm.executeTransaction(r -> r.insertOrUpdate(jsonRecipe));
    }

    public boolean exists(RecipeModel recipe) {
        return getRecipeById(recipe.getId()) != null;
    }

    public void addAll(ArrayList<RealmRecipeModel> jsonRecipes) {
        for (RealmRecipeModel jsonRecipe : jsonRecipes) {
            insertOrUpdate(jsonRecipe);
        }
    }

    public ArrayList<RealmRecipeModel> getRecipes() {
        RealmResults<RealmRecipeModel> results = realm.where(RealmRecipeModel.class).findAll();
        return new ArrayList<>(realm.copyFromRealm(results));
    }

    public void deleteAll() {
        realm.executeTransaction(r -> r.delete(RealmRecipeModel.class));
    }

    public void deleteRecipe(RealmRecipeModel realmRecipeModel) {
        realm.executeTransaction(realm -> {
            RealmResults<RealmRecipeModel> result = realm.where(RealmRecipeModel.class)
                    .equalTo(RealmRecipeModel.ID_COLUMN_NAME, realmRecipeModel.getId())
                    .findAll();
            result.deleteFirstFromRealm();
        });
    }
}
