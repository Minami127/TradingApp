package com.example.carrotapp.api;

import com.example.carrotapp.model.Like;
import com.example.carrotapp.model.LikeList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LikeApi {

    //찜 하기
    @POST("/IsLike")
    Call<Like> getLike(@Header("Authorization") String token, @Body Like like);

    //찜 삭제
    @DELETE("/DisLike/{product_id}")
    Call<Like> getDisLike(@Header("Authorization") String token, @Path("product_id") int product_id);

    //찜 목록 불러오기
    @GET("/likeCnt/{product_id}")
    Call<LikeList> getLikeList(@Header("Authorization") String token,@Path("product_id") int product_id);

    //찜 상태
    @POST("/like/status")
    Call<Like> getLikeStatus(@Header("Authorization") String token, @Body Like like1);

}
