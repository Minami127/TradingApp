package com.example.carrotapp.model;

import com.google.gson.annotations.SerializedName;

public class Like {



    @SerializedName("product_id")
    private int productId;

    private int id;

    @SerializedName("Is_Valid")
    private int isValid;

    public Like(int productId) {
        this.productId = productId;
    }


    public int getProductId() {
        return productId;
    }
    public int getId() {
        return id;
    }
    public int getIsValid() {
        return isValid;
    }

}
