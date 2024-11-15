package com.example.carrotapp.api;

import com.example.carrotapp.model.Post;
import com.example.carrotapp.model.PostAdd;
import com.example.carrotapp.model.PostDetail;
import com.example.carrotapp.model.PostImageDetail;
import com.example.carrotapp.model.PostList;
import com.example.carrotapp.model.User;
import com.example.carrotapp.model.UserRes;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PostApi {

    // 게시글 카드뷰 조회 API
    @GET("/post/list")
    Call<PostList> getPost(@Header("Authorization") String token,
                                    @Query("offset") int offset,
                                    @Query("limit") int limit);

    // 게시글 상세조회 API
    @GET("/post/detail/{id}")
    Call<PostDetail> getPostDetail(@Path("id") int id, @Header("Authorization") String token);

    // 게시글 업로드 API
    @POST("/post/add")
    Call<PostAdd> getPostAdd(@Header("Authorization") String token,@Body PostAdd postAdd);

    // 게시글 사진 상세보기 API
    @GET("/post/image/{product_id}")
    Call<PostImageDetail> getPostImageDetail(@Path("product_id") int product_id, @Header("Authorization") String token);

    // 찜 목록 리스트
    @GET("/favorite/list")
    Call<PostList> getFavoriteList(@Header("Authorization") String token,
                                   @Query("offset") int offset,
                                   @Query("limit") int limit);

    // 조회수 증가
    @GET("/viewCnt/{id}")
    Call<PostDetail> getViewCnt(@Path("id") int id, @Header("Authorization") String token);

    @GET("/post/mylist")
    Call<PostList> getMyPostList(@Header("Authorization") String token,
                                   @Query("offset") int offset,
                                   @Query("limit") int limit);

    // 게시글 삭제
    @DELETE("/delete/{product_id}")
    Call<Post> Delete(@Path("product_id") int product_id, @Header("Authorization") String token);


    @GET("/post/search")
    Call<PostList> getSearchList(@Header("Authorization") String token,
                                   @Query("offset") int offset,
                                   @Query("limit") int limit,
                                   @Query("keyword") String keyword);

    @PUT("/post/state/{product_id}")
    Call<Post> getStatus(@Path("product_id") int product_id, @Header("Authorization") String token, @Body Post post);

}
