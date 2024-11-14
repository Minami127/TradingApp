package com.example.carrotapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class LikeList {

    @SerializedName("items")
    private ArrayList<Integer> items;



    @SerializedName("count")
    private int likeCnt;

    public ArrayList<Integer> getItems() {
        return items;
    }

    public int getLikeCnt() {
        return likeCnt;
    }
}
