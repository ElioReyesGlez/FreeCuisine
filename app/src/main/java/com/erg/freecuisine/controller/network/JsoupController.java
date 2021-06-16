package com.erg.freecuisine.controller.network;

import android.util.Log;

import com.erg.freecuisine.R;
import com.erg.freecuisine.controller.network.helpers.StringHelper;
import com.erg.freecuisine.models.ImageModel;
import com.erg.freecuisine.models.LinkModel;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.StepModel;
import com.erg.freecuisine.models.TagModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.erg.freecuisine.util.Constants.ATTRIBUTE_HREF;
import static com.erg.freecuisine.util.Constants.A_TAG;
import static com.erg.freecuisine.util.Constants.CLASS_INTRO_TAG;
import static com.erg.freecuisine.util.Constants.IMAGE_DATA_TAG;
import static com.erg.freecuisine.util.Constants.IMG_TAG;
import static com.erg.freecuisine.util.Constants.MAIN_CONTENT;
import static com.erg.freecuisine.util.Constants.MAIN_TAG;
import static com.erg.freecuisine.util.Constants.POSITION_IMAGE;
import static com.erg.freecuisine.util.Constants.PROPERTY_DINERS_TAG;
import static com.erg.freecuisine.util.Constants.PROPERTY_EXTRA_TAG;
import static com.erg.freecuisine.util.Constants.PROPERTY_TAG_CLASS;
import static com.erg.freecuisine.util.Constants.PROPERTY_TIME_TAG;
import static com.erg.freecuisine.util.Constants.PROPERTY_TYPE_TAG;
import static com.erg.freecuisine.util.Constants.RECIPE_INFO_TAG;
import static com.erg.freecuisine.util.Constants.RESULT_LINK;
import static com.erg.freecuisine.util.Constants.SRC_TAG;
import static com.erg.freecuisine.util.Constants.TITLE_TAG;
import static com.erg.freecuisine.util.Constants.TITLE_TITLE_RESULT;

public class JsoupController {

    private static final String TAG = "JsoupController";

    public static ArrayList<RecipeModel> getRecipesByLink(LinkModel linkModel) {
        ArrayList<RecipeModel> recipes = new ArrayList<>();

        try {

            Document document = Jsoup.connect(linkModel.getUrl()).get();
            Element mainContent = document.getElementsByClass(MAIN_CONTENT).first();
            Elements elementsRecipes = mainContent.getElementsByClass(RESULT_LINK);

            int limit = (elementsRecipes.size() / 2);
            for (int i = 0; i < limit; i++) {

                RecipeModel recipe = new RecipeModel();
                Element elementRecipe = elementsRecipes.get(i);
                String recipeLink = elementRecipe.select(A_TAG).first().attr(ATTRIBUTE_HREF);
                Element imageElement = elementRecipe.getElementsByClass(POSITION_IMAGE).first();
                String imgUrl = imageElement.select(IMG_TAG).first().absUrl(IMAGE_DATA_TAG);
                if (imgUrl == null || imgUrl.isEmpty()) {
                    imgUrl = imageElement.select(IMG_TAG).first().absUrl(SRC_TAG);
                }

                String diners = "";
                String time = "";

                String title = elementRecipe.getElementsByClass(TITLE_TITLE_RESULT).first().text();
                Element properties = elementRecipe.getElementsByClass(PROPERTY_TAG_CLASS).first();
                if (properties != null) {
                    diners = properties.getElementsByClass(PROPERTY_DINERS_TAG).first().text();
                    time = properties.getElementsByClass(PROPERTY_TIME_TAG).first().text();
                }
                String description = elementRecipe.getElementsByClass(CLASS_INTRO_TAG).first().text();
//                description = description.replaceAll()

                if (!recipeLink.isEmpty()) {
                    recipe.setLink(recipeLink);
                    recipe.setId(recipeLink);
                }
                if (!imgUrl.isEmpty())
                    recipe.setImage(new ImageModel(imgUrl));
                if (!title.isEmpty())
                    recipe.setTitle(title);
                if (!diners.isEmpty())
                    recipe.setDiners(diners);
                if (!time.isEmpty())
                    recipe.setTime(time);
                if (!description.isEmpty())
                    recipe.setDescription(description);
                if (!recipeLink.isEmpty()) {
                    TagModel tagModel = new TagModel(linkModel.getTag(), R.color.colorPrimary);
                    List<TagModel> tags = new ArrayList<>();
                    tags.add(tagModel);
                    recipe.setTags(tags);
                }
                if (!recipe.getTitle().isEmpty() && !recipe.getTime().isEmpty()
                        && !recipe.getDiners().isEmpty()) {
                    recipes.add(recipe);
                }
            }

            return recipes;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static RecipeModel getRecipe(String url, List<TagModel> tags) {
        RecipeModel recipe;
        try {

            Document document = Jsoup.connect(url).get();

            Element article = document.getElementsByClass(MAIN_TAG).first();
            Element classIntro = article.getElementsByClass(CLASS_INTRO_TAG).first();
            Element mainImg = classIntro.select(IMG_TAG).first();
            Element recipeInfo = article.getElementsByClass(RECIPE_INFO_TAG).first();

            String id = url;
            String recipeTitle = article.getElementsByClass(TITLE_TAG).first().text();
            String recipeDescription = StringHelper.getRecipeDescription(classIntro);
            int ratings = 5;
            String strDiners = StringHelper.extractPropertyByClassTag(recipeInfo, PROPERTY_DINERS_TAG);
            String diners = StringHelper.extractDiners(strDiners);
            String time = StringHelper.extractPropertyByClassTag(recipeInfo, PROPERTY_TIME_TAG);
            String type = StringHelper.extractPropertyByClassTag(recipeInfo, PROPERTY_TYPE_TAG);
            String extra = StringHelper.extractTextByClassTag(
                    recipeInfo.getElementsByClass(PROPERTY_EXTRA_TAG).first(), "");
            String ingredients = StringHelper.extractIngredients(recipeInfo);
            ImageModel mainImage = new ImageModel(id, mainImg.absUrl(SRC_TAG));
            List<StepModel> steps = StringHelper.extractPreparationSteps(article);

            recipe = new RecipeModel(id, recipeTitle,
                    recipeDescription, ratings, time, diners, type, new ArrayList<>(),
                    ingredients, steps, extra, mainImage, tags, url);

            return recipe;

        } catch (IOException e) {
            Log.e(TAG, "getRecipe: ERROR: " + e.getMessage());
            return null;
        }
    }
}
