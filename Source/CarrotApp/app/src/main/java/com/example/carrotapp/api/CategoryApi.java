package com.example.carrotapp.api;

import com.example.carrotapp.model.Category;
import com.example.carrotapp.model.CategoryList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface CategoryApi {


    @GET("/category/{id}")
    Call<Category> getCategory(@Path("id") int id, @Header("Authorization") String token);

    @GET("/categorylist")
    Call<CategoryList> getCategoryList(@Header("Authorization") String token);

}
