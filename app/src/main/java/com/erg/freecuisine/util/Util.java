package com.erg.freecuisine.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.erg.freecuisine.R;
import com.erg.freecuisine.helpers.SharedPreferencesHelper;
import com.erg.freecuisine.helpers.TimeHelper;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.TagModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static com.erg.freecuisine.util.Constants.DIVISION_SING;
import static com.erg.freecuisine.util.Constants.MIN_VIBRATE_TIME;
import static com.erg.freecuisine.util.Constants.SPECIAL_MIN_VIBRATE_TIME;
import static com.erg.freecuisine.util.Constants.SPECIAL_VIBRATE_TIME;
import static com.erg.freecuisine.util.Constants.TAG_KEY;
import static com.erg.freecuisine.util.Constants.URL_KEY;
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
            }
    }

    public static void hideView(Animation anim, View view) {
        if (view != null)
            if (view.getVisibility() == View.VISIBLE) {
                if (anim != null)
                    view.startAnimation(anim);
                view.setVisibility(View.GONE);
            }
    }

    public static void hideViewInvisibleWay(Animation anim, View view) {
        if (view != null)
            if (view.getVisibility() == View.VISIBLE) {
                if (anim != null)
                    view.startAnimation(anim);
                view.setVisibility(View.INVISIBLE);
            }
    }

    public static void hideBottomBar(Activity activity, BottomNavigationView navView) {
        Animation anim = AnimationUtils.loadAnimation(activity, R.anim.custom_exit_anim_faster);
        if (navView != null)
            if (navView.getVisibility() == View.VISIBLE) {
                if (anim != null)
                    navView.startAnimation(anim);
                navView.setVisibility(View.GONE);
            }
    }

    public static void showBottomBar(Activity activity, BottomNavigationView navView) {
        Animation anim = AnimationUtils.loadAnimation(activity, R.anim.custom_enter_anim_faster);
        if (navView != null)
            if (navView.getVisibility() == View.GONE
                    || navView.getVisibility() == View.INVISIBLE) {
                if (anim != null)
                    navView.startAnimation(anim);
                navView.setVisibility(View.VISIBLE);
            }
    }

    public static void hideViewWithDelay(Animation anim, View view) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> hideView(anim, view), TimeHelper.DELAY);
    }

    public static void vibrate(Context context) {
//        ToDo
        SharedPreferencesHelper spHelper = new SharedPreferencesHelper(context);
        if (spHelper.getVibrationStatus()) {
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
        }
    }

    public static void vibrateMin(Context context) {
        SharedPreferencesHelper spHelper = new SharedPreferencesHelper(context);
        if (spHelper.getVibrationStatus()) {
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
        }
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

    public static String[] extractIngredients(String strIngredients) {
        return strIngredients.split(DIVISION_SING);
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static float calculateUsageScore(Long time) {

        float aux = Math.round(time.floatValue());
        Log.d(TAG, "calculateUsageScore: Time Float value: " + aux);

        if (aux == 0.0f)
            return 0.073f;
        if (aux > 0.0f && aux < 4.9f)
            return 0.5f;
        if (aux > 5.0f && aux < 5.9f)
            return 1.0f;
        if (aux > 6.0f && aux < 6.9f)
            return 1.5f;
        if (aux > 7.0f && time < 7.9f)
            return 2.0f;
        if (aux > 8.0f && aux < 8.9f)
            return 2.5f;
        if (aux > 9.0f && aux < 9.9f)
            return 3.0f;
        if (aux > 10f)
            return 4.0f;

        return 0.0f;
    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isConnected;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(nw);
            isConnected = capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
            Log.d(TAG, "isNetworkAvailable: CAPABILITIES, CONNECTED = " + isConnected);
        } else {
            // noinspection deprecation
            NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
            //noinspection deprecation
            isConnected = nwInfo != null && nwInfo.isConnected();
            Log.d(TAG, "isNetworkAvailable: NETWORK INFO, CONNECTED = " + isConnected);
        }
        return isConnected;
    }

    public static void refreshCurrentFragment(Activity activity) {
        NavController navController = Navigation
                .findNavController(activity, R.id.nav_host_fragment);
        int id = Objects.requireNonNull(navController.getCurrentDestination()).getId();
        navController.popBackStack(id, true);
        navController.navigate(id);
    }

    public static void refreshCurrentFragmentWithArgs(Activity activity, Bundle args) {
        NavController navController = Navigation
                .findNavController(activity, R.id.nav_host_fragment);
        int id = Objects.requireNonNull(navController.getCurrentDestination()).getId();
        navController.popBackStack(id, true);
        navController.navigate(id, args);
    }


    public static void saveUsageByWeekDay(Context context) {
        SharedPreferencesHelper spHelper = new SharedPreferencesHelper(context);
        String dayCodeKey;
        String[] dayCodes = context.getResources().getStringArray(R.array.day_codes);
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        long currentTime = System.currentTimeMillis();
        long openTimeUsage = spHelper.getUsageOpenTime();
        long diff = TimeHelper.getDifferenceInMinutes(currentTime, openTimeUsage);
        float usageScore = Util.calculateUsageScore(diff);

        Log.d(TAG, "saveUsageScoreByWeekDay: Memorizing difference: " + diff);
        Log.d(TAG, "saveUsageScoreByWeekDay: UsageScore: " + usageScore);

        switch (day) {
            case Calendar.MONDAY:
                dayCodeKey = dayCodes[0] + 1;
                spHelper.increasesUsageValue(dayCodeKey, usageScore);
                break;
            case Calendar.TUESDAY:
                dayCodeKey = dayCodes[1] + 2;
                spHelper.increasesUsageValue(dayCodeKey, usageScore);
                break;
            case Calendar.WEDNESDAY:
                dayCodeKey = dayCodes[2] + 3;
                spHelper.increasesUsageValue(dayCodeKey, usageScore);
                break;
            case Calendar.THURSDAY:
                dayCodeKey = dayCodes[3] + 4;
                spHelper.increasesUsageValue(dayCodeKey, usageScore);
                break;
            case Calendar.FRIDAY:
                dayCodeKey = dayCodes[4] + 5;
                spHelper.increasesUsageValue(dayCodeKey, usageScore);
                break;
            case Calendar.SATURDAY:
                dayCodeKey = dayCodes[5] + 6;
                spHelper.increasesUsageValue(dayCodeKey, usageScore);
                break;
            case Calendar.SUNDAY:
                dayCodeKey = dayCodes[6] + 7;
                spHelper.increasesUsageValue(dayCodeKey, usageScore);
                break;
        }
    }

    public static void loadSingleRecipeFragmentSelf(Activity activity, String url,
                                                    ArrayList<TagModel> tags) {
        NavController navController = Navigation
                .findNavController(activity, R.id.nav_host_fragment);
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        args.putParcelableArrayList(TAG_KEY, tags);
        navController.navigate(R.id.action_singleRecipeFragment_self, args);
    }

    public static void loadFragment(Activity activity, int action, RecipeModel recipe) {
        if (recipe != null) {
            Bundle args = new Bundle();
            args.putString(URL_KEY, recipe.getUrl());
            ArrayList<TagModel> tagModels = new ArrayList<>(recipe.getTags());
            args.putParcelableArrayList(TAG_KEY, tagModels);

            NavController navController = Navigation
                    .findNavController(activity, R.id.nav_host_fragment);
            navController.navigate(action, args);
        } else {
            Log.d(TAG, "loadFragment: NULL POINT recipe = null");
        }
    }

    public static void loadFragmentByUrl(Activity activity, int action,
                                         String url, ArrayList<TagModel> tagModels) {
        if (!url.isEmpty()) {
            Bundle args = new Bundle();
            args.putString(URL_KEY, url);
            args.putParcelableArrayList(TAG_KEY, tagModels);

            NavController navController = Navigation
                    .findNavController(activity, R.id.nav_host_fragment);
            navController.navigate(action, args);
        } else {
            Log.d(TAG, "loadFragment: NULL POINT recipe = null");
        }
    }

    public static void loadFragmentWhitDelay(Activity activity, int action, RecipeModel recipe) {
        if (recipe != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Bundle args = new Bundle();
                args.putString(URL_KEY, recipe.getUrl());
                ArrayList<TagModel> tagModels = new ArrayList<>(recipe.getTags());
                args.putParcelableArrayList(TAG_KEY, tagModels);

                NavController navController = Navigation
                        .findNavController(activity, R.id.nav_host_fragment);
                navController.navigate(action, args);
            }, TimeHelper.DIALOG_DELAY);
        } else {
            Log.d(TAG, "loadFragment: NULL POINT recipe = null");
        }
    }

    public static void goBack(Activity activity) {
        NavController navController = Navigation
                .findNavController(activity, R.id.nav_host_fragment);
        navController.popBackStack();
    }

    public static void goToBrowser(Activity activity, String stepUrl) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(stepUrl));
        activity.startActivity(i);
    }

    public static void share(Activity activity, String textTosShare) {
        Log.d(TAG, "share: URL = " + textTosShare);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, textTosShare);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent,
                activity.getString(R.string.share_recipe));
        activity.startActivity(shareIntent);
    }
}
