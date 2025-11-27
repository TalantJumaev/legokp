package com.example.legokp.models;

import com.google.gson.annotations.SerializedName;

public class Minifig {
    @SerializedName("set_num")
    private String setNum;

    private String name;

    @SerializedName("num_parts")
    private int numParts;

    @SerializedName("set_img_url")
    private String setImgUrl;

    // Getters and Setters
    public String getSetNum() {
        return setNum;
    }

    public void setSetNum(String setNum) {
        this.setNum = setNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumParts() {
        return numParts;
    }

    public void setNumParts(int numParts) {
        this.numParts = numParts;
    }

    public String getSetImgUrl() {
        return setImgUrl;
    }

    public void setSetImgUrl(String setImgUrl) {
        this.setImgUrl = setImgUrl;
    }
}