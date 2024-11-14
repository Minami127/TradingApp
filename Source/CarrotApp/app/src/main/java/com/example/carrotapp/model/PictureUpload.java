package com.example.carrotapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PictureUpload implements Serializable {

    private String imgUrl;

    public PictureUpload (String imgUrl){
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }
}

