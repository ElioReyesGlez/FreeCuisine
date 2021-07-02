package com.erg.freecuisine;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.erg.freecuisine.controller.network.helpers.FireBaseHelper;
import com.erg.freecuisine.models.LinkModel;
import com.erg.freecuisine.ui.RecipesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemReselectedListener {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Realm.init(this);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemReselectedListener(this);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {
        Log.d(TAG, "onNavigationItemReselected: " + item);
    }
}