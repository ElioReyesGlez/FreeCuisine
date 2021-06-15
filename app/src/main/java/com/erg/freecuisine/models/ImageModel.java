package com.erg.freecuisine.models;

public class ImageModel {
    private String id;
    private String url;

    public ImageModel(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public ImageModel(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
