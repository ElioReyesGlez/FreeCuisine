package com.erg.freecuisine.util;

import android.content.Context;

import com.erg.freecuisine.R;

import java.util.ArrayList;
import java.util.Collections;

public class Constants {
    /*UTILS*/
    public static final int DECIMAL_PLACE = 1;

    /*VIBRATION*/
    public static final long VIBRATE_TIME = 12;
    public static final long SPECIAL_VIBRATE_TIME = 70;
    public static final long SPECIAL_MIN_VIBRATE_TIME = 50;
    public static final long MIN_VIBRATE_TIME = 10;
    public static final int FIX_SIZE = 4;
    public static final String NUMERAL = "#";
    public static final String SEPARATOR_SING = "%";

    /*KEYS*/
    public static final String SAVED_STATE_KEY = "saved_state_key";
    public static final String RECIPE_KEY = "recipe_key";
    public static final String LINK_KEY = "link_key";
    public static final String URL_KEY = "url";
    public static final String TAG_KEY = "tag_key";
    public static final String QUERY_KEY = "query_key";
    public static final String SPACE_REGEX = "\\s+";
    public static final String SPACE = " ";

    public static ArrayList<String> getBrands(Context context) {
        ArrayList<String> brands = new ArrayList<>();
        Collections.addAll(brands, context.getResources().getStringArray(R.array.brands));
        return brands;
    }

    public static String preparation = "Corta el salmón en lomos rectangulares. " +
            "En una olla, calienta el aceite de oliva y sella el pescado unos segundos por " +
            "cada lado. Resérvalo para más adelante.";


    /*TAGS FOR WEB SCRAPPING*/
    public static final String MAIN_TAG = "columna-post";
    public static final String TITLE_TAG = "titulo titulo--articulo";
    public static final String RECIPE_INFO_TAG = "recipe-info";
    public static final String CLASS_INTRO_TAG = "intro";
    public static final String IMG_TAG = "img";
    public static final String SRC_TAG = "src";
    public static final String IMAGE_DATA_TAG = "data-imagen";
    public static final String PARAGRAPH_TAG = "p";
    public static final String PROPERTY_TAG_CLASS = "properties";
    public static final String PROPERTY_TAG = "span";
    public static final String PROPERTY_DINERS_TAG = "property comensales";
    public static final String PROPERTY_TIME_TAG = "property duracion";
    public static final String PROPERTY_TYPE_TAG = "property para";
    public static final String PROPERTY_EXTRA_TAG = "properties inline";
    public static final String PROPERTY_INGREDIENTS_TAG = "ingrediente";
    public static final String INGREDIENTS_LABEL_TAG = "label";
    public static final String STEPS_TAG = "apartado";
    public static final String STEPS_ORDER_TAG = "orden";
    public static final String STEPS_IMG_CLASS_TAG = "imagen_wrap";
    public static final String STEPS_IMG_TAG = "imagen lupa";
    public static final String STEPS_VIDEO_TAG = "video";
    public static final String IFRAME_TAG = "iframe";
    public static final String VIDEO_ID_ATTR_TAG = "data-id-youtube";
    public static final String DIV_TAG = "div";

    /*TAGS FOR WEB SCRAPPING LIST BY TAG*/
    public static final String MAIN_CONTENT = "main-content";
    public static final String RESULT_LINK = "resultado link";
    public static final String TITLE_TITLE_RESULT = "titulo titulo--resultado";
    public static final String ATTRIBUTE_HREF = "href";
    public static final String A_TAG = "a";
    public static final String POSITION_IMAGE = "position-imagen";
    public static final String IMAGE_TAG_CLASS = "image";
    public static final String SPAN = "span";

    public static final String NEWLINE = "\n";
    public static final String DIVISION_SING = "%";


    /*REALTIME FIRE DATABASE KEYS */
    public static String FIRE_BASE_REFERENCE = "free-cuisine-default-rtdb";
    public static final String LINKS_FIRE_BASE_REFERENCE = "links";
    public static final String MAIN_URL_FIRE_BASE_REFERENCE = "main_urls";
    public static final String LINK_COLUMN = "link";
    public static final String TAG_COLUMN = "tag";

    public static final String PATTER_FOR_YOUTUBE_ID = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";


    /*HOME TAGS*/
    public static final String HOME_RECOMMENDED_MAIN_TAG = "bloquegroup clear padding-left-1";
    public static final String BLOQUE_LINK = "bloque link";
    public static final String RECETA_GRATIS_COLUMN = "recetasgratis";
    public static final String TITLE_TITLE_BLOCK = "titulo titulo--bloque";
    public static final String CATEGORIA_CLASS = "categoria";
    public static final String ETIQUETA_CLASS = "etiqueta";


    /*SHARED PREFERENCES KEYS*/
    public static final String SHARED_PREF_NAME = "free_cuisine_shared_pref";
    public static final String LAST_USAGE_KEY = "last_usage_key";
    public static final String PREF_APP_FIRST_LAUNCH_KEY = "first_launch_key";
    public static final String OPEN_TIME_USAGE_KEY = "open_time_usage_key";
    public static final String LAST_RECIPE_READ_KEY = "last_recipe_key";
    public static final String VIBRATION_STATUS_FLAG_KEY = "vibration_status_key";
    public static final String SHUFFLE_STATUS_FLAG_KEY = "shuffle_status_key";

    /*VIEWS IDS*/
    public static final int FILTER_ID = 1000431;

    public static final String URL_DEVELOPER = "https://www.linkedin.com/in/elioenai-reyes-gonzález";
    public static final String INSTAGRAM_PACKAGE = "com.instagram.android";
    public static final String LINKEDIN_PACKAGE = "com.linkedin.android";
    public static final String MARKET_APP_DETAILS_URL = "market://details?id=";
    public static final String GOOGLE_APP_DETAILS_URL = "http://play.google.com/store/apps/details?id=";
}
