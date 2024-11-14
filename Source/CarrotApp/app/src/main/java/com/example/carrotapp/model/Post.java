package com.example.carrotapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class Post implements Serializable {
    private int id;
    @SerializedName("seller_id")
    private int sellerId;
    @SerializedName("category_id")
    private int categoryId;
    private String title;
    private int price;
    private String description;
    private int productState;
    private int viewCnt;
    @SerializedName("product_image_url")
    private String productImageUrl;
    private String keyword;
    @SerializedName("product_state")
    private int product_state;
    @SerializedName("name")
    private String category;
    private int likeCnt;
    private int imgId;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;

    public void setLikeCnt(int likeCnt) {
        this.likeCnt = likeCnt;
    }

    // Getters
    public Post(int id, int sellerId, int categoryId, String productImageUrl, String title, int price, String description, int productState, String createdAt, int viewCnt, String updatedAt, int imgId,String category) {
        this.id = id;
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.title = title;
        this.price = price;
        this.productImageUrl = productImageUrl;
        this.productState = productState;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.description = description;
        this.viewCnt = viewCnt;

        this.imgId = imgId;
    }
    public Post(int product_state){
        this.product_state = product_state;
    }
    // Getters
    public int getId() {
        return id;
    }

    public int getSellerId() {
        return sellerId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public int getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public int getProductState() {
        return productState;
    }

    public int getViewCnt() {
        return viewCnt;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public int getLikeCnt() {
        return likeCnt;
    }

    public String getCategory() {
        return category;
    }

    public int getImgId() {
        return imgId;
    }
    public int getProduct_state() {
        return product_state;
    }



}



