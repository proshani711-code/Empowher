package com.example.majorproject;

public class Category {
    public String categoryName;
    private int image;

    public Category(String categoryName, int image) {
        this.categoryName = categoryName;
        this.image = image;
    }

    public int getImage() {
        return image;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
