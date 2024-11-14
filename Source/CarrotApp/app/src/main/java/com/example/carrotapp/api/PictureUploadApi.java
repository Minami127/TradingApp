package com.example.carrotapp.api;

import com.example.carrotapp.model.PictureUpload;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface PictureUploadApi {

    @Multipart
    @POST("/uploadimg")
    Call<PictureUpload> getPost(@Header("Authorization") String token,
                                @Part MultipartBody.Part image,
                                @Part("product_id") int productId);

}
