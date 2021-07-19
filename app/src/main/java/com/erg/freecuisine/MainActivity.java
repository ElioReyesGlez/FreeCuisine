package com.erg.freecuisine;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.erg.freecuisine.helpers.MessageHelper;
import com.erg.freecuisine.helpers.SharedPreferencesHelper;
import com.erg.freecuisine.util.Util;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.util.Objects;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SharedPreferencesHelper spHelper;
    private NavController navController;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Realm.init(this);
        spHelper = new SharedPreferencesHelper(this);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnItemReselectedListener(item -> Log.d(TAG, "onNavigationItemReselected: " + item));

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            switch (destination.getId()){
                case R.id.navigation_home:
                case R.id.navigation_recipes:
                case R.id.navigation_settings:
                    Util.showBottomBar(MainActivity.this, navView);
                    break;
                default:
                    Util.hideBottomBar(MainActivity.this, navView);
                    break;
            }

        });

        initMobileAds();
    }

    private void initMobileAds() {
        MobileAds.initialize(this, initializationStatus ->
                Log.d(TAG, "onInitializationComplete: STATUS: "
                        + initializationStatus.toString()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (spHelper.isFirstLunch()) {
            spHelper.setVibrationStatus(true); //Setting vibration on by default
            spHelper.setScrollUpStatus(true); //Setting scrolling up on by default
        }
    }

    @Override
    public void onBackPressed() {
        boolean isAdFragmentVisible = false;
        if (navController != null) {
            isAdFragmentVisible = Objects.requireNonNull(
                    navController.getCurrentDestination()).getId() == R.id.adMobFragment;
        }
        if (isAdFragmentVisible) {
            MessageHelper.showSuccessMessageOnMain(
                    this, getString(R.string.function_temporarily_disabled));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        spHelper.saveLastUsage(System.currentTimeMillis());
        if (spHelper.isFirstLunch())
            spHelper.setFistLunchStatus(false);
    }
}