package com.example.carrotapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PostAdd implements Serializable {

    private int sellerId;
    @SerializedName("category_id") // JSON 키 이름
    private int categoryId; // 여기서는 categoryId를 그대로 사용
    private String title;
    private int price;
    private String description;
    private int productState;
    @SerializedName("id")
    private int postId;


    public PostAdd( int sellerId,int categoryId, String title,int price,String description,int productState) {
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.title = title;
        this.price = price;
        this.productState = productState;
        this.description = description;
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

    public int getPostId() {
        return postId;
    }

}
