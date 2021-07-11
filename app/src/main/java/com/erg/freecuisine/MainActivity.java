package com.erg.freecuisine;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.erg.freecuisine.helpers.SharedPreferencesHelper;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemReselectedListener {

    private static final String TAG = "MainActivity";


    SharedPreferencesHelper spHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Realm.init(this);
        spHelper = new SharedPreferencesHelper(this);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemReselectedListener(this);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        initMobileAds();
    }

    private void initMobileAds() {
        MobileAds.initialize(this, initializationStatus ->
                Log.d(TAG, "onInitializationComplete: STATUS: "
                        + initializationStatus.toString()));
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {
        Log.d(TAG, "onNavigationItemReselected: " + item);
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
    protected void onDestroy() {
        super.onDestroy();
        spHelper.saveLastUsage(System.currentTimeMillis());
        if (spHelper.isFirstLunch())
            spHelper.setFistLunchStatus(false);
    }
}