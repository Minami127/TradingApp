package com.example.carrotapp.model;

import com.google.gson.annotations.SerializedName;

public class PictureRes {

    @SerializedName("product_image_url")
    private final String imgUrl;
    @SerializedName("product_id")
    private final int productId;


    public PictureRes (String imgUrl, int productId){
        this.imgUrl = imgUrl;
        this.productId = productId;
    }

    public String getImgUrl() {
        return imgUrl;
    }
    public int getProductId() {
        return productId;
    }

}

