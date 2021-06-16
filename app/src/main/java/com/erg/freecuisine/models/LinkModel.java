package com.erg.freecuisine.models;

public class LinkModel {
    private String tag;
    private String url;


    public LinkModel() {
    }

    public LinkModel(String tag, String url) {
        this.tag = tag;
        this.url = url;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
