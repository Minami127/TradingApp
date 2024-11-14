package com.example.carrotapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carrotapp.adapter.FavoriteListAdapter;
import com.example.carrotapp.adapter.PostAdapter;
import com.example.carrotapp.api.NetworkClient;
import com.example.carrotapp.api.PostApi;
import com.example.carrotapp.config.Config;
import com.example.carrotapp.model.Post;
import com.example.carrotapp.model.PostList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Header;
import retrofit2.http.Query;


public class FavoriteListFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<Post> postingArrayList = new ArrayList<>();
    FavoriteListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    TextView emptyView;
    int offset = 0;
    int limit = 25;
    int count = 0;


    public FavoriteListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorite_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerview_fav);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_fav);
        emptyView = view.findViewById(R.id.empty_view1);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        adapter = new FavoriteListAdapter(getContext(), postingArrayList);
        recyclerView.setAdapter(adapter);

        getNetworkData();



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
                        getNetworkData();
                    }
                }
            }
        });
        return view;

    }

    private void refreshData() {
        offset = 0;
        postingArrayList.clear();
        getNetworkData();
    }

    private void getNetworkData() {

        Retrofit retrofit = NetworkClient.getRetrofitClient(getActivity());

        SharedPreferences sp = getActivity().getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
        String token = sp.getString("token", "");

        PostApi api = retrofit.create(PostApi.class);
        Log.d("TOKEN_CHECK", "Token: " + token);

        Call<PostList> call = api.getFavoriteList("Bearer " + token, offset, limit);

        call.enqueue(new Callback<PostList>() {
            @Override
            public void onResponse(Call<PostList> call, Response<PostList> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful()) {
                    PostList postingList = response.body();
                    Log.d("API_RESPONSE", "success: " + response.code() + " - " + response.message());

                    if (postingList != null) {
                        postingArrayList.clear(); // 기존 리스트를 비움
                        postingArrayList.addAll(postingList.items); // 새로운 아이템 추가
                        count = postingList.count;
                        Log.d("API_RESPONSE", "success: " + response.code() + " - " + response.message());
                        Log.d("asdf", "success: " + count);
                        // ID로 내림차순 정렬
                        Collections.sort(postingArrayList, new Comparator<Post>() {
                            @Override
                            public int compare(Post post1, Post post2) {
                                return Integer.compare(post2.getId(), post1.getId()); // ID 기준 내림차순
                            }
                        });
                        // 어댑터에 데이터 변경 알림
                        adapter.notifyDataSetChanged();
                        if(count > 0){
                            hideEmptyView();
                        } else if(count == 0) {
                            showEmptyView();
                        }

                    }
                }
                if (!response.isSuccessful()) {
                    Log.e("API_RESPONSE", "Error: " + response.code() + " - " + response.message());
                    showEmptyView();
                }
            }

            @Override
            public void onFailure(Call<PostList> call, Throwable t) {
                Toast.makeText(getActivity(), "데이터를 불러오는데 실패했습니다. 네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                showEmptyView();
            }
        });

    }

    public void showEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    public void hideEmptyView() {
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }
}