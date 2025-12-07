package com.example.legokp.models;

import com.google.gson.annotations.SerializedName;

public class LegoSet {
    @SerializedName("set_num")
    private String setNum;

    private String name;
    private int year;
    private String theme;

    @SerializedName("num_parts")
    private int numParts;

    @SerializedName("set_img_url")
    private String setImgUrl;

    private double price;
    private double rating;

    @SerializedName("age_range")
    private String ageRange;

    @SerializedName("is_exclusive")
    private boolean isExclusive;

    @SerializedName("in_stock")
    private boolean inStock;

    @SerializedName("is_favorite")
    private boolean isFavorite;

    private String description;

    // Getters and Setters
    public String getSetNum() { return setNum; }
    public void setSetNum(String setNum) { this.setNum = setNum; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public int getNumParts() { return numParts; }
    public void setNumParts(int numParts) { this.numParts = numParts; }

    public String getSetImgUrl() { return setImgUrl; }
    public void setSetImgUrl(String setImgUrl) { this.setImgUrl = setImgUrl; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getAgeRange() { return ageRange; }
    public void setAgeRange(String ageRange) { this.ageRange = ageRange; }

    public boolean isExclusive() { return isExclusive; }
    public void setExclusive(boolean exclusive) { isExclusive = exclusive; }

    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
