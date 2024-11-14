package com.example.carrotapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.carrotapp.api.NetworkClient;
import com.example.carrotapp.api.UserApi;
import com.example.carrotapp.config.Config;
import com.example.carrotapp.model.UserRes;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView btm;
    HomeFragment homeFragment;
    FavoriteListFragment favoriteListFragment;
    ChatFragment chatFragment;
    ProfileFragment profileFragment;
    Toolbar mainToolbar;
    TextView toolbarTitle;
    ImageButton postAddBtn,search;
    int id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btm = findViewById(R.id.bottom_navigation);
        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        homeFragment = new HomeFragment();
        favoriteListFragment = new FavoriteListFragment();
        chatFragment = new ChatFragment();
        profileFragment = new ProfileFragment();
        toolbarTitle = findViewById(R.id.toolbar_title);
        postAddBtn = findViewById(R.id.post_add_btn);
        search = findViewById(R.id.search);

        postAddBtn.setVisibility(View.VISIBLE);


        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", null);
        String userId = sp.getString("userId", null);
        id = Integer.parseInt(userId);
        getUserInfo(id);



        postAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PostAddActivity.class);
                startActivity(intent);
                finish();
            }
        });


        search.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchingActivity.class);
            startActivity(intent);
            finish();
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();

        setSupportActionBar(mainToolbar);
        mainToolbar.setTitle("");

        btm.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                String title = ""; // 툴바 제목 초기화

                if (item.getItemId() == R.id.menu_main) {
                    selectedFragment = homeFragment;
                    title = "ホーム";
                    postAddBtn.setVisibility(View.VISIBLE);
                    search.setVisibility(View.VISIBLE);
                } else {
                    postAddBtn.setVisibility(View.GONE);
                    search.setVisibility(View.GONE);
                    if (item.getItemId() == R.id.favorite_list) {
                        selectedFragment = favoriteListFragment;
                        title = "お気に入り";
                    } else if (item.getItemId() == R.id.menu_chat) {
                        selectedFragment = chatFragment;
                        title = "チャット";
                    } else if (item.getItemId() == R.id.menu_profile) {
                        selectedFragment = profileFragment;
                        title = "プロフィール";
                    }
                }

                // 선택된 프래그먼트가 null이 아닐 경우 변경
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    setToolbarTitle(title); // 툴바 제목 설정
                }
                return true;
            }
        });
    }

    public void setToolbarTitle(String title) {
        toolbarTitle.setText(title);
    }

    private void getUserInfo(int id){
        Retrofit retrofit = NetworkClient.getRetrofitClient(MainActivity.this);

        UserApi api = retrofit.create(UserApi.class);

        Call<UserRes> call = api.userInfo(id);

        call.enqueue(new Callback<UserRes>() {
            @Override
            public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                if(response.isSuccessful()){
                    UserRes userRes = response.body();
                    Log.d("qwqrt", "success: " + response.body());
                    SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("token", userRes.accessToken);
                    editor.putString("userId", userRes.userId);
                    editor.putString("nickname", userRes.nickname);
                    editor.putString("profileImg", userRes.profileImg);
                    editor.putInt("type", 0);
                    editor.apply();

                } else {
                    Log.d("qwqrt", "fail: " + response.code() +"  "+ response.message());
                }
            }
            @Override
            public void onFailure(Call<UserRes> call, Throwable t) {
                Log.d("qwqrt", "fail: ");

            }
        });
    }

}