package com.example.carrotapp.model;

import java.util.ArrayList;

public class PictureList {

    public ArrayList<PictureUpload> picItems;

    public PictureList() {
        this.picItems = new ArrayList<>();
    }

    public void addPicture(PictureUpload picture) {
        picItems.add(picture);
    }

    public ArrayList<PictureUpload> getPicItems() {
        return picItems;
    }

    public int size() {
        return picItems.size();
    }

    public PictureUpload getPicture(int index) {
        return picItems.get(index);
    }


}
