package com.erg.freecuisine.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.yalantis.filter.model.FilterModel;


public class TagModel implements FilterModel, Parcelable {

    private String text;
    private int color;

    public TagModel(String text, int color) {
        this.text = text;
        this.color = color;
    }

    public TagModel(String text) {
        this.text = text;
    }

    public static final Creator<TagModel> CREATOR = new Creator<TagModel>() {
        @Override
        public TagModel createFromParcel(Parcel in) {
            return new TagModel(in);
        }

        @Override
        public TagModel[] newArray(int size) {
            return new TagModel[size];
        }
    };

    @NonNull
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagModel)) return false;

        TagModel tag = (TagModel) o;
        return getText().contains(tag.getText());
    }

    @Override
    public int hashCode() {
        int result = getText().hashCode();
        result = 31 * result + getColor();
        return result;
    }

    //Parcelling
    public TagModel (Parcel in){
        String[] data = new String[2];
        in.readStringArray(data);

        text = data[0];
        color = Integer.parseInt(data[1]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        String[] aux = new String[] {
                text,
                String.valueOf(color)
        };
        dest.writeStringArray(aux);
    }
}
