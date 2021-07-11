package com.erg.freecuisine.helpers;

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
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.erg.freecuisine.util.Constants.ATTRIBUTE_HREF;
import static com.erg.freecuisine.util.Constants.A_TAG;
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
import static com.erg.freecuisine.util.Constants.STEPS_TAG_TITLE;
import static com.erg.freecuisine.util.Constants.STEPS_VIDEO_TAG;
import static com.erg.freecuisine.util.Constants.VIDEO_ID_ATTR_TAG;

public class StringHelper {

    private static final String TAG = "StringHelper";

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
                String order = orderClass.text();
                String stepDescription = apartado.text();
//                String stepDescription = extractTextFromElement(apartado);
                ArrayList<LinkModel> stepLinks = getStepLinks(apartado);
                Element imgClass = apartado.getElementsByClass(STEPS_IMG_TAG).first();
                Element videoClass = apartado.getElementsByClass(STEPS_VIDEO_TAG).first();
                StepModel step;
                if (imgClass != null) {
                    String url = imgClass.select(IMG_TAG).first().absUrl(SRC_TAG);
                    ImageModel image = new ImageModel(url, url);
                    step = new StepModel(order, stepDescription, image);
                    step.setStepLinks(stepLinks);
                    list.add(step);
                } else if (videoClass != null) {
                    String id = videoClass.child(0).select(DIV_TAG).first().attr(VIDEO_ID_ATTR_TAG);
                    String url = videoClass.child(0).select(DIV_TAG).first().absUrl(SRC_TAG);
                    VideoModel video = new VideoModel(id, url);
                    step = new StepModel(order, stepDescription, video);
                    step.setStepLinks(stepLinks);
                    list.add(step);
                } else {
                    step = new StepModel(order, stepDescription);
                    step.setStepLinks(stepLinks);
                    list.add(step);
                }
            }
        }
        return list;
    }

    private static ArrayList<LinkModel> getStepLinks(Element apartado) {
        ArrayList<LinkModel> links = new ArrayList<>();
        Elements pList = apartado.select(PARAGRAPH_TAG);
        if (pList != null && !pList.isEmpty()) {
            for (Element p : pList) {
                Elements aList = p.select(A_TAG);
                if (aList != null && !aList.isEmpty()) {
                    for (Element a : aList) {
                        String tag = a.text();
                        String url = a.attr(ATTRIBUTE_HREF);
                        LinkModel link = new LinkModel(tag, url);
                        links.add(link);
                    }
                }
            }
        }
        return links;
    }

    public static List<StepModel> extractStepsWhitSubTitles(Element article) {
        List<StepModel> list = new ArrayList<>();
        Elements apartados = article.getElementsByClass(STEPS_TAG);
        Elements apartadoTitles = article.getElementsByClass(STEPS_TAG_TITLE);
        for (int i = 0; i < apartados.size() && i < apartadoTitles.size(); i++) {
            Element apartado = apartados.get(i);
            Element apartadoTitle = apartadoTitles.get(i);
            String stepDescription = apartado.text();
//            String stepDescription = extractTextFromElement(apartado);
            ArrayList<LinkModel> stepLinks = getStepLinks(apartado);
            String stepTitle = apartadoTitle.text();
            Element imgClass = apartado.getElementsByClass(STEPS_IMG_TAG).first();
            Element videoClass = apartado.getElementsByClass(STEPS_VIDEO_TAG).first();
            StepModel step;
            if (imgClass != null) {
                String url = imgClass.select(IMG_TAG).first().absUrl(SRC_TAG);
                ImageModel image = new ImageModel(url, url);
                step = new StepModel(stepTitle, stepDescription, image);
                step.setStepLinks(stepLinks);
                list.add(step);
            } else if (videoClass != null) {
                String id = videoClass.child(0).select(DIV_TAG).first().attr(VIDEO_ID_ATTR_TAG);
                String url = videoClass.child(0).select(DIV_TAG).first().absUrl(SRC_TAG);
                VideoModel video = new VideoModel(id, url);
                step = new StepModel(stepTitle, stepDescription, video);
                step.setStepLinks(stepLinks);
                list.add(step);
            } else {
                step = new StepModel(stepTitle, stepDescription);
                step.setStepLinks(stepLinks);
                list.add(step);
            }
        }
        return list;
    }


    public static String extractTextFromElement(Element rootClass) {
        StringBuilder stringBuilder = new StringBuilder();
        Elements elements = rootClass.getAllElements();
        if (elements != null || !elements.isEmpty()) {
            for (Element element : elements) {
                String text = element.text();
                if (text != null || !text.isEmpty())
                    stringBuilder.append(text).append(NEWLINE);
            }
        }
        return stringBuilder.toString();
    }

    public static String extractTextFromElement2(Element rootClass) {
        StringBuilder stringBuilder = new StringBuilder();
        for (TextNode node : rootClass.textNodes()) {
            stringBuilder.append(node).append(NEWLINE);
        }
        return stringBuilder.toString();
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
        Type type = new TypeToken<ArrayList<RecipeModel>>() {
        }.getType();
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
        Type type = new TypeToken<RecipeModel>() {
        }.getType();
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
