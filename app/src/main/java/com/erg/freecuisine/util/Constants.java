package com.erg.freecuisine.util;

import android.content.Context;

import com.erg.freecuisine.R;

import java.util.ArrayList;
import java.util.Collections;

public class Constants {
    /*VIBRATION*/
    public static final long VIBRATE_TIME = 12;
    public static final long SPECIAL_VIBRATE_TIME = 70;
    public static final long SPECIAL_MIN_VIBRATE_TIME = 50;
    public static final long MIN_VIBRATE_TIME = 10;
    public static final int FIX_SIZE = 4;
    public static final String NUMERAL = "#";
    public static final String SEPARATOR_SING = "%";

    /*KEYS*/
    public static final String RECIPE_KEY = "recipe_key";
    public static final String URL_KEY = "url";
    public static final String TAG_KEY = "tag_key";
    public static final String SPACE_REGEX = "\\s+";

    public static ArrayList<String> getBrands(Context context) {
        ArrayList<String> brands = new ArrayList<>();
        Collections.addAll(brands, context.getResources().getStringArray(R.array.brands));
        return brands;
    }

    public static String preparation = "Corta el salmón en lomos rectangulares. " +
            "En una olla, calienta el aceite de oliva y sella el pescado unos segundos por " +
            "cada lado. Resérvalo para más adelante.";


    /*TAGS FOR WEB SCRAPPING*/
    public static String MAIN_TAG = "columna-post";
    public static String TITLE_TAG = "titulo titulo--articulo";
    public static String RECIPE_INFO_TAG = "recipe-info";
    public static String CLASS_INTRO_TAG = "intro";
    public static String IMG_TAG = "img";
    public static String SRC_TAG = "src";
    public static String IMAGE_DATA_TAG = "data-imagen";
    public static String PARAGRAPH_TAG = "p";
    public static String PROPERTY_TAG_CLASS = "properties";
    public static String PROPERTY_TAG = "span";
    public static String PROPERTY_DINERS_TAG = "property comensales";
    public static String PROPERTY_TIME_TAG = "property duracion";
    public static String PROPERTY_TYPE_TAG = "property para";
    public static String PROPERTY_EXTRA_TAG = "properties inline";
    public static String PROPERTY_INGREDIENTS_TAG = "ingrediente";
    public static String INGREDIENTS_LABEL_TAG = "label";
    public static String STEPS_TAG = "apartado";
    public static String STEPS_ORDER_TAG = "orden";
    public static String STEPS_IMG_CLASS_TAG = "imagen_wrap";
    public static String STEPS_IMG_TAG = "imagen lupa";

    /*TAGS FOR WEB SCRAPPING LIST BY TAG*/
    public static String MAIN_CONTENT = "main-content";
    public static String RESULT_LINK = "resultado link";
    public static String TITLE_TITLE_RESULT = "titulo titulo--resultado";
    public static String ATTRIBUTE_HREF = "href";
    public static String A_TAG = "a";
    public static String POSITION_IMAGE = "position-imagen";
    public static String IMAGE_TAG_CLASS = "image";
    public static String SPAN = "span";

    public static String NEWLINE = "\n";
    public static String REGEX_NEW_LINE = "/n";


    /*REALTIME FIRE DATABASE KEYS */
    public static String FIRE_BASE_REFERENCE = "free-cuisine-default-rtdb";
    public static String LINKS_FIRE_BASE_REFERENCE = "links";
    public static final String LINK_COLUMN = "link";
    public static final String TAG_COLUMN = "tag";

    public static String TEST_URL =
            "https://www.recetasgratis.net/receta-de-sushi-maki-de-atun-75179.html";

    public static  String LIST_URL =
            "https://www.recetasgratis.net/Recetas-de-Carne-listado_receta-10_1.html";
    public static final String TEST_NAME = "Receta de Sushi maki de atún";

}
