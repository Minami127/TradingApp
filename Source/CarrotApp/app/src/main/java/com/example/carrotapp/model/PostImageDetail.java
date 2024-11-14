package com.example.carrotapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PostImageDetail {

    @SerializedName("items")
    private ArrayList<ImageItem> imageItems;

    public PostImageDetail() {
        this.imageItems = new ArrayList<>();
    }

    public ArrayList<ImageItem> getImageItems() {
        return imageItems;
    }

    public void setImageItems(ArrayList<ImageItem> imageItems) {
        this.imageItems = imageItems;
    }

    public void addImageItem(ImageItem item) {
        this.imageItems.add(item);
    }

    public static class ImageItem {
        private int id;
        private int product_id;
        private String product_image_url;

        // Constructor
        public ImageItem(int id, int product_id, String product_image_url) {
            this.id = id;
            this.product_id = product_id;
            this.product_image_url = product_image_url;
        }

        // Getters
        public int getId() {
            return id;
        }

        public int getProductId() {
            return product_id;
        }

        public String getProductImageUrl() {
            return product_image_url;
        }
    }
}