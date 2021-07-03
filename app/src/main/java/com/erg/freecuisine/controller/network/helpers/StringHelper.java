package com.erg.freecuisine.controller.network.helpers;

import android.util.Log;

import com.erg.freecuisine.models.ImageModel;
import com.erg.freecuisine.models.LinkModel;
import com.erg.freecuisine.models.RealmRecipeModel;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.StepModel;
import com.erg.freecuisine.models.TagModel;
import com.erg.freecuisine.models.VideoModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.erg.freecuisine.util.Constants.DIV_TAG;
import static com.erg.freecuisine.util.Constants.IMG_TAG;
import static com.erg.freecuisine.util.Constants.INGREDIENTS_LABEL_TAG;
import static com.erg.freecuisine.util.Constants.NEWLINE;
import static com.erg.freecuisine.util.Constants.NUMERAL;
import static com.erg.freecuisine.util.Constants.PARAGRAPH_TAG;
import static com.erg.freecuisine.util.Constants.PATTER_FOR_YOUTUBE_ID;
import static com.erg.freecuisine.util.Constants.PROPERTY_INGREDIENTS_TAG;
import static com.erg.freecuisine.util.Constants.PROPERTY_TAG_CLASS;
import static com.erg.freecuisine.util.Constants.SEPARATOR_SING;
import static com.erg.freecuisine.util.Constants.SPACE_REGEX;
import static com.erg.freecuisine.util.Constants.SRC_TAG;
import static com.erg.freecuisine.util.Constants.STEPS_IMG_TAG;
import static com.erg.freecuisine.util.Constants.STEPS_ORDER_TAG;
import static com.erg.freecuisine.util.Constants.STEPS_TAG;
import static com.erg.freecuisine.util.Constants.STEPS_VIDEO_TAG;
import static com.erg.freecuisine.util.Constants.VIDEO_ID_ATTR_TAG;

public class StringHelper {

    private static final String TAG = "StringHelper";

    public static String extractTextFromParagraph(Element rootClass, String tag) {
        StringBuilder stringBuilder = new StringBuilder();
        Elements elements = rootClass.getAllElements();

        for (Element element : elements) {
            Element p = element.getElementsByTag(tag).first();
            stringBuilder.append(p.text()).append(NEWLINE);
        }
        return stringBuilder.toString();
    }

    public static String getRecipeDescription(Element classIntro) {
        StringBuilder stringBuilder = new StringBuilder();
        Elements elements = classIntro.select(PARAGRAPH_TAG);
        for (Element element : elements) {
            stringBuilder.append(element.text()).append(NEWLINE);
        }
        return stringBuilder.toString();
    }

    public static String extractPropertyByClassTag(Element recipeInfo, String tag) {
        if (recipeInfo != null) {
            Element property = recipeInfo.getElementsByClass(tag).first();
            if (property != null)
                return property.text();
            else
                return "";
        }
        return "";
    }

    public static String extractTextByClassTag(Element rootElement, String tag) {
        Element property = rootElement.getElementsByClass(PROPERTY_TAG_CLASS).first();
        if (!tag.isEmpty()) {
            return property.getElementsByClass(tag).text();
        } else {
            return property.text();
        }
    }

    public static String extractIngredients(Element recipeInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        Elements ingredients = recipeInfo.getElementsByClass(PROPERTY_INGREDIENTS_TAG);
        for (Element element : ingredients) {
            if (element != null) {
                Element ingredient = element.select(INGREDIENTS_LABEL_TAG).first();
                if (ingredient != null) {
                    stringBuilder.append(ingredient.text()).append(SEPARATOR_SING);
                }
            }
        }
        return stringBuilder.toString();
    }

    public static List<StepModel> extractPreparationSteps(Element article) {
        List<StepModel> list = new ArrayList<>();
        Elements apartados = article.getElementsByClass(STEPS_TAG);
        for (Element apartado : apartados) {
            Element orderClass = apartado.getElementsByClass(STEPS_ORDER_TAG).first();
            if (orderClass != null && !orderClass.text().isEmpty()) {
                int order = Integer.parseInt(orderClass.text());
                String stepDescription = apartado.select(PARAGRAPH_TAG).first().text();
                Element imgClass = apartado.getElementsByClass(STEPS_IMG_TAG).first();
                if (imgClass != null) {
                    String url = imgClass.select(IMG_TAG).first().absUrl(SRC_TAG);
                    ImageModel image = new ImageModel(url, url);
                    list.add(new StepModel(order, stepDescription, image));
                }
                Element videoClass = apartado.getElementsByClass(STEPS_VIDEO_TAG).first();
                if (videoClass != null) {
                    String id = videoClass.child(0).select(DIV_TAG).first().attr(VIDEO_ID_ATTR_TAG);
                    String url = videoClass.child(0).select(DIV_TAG).first().absUrl(SRC_TAG);
                    VideoModel video = new VideoModel(id, url);
                    list.add(new StepModel(order, stepDescription, video));
                }
            }
        }
        return list;
    }


    public static String extractDiners(String strDiners) {
        return strDiners.split(SPACE_REGEX)[0];
    }

    public static String extractIdFromUrl(String url) {
        Pattern compiledPatter = Pattern.compile(PATTER_FOR_YOUTUBE_ID);
        Matcher matcher = compiledPatter.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        } else
            return "";
    }

    public static String extractTag(String tag) {
        String aux = tag.replaceAll(SPACE_REGEX, "").toLowerCase();
        return NUMERAL + aux;
    }

    public static List<RecipeModel> getRecipesFromStringJson(String recipesJson) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<RecipeModel>>() {}.getType();
        return gson.fromJson(recipesJson, type);
    }

    public static List<RecipeModel> getRecipesFromStringJsonList(
            ArrayList<RealmRecipeModel> realmRecipes) {
        List<RecipeModel> aux = new ArrayList<>();
        for (RealmRecipeModel realmRecipe : realmRecipes) {
            aux.add(getSingleRecipeFromJson(realmRecipe.getJsonRecipe()));
        }
        return aux;
    }

    public static RecipeModel getSingleRecipeFromJson(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<RecipeModel>() {}.getType();
        try {
            return gson.fromJson(json, type);
        } catch (Exception e) {
            Log.e(TAG, "getLastVerseRead: ERROR: " + e.getMessage());
            return new RecipeModel();
        }
    }

    public static List<LinkModel> getLinksFromStringJson(String linksStringJson) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<LinkModel>>() {
        }.getType();
        return gson.fromJson(linksStringJson, type);
    }

    public static ArrayList<TagModel> getSelectedTagsFromStringJson(String tagsStringJson) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<TagModel>>() {
        }.getType();
        return gson.fromJson(tagsStringJson, type);
    }
}
