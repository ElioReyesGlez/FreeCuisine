package com.erg.freecuisine;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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

public class MainActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener {

    private static final String TAG = "MainActivity";

    private SharedPreferencesHelper spHelper;
    private NavController navController;
    private BottomNavigationView navView;
    private Animation exit_anim, enter_anim;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Realm.init(this);
        spHelper = new SharedPreferencesHelper(this);
        exit_anim = AnimationUtils.loadAnimation(this, R.anim.custom_exit_anim_faster);
        enter_anim = AnimationUtils.loadAnimation(this, R.anim.custom_enter_anim_faster);


        navView = findViewById(R.id.nav_view);
//        navView.setOnItemReselectedListener(item -> Log.d(TAG, "onNavigationItemReselected: " + item));
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener(this);
        NavigationUI.setupWithNavController(navView, navController);

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
                    navController.getCurrentDestination())
                    .getId() == R.id.navigation_ad_mob_fragment;
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onDestinationChanged(@NonNull NavController controller,
                                     @NonNull NavDestination destination,
                                     @Nullable Bundle arguments) {
        if (destination.getId() == R.id.navigation_ad_mob_fragment) {
            Util.hideView(exit_anim, navView);
        } else {
            Util.showView(enter_anim, navView);
        }
    }
}