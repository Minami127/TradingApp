package com.example.carrotapp.api;

import com.example.carrotapp.model.Res;
import com.example.carrotapp.model.User;
import com.example.carrotapp.model.UserRes;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserApi {
    // 회원가입 API
    @POST("/user/register")
    Call<UserRes> register(@Body User user); //받는 것 return , 보내는 것 : 파라미터

    //로그인 API
    @POST("/user/login")
    Call<UserRes> login (@Body User user);

    //회원탈퇴 API
    @DELETE("/user/secede")
    Call<Res> deleteUser(@Header("Authorization") String token);

    //프로필 정보 불러오기
    @GET("/mypage/userInfo")
    Call<UserRes> getProfile(@Header("Authorization") String token);

    @GET("check")
    Call<UserRes> checkEmail(@Query("email") String email);

    //프로필 정보 변경
    @PUT("/user/nickname/{id}")
    Call<UserRes> EditProfileNickname(@Path("id") int id,@Header("Authorization") String token, @Body User user);

    @Multipart
    @PUT("/user/profileimg/{id}")
    Call<UserRes> EditProfileImg(@Path("id") int id,@Header("Authorization") String token,@Part MultipartBody.Part image);


    @GET("/user/info/{id}")
    Call<UserRes> userInfo(@Path("id") int id);

    //로그아웃
    @DELETE("user/logout")
    Call<Res> LogOut(@Header("Authorization") String token);

}