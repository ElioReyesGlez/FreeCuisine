package com.erg.freecuisine.models;

public class StepModel {

    private int step;
    private String preparation;
    private ImageModel image;
    private VideoModel video;

    public StepModel() { }

    public StepModel(int step, String preparation, ImageModel image) {
        this.step = step;
        this.preparation = preparation;
        this.image = image;
    }

    public StepModel(int step, String preparation, VideoModel video) {
        this.step = step;
        this.preparation = preparation;
        this.video = video;
    }

    public VideoModel getVideo() {
        return video;
    }

    public void setVideo(VideoModel video) {
        this.video = video;
    }

    public int getStepNumber() {
        return step;
    }

    public void setStep(int step) {
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
}
