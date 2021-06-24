package com.erg.freecuisine.controller.network.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.erg.freecuisine.R;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.util.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

import static com.erg.freecuisine.util.Constants.DECIMAL_PLACE;
import static com.erg.freecuisine.util.Constants.LAST_RECIPE_READ_KEY;
import static com.erg.freecuisine.util.Constants.LAST_USAGE_KEY;
import static com.erg.freecuisine.util.Constants.OPEN_TIME_USAGE_KEY;
import static com.erg.freecuisine.util.Constants.PREF_APP_FIRST_LAUNCH_KEY;
import static com.erg.freecuisine.util.Constants.SHARED_PREF_NAME;

public class SharedPreferencesHelper {

    private static final String TAG = "SharedPreferencesHelper";

    private final Context context;
    private final SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;


    public SharedPreferencesHelper(Context context) {
        this.context = context;
        this.sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Activity.MODE_PRIVATE);
    }

    public boolean isEmpty() {
        return sharedPref.getAll().isEmpty();
    }


    public void saveUsageOpenTime(long currentTimeMillis) {
        editor = sharedPref.edit();
        editor.putLong(OPEN_TIME_USAGE_KEY, currentTimeMillis);
        editor.apply();
    }

    public long getUsageOpenTime() {
        return sharedPref.getLong(OPEN_TIME_USAGE_KEY, 0);
    }

    public void resetUsageOpenTime() {
        editor = sharedPref.edit();
        editor.remove(OPEN_TIME_USAGE_KEY);
        editor.apply();
    }

    public boolean existKey(String key) {
        return sharedPref.contains(key);
    }

    public Long getLastSyncDate(String key) {
        return sharedPref.getLong(key, 0);
    }

    public void setLastSync(String key, long date) {
        editor = sharedPref.edit();
        editor.putLong(key, date);
        editor.apply();
    }

    public boolean getEvaluatorInfoMessageStatus(String tag) {
        return sharedPref.getBoolean(tag, false);
    }

    public ArrayList<Float> getUserActivity() {
        ArrayList<Float> temp = new ArrayList<>();
        String[] dayCodes = context.getResources().getStringArray(R.array.day_codes);
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        for (int i = 0; i < TimeHelper.getCurrentDayOfWeekInNumber(); i++) {
            String key = dayCodes[i] + (i + 1);
            float value = getUsageValue(key);
            float roundValue = Util.round(value, DECIMAL_PLACE);
            Log.d(TAG, "getUserActivity: ROUND VALUE =" + roundValue);
            temp.add(roundValue);
        }
        Log.d(TAG, "getUserActivity: USER ACTIVITY = " + temp.toString());
        return temp;
    }


    public void saveLastUsage(long date) {
        editor = sharedPref.edit();
        editor.putLong(LAST_USAGE_KEY, date);
        editor.apply();
    }

    public long getLastUsage() {
        return sharedPref.getLong(LAST_USAGE_KEY, 0);
    }

    public void increasesUsageValue(String key, float value) {
        editor = sharedPref.edit();
        float usageValue = getUsageValue(key);
        usageValue += value;
        editor.putFloat(key, usageValue);
        editor.apply();
        Log.d(TAG, "increasesUsageValue: USAGE VALUE = " + usageValue);
    }


    public float getUsageValue(String key) {
        return sharedPref.getFloat(key, 0.0f);
    }

    public void removeUsageValue(String key) {
        editor = sharedPref.edit();
        editor.remove(key);
        editor.apply();
    }

    public void saveLastRecipeRead(RecipeModel recipe) {
        editor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(recipe);
        editor.putString(LAST_RECIPE_READ_KEY, json);
        editor.apply();
    }

    public RecipeModel getLastRecipeRead() {
        Gson gson = new Gson();
        String json = sharedPref.getString(LAST_RECIPE_READ_KEY, null);
        Type type = new TypeToken<RecipeModel>() {
        }.getType();
        try {
            return gson.fromJson(json, type);
        } catch (Exception e) {
            Log.e(TAG, "getLastVerseRead: ERROR: " + e.getMessage());
            return null;
        }
    }
}
