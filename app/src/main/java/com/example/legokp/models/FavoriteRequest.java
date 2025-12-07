package com.example.legokp.models;

import com.google.gson.annotations.SerializedName;

public class FavoriteRequest {
    @SerializedName("set_num")
    private String setNum;

    public FavoriteRequest(String setNum) {
        this.setNum = setNum;
    }

    // Getters and Setters
    public String getSetNum() { return setNum; }
    public void setSetNum(String setNum) { this.setNum = setNum; }
}