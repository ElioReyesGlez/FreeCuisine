package com.erg.freecuisine.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;

import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.TagModel;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import static com.erg.freecuisine.util.Constants.MIN_VIBRATE_TIME;
import static com.erg.freecuisine.util.Constants.NEWLINE;
import static com.erg.freecuisine.util.Constants.SPECIAL_MIN_VIBRATE_TIME;
import static com.erg.freecuisine.util.Constants.SPECIAL_VIBRATE_TIME;
import static com.erg.freecuisine.util.Constants.VIBRATE_TIME;

public class Util {

    private static final String TAG = "Util";

    public static List<RecipeModel> findByTags(List<TagModel> tags,
                                               List<RecipeModel> recipes) {
        List<RecipeModel> aux = new ArrayList<>();

        for (RecipeModel recipe : recipes) {
            for (TagModel tag : tags) {
                if (recipe.getTags().contains(tag)) {
                    if (!aux.contains(recipe))
                        aux.add(recipe);
                }
            }
        }
        return aux;
    }

    public static void showView(Animation anim, View view) {
        if (view != null)
            if (view.getVisibility() == View.GONE || view.getVisibility() == View.INVISIBLE) {
                if (anim != null)
                    view.startAnimation(anim);
                view.setVisibility(View.VISIBLE);
            } else {
                if (anim != null) {
                    view.startAnimation(anim);
                }
            }
    }

    public static void hideView(Animation anim, View view) {
        if (view != null)
            if (view.getVisibility() == View.VISIBLE) {
                if (anim != null)
                    view.setAnimation(anim);
                view.setVisibility(View.GONE);
            }
    }

    public static void vibrate(Context context) {
//        ToDo
//        SharedPreferencesHelper spHelper = new SharedPreferencesHelper(context);
//        if (spHelper.getVibrationStatus()) {
            long VIBRATION_TIME = getRightVibrationTime(context, true);
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

            assert v != null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(VIBRATION_TIME,
                        VibrationEffect.DEFAULT_AMPLITUDE));
                Log.d(TAG, "regular vibrate: VIBRATE_TIME VibrationEffect :" + VIBRATION_TIME);
            } else {
                v.vibrate(VIBRATION_TIME);
                Log.d(TAG, "regular vibrate: VIBRATE_TIME :" + VIBRATION_TIME);
            }
//        }
    }

    public static void vibrateMin(Context context) {
//        SharedPreferencesHelper spHelper = new SharedPreferencesHelper(context);
//        if (spHelper.getVibrationStatus()) {
            long VIBRATION_TIME = getRightVibrationTime(context, false);
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

            assert v != null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(VIBRATION_TIME,
                        VibrationEffect.DEFAULT_AMPLITUDE));
                Log.d(TAG, "vibrateMin: MIN_VIBRATE_TIME VibrationEffect " + VIBRATION_TIME);
            } else {
                v.vibrate(VIBRATION_TIME);
                Log.d(TAG, "vibrateMin: MIN_VIBRATE_TIME " + VIBRATION_TIME);
            }
//        }
    }

    private static long getRightVibrationTime(Context context, boolean isRegular) {
        if (isRegular) {
            for (String brand : Constants.getBrands(context)) {
                if (Build.MANUFACTURER.toLowerCase().contains(brand.toLowerCase())) {
                    return SPECIAL_VIBRATE_TIME;
                }
                Log.d(TAG, "regular vibrate: flagSwitchVibrationTime: false");
            }
            return VIBRATE_TIME;
        } else {
            for (String brand : Constants.getBrands(context)) {
                if (Build.MANUFACTURER.toLowerCase().contains(brand.toLowerCase())) {
                    return SPECIAL_MIN_VIBRATE_TIME;
                }
                Log.d(TAG, "regular vibrate: flagSwitchVibrationTime: false");
            }
            return MIN_VIBRATE_TIME;
        }
    }

    public static String[] extractIngredients (String strIngredients) {
        return strIngredients.split("%");
    }

    public static String getUrlByTag(String tag) {
        return "https://www.recetasgratis.net/Recetas-de-Carne-listado_receta-10_1.html"; // ToDo
    }
}
