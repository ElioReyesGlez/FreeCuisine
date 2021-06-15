package com.erg.freecuisine.models;

public class StepModel {

    private int step;
    private String preparation;
    private ImageModel image;

    public StepModel() { }

    public StepModel(int step, String preparation, ImageModel image) {
        this.step = step;
        this.preparation = preparation;
        this.image = image;
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
