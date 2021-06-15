package com.erg.freecuisine.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.erg.freecuisine.util.Constants.NUMERAL;
import static com.erg.freecuisine.util.Constants.SEPARATOR_SING;

public class RecipeModel implements Parcelable {
    private String id = "";
    private String title = "";
    private String description = "";
    private int ratings = 0;
    private String time = "";
    private String diners = "";
    private String type = "";
    private List<CommentModel> comments;
    private String ingredients = "";
    private List<StepModel> steps;
    private String extra = "";
    //    private List<ImageModel> images;
    private ImageModel image;
    private List<TagModel> tags;
    private String link;

    public RecipeModel() {
    }

    public RecipeModel(String title, String description, List<TagModel> tags) {
        this.title = title;
        this.description = description;
        this.tags = tags;
    }

    public RecipeModel(String id, String title, String description, int ratings, String time,
                       String diners, String type, List<TagModel> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ratings = ratings;
        this.time = time;
        this.diners = diners;
        this.tags = tags;
        this.type = type;
    }

    public RecipeModel(String id, String title, String description, int ratings, String time,
                       String diners, String type, List<CommentModel> comments, String ingredients,
                       List<StepModel> steps, String extra, ImageModel image,
                       List<TagModel> tags, String link) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ratings = ratings;
        this.time = time;
        this.diners = diners;
        this.type = type;
        this.comments = comments;
        this.ingredients = ingredients;
        this.steps = steps;
        this.extra = extra;
        this.image = image;
        this.tags = tags;
        this.link = link;
    }

    // Parcelling part
    public RecipeModel(Parcel in) {
        String[] data = new String[13];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.id = data[0];
        this.title = data[1];
        this.description = data[2];
        this.ratings = Integer.parseInt(data[3]);
        this.time = data[4];
        this.diners = data[5];
        this.type = data[6];
        this.comments = extractComments(data[7]);
        this.ingredients = data[8];
        this.steps = extractSteps(data[9]);
        this.extra = data[10];
        this.image = new ImageModel(
                String.valueOf(System.currentTimeMillis()), data[11]);
        this.tags = extractTags(data[12]);
    }

    private List<StepModel> extractSteps(String datum) {
        List<StepModel> list = new ArrayList<>();
        String[] steps = datum.split(SEPARATOR_SING);

        for (int i = 0; i < steps.length; i++) {
            StepModel step = new StepModel();
            step.setStep(Integer.parseInt(steps[i]));
            i++;
            if (i < steps.length) {
                step.setPreparation(steps[i]);
                i++;
            }
            if (i < steps.length) {
                step.setImage(new ImageModel(String.valueOf(System.currentTimeMillis()),
                                steps[i]));
                i++;
            }
            list.add(step);
        }
        return list;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        String[] aux = new String[]{
                this.id,
                this.title,
                this.description,
                String.valueOf(this.ratings),
                this.time,
                this.diners,
                this.type,
                getCommentsIntoString(),
                this.ingredients,
                getPreparationStepsIntoString(),
                this.extra,
                this.image.getUrl(),
                getTagsIntoString()
        };
        dest.writeStringArray(aux);
    }

    private String getPreparationStepsIntoString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (StepModel step : steps) {
            stringBuilder.append(step.getStepNumber()).append(SEPARATOR_SING);
            stringBuilder.append(step.getPreparation()).append(SEPARATOR_SING);
            stringBuilder.append(step.getImage().getUrl()).append(SEPARATOR_SING);
        }
        return stringBuilder.toString();
    }

    private String getCommentsIntoString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < comments.size(); i++) {
            CommentModel comment = comments.get(i);
            stringBuilder.append(comment.getComment());
            stringBuilder.append("%");
        }
        return stringBuilder.toString();
    }

    private String getTagsIntoString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < comments.size(); i++) {
            CommentModel comment = comments.get(i);
            stringBuilder.append(comment.getComment());
        }
        return stringBuilder.toString();
    }

    private List<CommentModel> extractComments(String datum) {
        ArrayList<CommentModel> auxList = new ArrayList<>();
        String[] auxArray = datum.split("%");
        for (String comment : auxArray) {
            auxList.add(new CommentModel(comment));
        }
        return auxList;
    }

    private List<TagModel> extractTags(String datum) {
        ArrayList<TagModel> auxList = new ArrayList<>();
        String[] auxArray = datum.split(NUMERAL);
        for (String tag : auxArray) {
            auxList.add(new TagModel(NUMERAL + tag));
        }
        return auxList;
    }

    public static final Creator<RecipeModel> CREATOR = new Creator<RecipeModel>() {
        @Override
        public RecipeModel createFromParcel(Parcel in) {
            return new RecipeModel(in);
        }

        @Override
        public RecipeModel[] newArray(int size) {
            return new RecipeModel[size];
        }
    };

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public String getDiners() {
        return diners;
    }

    public void setDiners(String diners) {
        this.diners = diners;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRatings() {
        return ratings;
    }

    public void setRatings(int ratings) {
        this.ratings = ratings;
    }

    public List<CommentModel> getComments() {
        return comments;
    }

    public void setComments(List<CommentModel> comments) {
        this.comments = comments;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public List<StepModel> getSteps() {
        return steps;
    }

    public void setSteps(List<StepModel> steps) {
        this.steps = steps;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public ImageModel getImage() {
        return image;
    }

    public void setImage(ImageModel image) {
        this.image = image;
    }

    public List<TagModel> getTags() {
        return tags;
    }

    public void setTags(List<TagModel> tags) {
        this.tags = tags;
    }

    public boolean hasTag(String string) {
        for (TagModel tag : tags) {
            if (tag.getText().equals(string)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RecipeModel)) return false;

        RecipeModel recipe = (RecipeModel) obj;

        return getId().equals(recipe.getId());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
