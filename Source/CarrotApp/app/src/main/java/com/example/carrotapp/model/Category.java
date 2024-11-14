package com.example.carrotapp.model;

import com.google.gson.annotations.SerializedName;

public class Category {

    private int id;

    @SerializedName("name")
    private String category;

    public Category(int id, String category) {
        this.id = id;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }
}
