package com.example.legokp.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "lego_sets")
public class LegoSetEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "set_num")
    private String setNum;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "year")
    private int year;

    @ColumnInfo(name = "theme")
    private String theme;

    @ColumnInfo(name = "num_parts")
    private int numParts;

    @ColumnInfo(name = "set_img_url")
    private String setImgUrl;

    @ColumnInfo(name = "price")
    private double price;

    @ColumnInfo(name = "rating")
    private double rating;

    @ColumnInfo(name = "age_range")
    private String ageRange;

    @ColumnInfo(name = "is_exclusive")
    private boolean isExclusive;

    @ColumnInfo(name = "in_stock")
    private boolean inStock;

    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "last_updated")
    private long lastUpdated;

    public LegoSetEntity(@NonNull String setNum, String name, int year, String theme,
                         int numParts, String setImgUrl, double price, double rating,
                         String ageRange, boolean isExclusive, boolean inStock,
                         boolean isFavorite, String description) {
        this.setNum = setNum;
        this.name = name;
        this.year = year;
        this.theme = theme;
        this.numParts = numParts;
        this.setImgUrl = setImgUrl;
        this.price = price;
        this.rating = rating;
        this.ageRange = ageRange;
        this.isExclusive = isExclusive;
        this.inStock = inStock;
        this.isFavorite = isFavorite;
        this.description = description;
        this.lastUpdated = System.currentTimeMillis();
    }

    @NonNull
    public String getSetNum() { return setNum; }
    public void setSetNum(@NonNull String setNum) { this.setNum = setNum; }

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

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}