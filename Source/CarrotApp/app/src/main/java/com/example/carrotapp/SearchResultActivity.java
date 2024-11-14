package com.example.carrotapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.carrotapp.adapter.SearchListAdapter;
import com.example.carrotapp.api.LikeApi;
import com.example.carrotapp.api.NetworkClient;
import com.example.carrotapp.api.PostApi;
import com.example.carrotapp.config.Config;
import com.example.carrotapp.model.LikeList;
import com.example.carrotapp.model.Post;
import com.example.carrotapp.model.PostList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SearchResultActivity extends AppCompatActivity {


    ImageView backBtn,searchBtn;
    private SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    ArrayList<Post> postingArrayList = new ArrayList<>();
    SearchListAdapter adapter;
    int offset = 0;
    int limit = 25;
    int count = 0;
    int id;
    String keyword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backBtn = findViewById(R.id.back_search_list);
        searchBtn = findViewById(R.id.search2);
        recyclerView = findViewById(R.id.recyclerview_search);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_search);

        recyclerView.setLayoutManager(new LinearLayoutManager(SearchResultActivity.this));

        adapter = new SearchListAdapter(SearchResultActivity.this,postingArrayList);
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        keyword = intent.getStringExtra("searchText");

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
        String token = sp.getString("token", null);
        String userId = sp.getString("userId", null);
        id = Integer.parseInt(userId);

        Log.d("API_RESPONSE", "keyword: " + keyword);
        Log.d("API_RESPONSE", ": " + token);

        getSearchData(keyword);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == postingArrayList.size() - 1) {
                    if (postingArrayList.size() < count) {
                        limit += limit;
                        getSearchData(keyword);
                    }
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchResultActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchResultActivity.this, SearchingActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    private void getSearchData(String keyword){

        Retrofit retrofit = NetworkClient.getRetrofitClient(SearchResultActivity.this);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
        String token = sp.getString("token", "");

        PostApi api = retrofit.create(PostApi.class);

        Call<PostList> call = api.getSearchList("Bearer " + token, offset, limit, keyword);
        Log.d("API_RESPONSE", "hhh: " + keyword);

        call.enqueue(new Callback<PostList>() {
            @Override
            public void onResponse(Call<PostList> call, Response<PostList> response) {
                swipeRefreshLayout.setRefreshing(false);
                Log.d("API_RESPONSE", "Status code: " + response.code());
                Log.d("API_RESPONSE", "Response message: " + response.message());
                Log.d("API_RESPONSE", "Response message: " + response.body());
                if (response.isSuccessful()) {
                    PostList postingList = response.body();

                    if (postingList != null) {
                        postingArrayList.clear(); // 기존 리스트를 비움
                        postingArrayList.addAll(postingList.items); // 새로운 아이템 추가
                        count = postingList.count;

                        // ID로 내림차순 정렬
                        Collections.sort(postingArrayList, new Comparator<Post>() {
                            @Override
                            public int compare(Post post1, Post post2) {
                                return Integer.compare(post2.getId(), post1.getId()); // ID 기준 내림차순
                            }
                        });

                        // 좋아요 데이터 요청
                        getLikesData(); // 좋아요 개수 요청

                        // 어댑터에 데이터 변경 알림
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<PostList> call, Throwable t) {
                Log.e("API_FAILURE", "Request failed: " + t.getMessage(), t);
                Toast.makeText(SearchResultActivity.this, "데이터를 불러오는데 실패했습니다. 네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getLikesData() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(SearchResultActivity.this);
        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
        String token = sp.getString("token", "");

        LikeApi likeApi = retrofit.create(LikeApi.class);


        for (int i = 0; i < postingArrayList.size(); i++) {
            Post post = postingArrayList.get(i);
            Call<LikeList> call = likeApi.getLikeList("Bearer " + token, post.getId());
            Log.d("qwer", "getLikesData: " + post.getId());
            call.enqueue(new Callback<LikeList>() {
                @Override
                public void onResponse(Call<LikeList> call, Response<LikeList> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LikeList likeList = response.body();
                        int likeCnt = likeList.getLikeCnt();

                        post.setLikeCnt(likeCnt);
                        adapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onFailure(Call<LikeList> call, Throwable t) {
                    Log.e("search", "Failed to fetch likes", t);
                }
            });
        }
    }

    private void refreshData() {
        offset = 0;
        postingArrayList.clear();
        getSearchData(keyword);
    }

}