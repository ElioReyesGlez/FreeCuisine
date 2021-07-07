package com.erg.freecuisine.models;

public class StepModel {

    private String step = "";
    private String preparation = "";
    private ImageModel image;
    private VideoModel video;
    private String stepLink = "";

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

    public String getStepLink() {
        return stepLink;
    }

    public void setStepLink(String stepLink) {
        this.stepLink = stepLink;
    }

    @Override
    public String toString() {
        return "StepModel{" +
                "step='" + step + '\'' +
                ", preparation='" + preparation + '\'' +
                ", image=" + image +
                ", video=" + video +
                ", stepLink='" + stepLink + '\'' +
                '}';
    }
}
