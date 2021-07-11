package com.erg.freecuisine.models;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class StepModel {

    private String step = "";
    private String preparation = "";
    private ImageModel image;
    private VideoModel video;
    private ArrayList<LinkModel> stepLinks;

    public StepModel() { }

    public StepModel(String step, String preparation, ImageModel image) {
        this.step = step;
        this.preparation = preparation;
        this.image = image;
    }

    public StepModel(String step, String preparation, VideoModel video) {
        this.step = step;
        this.preparation = preparation;
        this.video = video;
    }

    public StepModel(String step, String preparation) {
        this.step = step;
        this.preparation = preparation;
    }

    public VideoModel getVideo() {
        return video;
    }

    public void setVideo(VideoModel video) {
        this.video = video;
    }

    public String getStepNumber() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    public ImageModel getImage() {
        return image;
    }

    public void setImage(ImageModel image) {
        this.image = image;
    }

    public String getStep() {
        return step;
    }

    public ArrayList<LinkModel> getStepLinks() {
        return stepLinks;
    }

    public void setStepLinks(ArrayList<LinkModel> stepLinks) {
        this.stepLinks = stepLinks;
    }

    @NotNull
    @Override
    public String toString() {
        return "StepModel{" +
                "step='" + step + '\'' +
                ", preparation='" + preparation + '\'' +
                ", image=" + image +
                ", video=" + video +
                ", stepLinks=" + stepLinks +
                '}';
    }
}
