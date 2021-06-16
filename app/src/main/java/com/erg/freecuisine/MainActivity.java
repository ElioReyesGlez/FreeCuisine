package com.erg.freecuisine;

import android.os.Bundle;

import com.erg.freecuisine.controller.network.helpers.FireBaseHelper;
import com.erg.freecuisine.models.LinkModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_recipes, R.id.navigation_settings)
//                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

//        FireBaseHelper fireBaseHelper = new FireBaseHelper();
//        ArrayList<LinkModel> links = new ArrayList<LinkModel>(){{
//            add(new LinkModel("#carne", "https://www.recetasgratis.net/receta-de-birria-de-res-estilo-sinaloa-75186.html"));
//            add(new LinkModel("#pescado", "https://www.recetasgratis.net/Recetas-de-Pescado-listado_receta-12_1.html"));
//            add(new LinkModel("#marisco", "https://www.recetasgratis.net/Recetas-de-Mariscos-listado_receta-13_1.html"));
//            add(new LinkModel("#pasta", "https://www.recetasgratis.net/Recetas-de-Pasta-listado_receta-5_1.html"));
//            add(new LinkModel("#verdura", "https://www.recetasgratis.net/Recetas-de-Verduras-listado_receta-7_1.html"));
//            add(new LinkModel("#sopa", "https://www.recetasgratis.net/Recetas-de-Sopa-listado_receta-6_1.html"));
//            add(new LinkModel("#pan", "https://www.recetasgratis.net/Recetas-de-Pan-bolleria-listado_receta-16_1.html"));
//            add(new LinkModel("#huevo_lacteo", "https://www.recetasgratis.net/Recetas-de-Huevos-lacteos-listado_receta-18_1.html"));
//            add(new LinkModel("#ceriales", "https://www.recetasgratis.net/Recetas-de-Arroces-cereales-listado_receta-9_1.html"));
//            add(new LinkModel("#ensalada", "https://www.recetasgratis.net/Recetas-de-Ensaladas-listado_receta-4_1.html"));
//            add(new LinkModel("#legumbre", "https://www.recetasgratis.net/Recetas-de-Legumbres-listado_receta-8_1.html"));
//            add(new LinkModel("#postre", "https://www.recetasgratis.net/Recetas-de-Postres-listado_receta-17_1.html"));
//            add(new LinkModel("#salsa", "https://www.recetasgratis.net/Recetas-de-Salsas-guarniciones-listado_receta-14_1.html"));
//            add(new LinkModel("#ave", "https://www.recetasgratis.net/Recetas-de-Aves-caza-listado_receta-11_1.html"));
//
//        }};
//        fireBaseHelper.setLinks(links);
    }
}