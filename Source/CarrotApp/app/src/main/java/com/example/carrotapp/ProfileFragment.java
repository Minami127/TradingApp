package com.example.carrotapp;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.carrotapp.adapter.MyPostListAdapter;
import com.example.carrotapp.adapter.PostAdapter;
import com.example.carrotapp.api.LikeApi;
import com.example.carrotapp.api.NetworkClient;
import com.example.carrotapp.api.PostApi;
import com.example.carrotapp.api.UserApi;
import com.example.carrotapp.config.Config;
import com.example.carrotapp.model.LikeList;
import com.example.carrotapp.model.Post;
import com.example.carrotapp.model.PostList;
import com.example.carrotapp.model.Res;
import com.example.carrotapp.model.UserRes;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class ProfileFragment extends Fragment {

    private ImageButton profileOption;
    private ShapeableImageView profileImg;
    private GoogleSignInClient mGoogleSignInClient;
    RecyclerView recyclerView;
    ArrayList<Post> postingArrayList = new ArrayList<>();
    MyPostListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    TextView emptyView,nicknameTxt;
    int id = 0;
    int offset = 0;
    int limit = 25;
    int count = 0;

    private Handler handler = new Handler();
    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {

            refreshUserData();
            handler.postDelayed(this, 10000); // 60초마다 갱신
        }
    };

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        recyclerView = view.findViewById(R.id.recyclerview_my);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_my);
        emptyView = view.findViewById(R.id.empty_view2);
        profileImg = view.findViewById(R.id.profile_img);
        profileOption = view.findViewById(R.id.profile_option);
        nicknameTxt = view.findViewById(R.id.profile_nickname);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MyPostListAdapter(getContext(), postingArrayList);
        recyclerView.setAdapter(adapter);
        SharedPreferences sp = getActivity().getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String userId = sp.getString("userId", null);
        id = Integer.parseInt(userId);

        getUserInfo(id);


        getNetworkData();


        profileOption.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setItems(new String[]{"編集", "ログアウト"}, (dialog, which) -> {
                switch (which) {
                    case 0:
                        Intent intent = new Intent(getActivity(), UserProfileUpdateActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        showAlertDialog();
                        break;
                }
            });
            builder.show();
        });
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

    @Override
    public void onStart() {
        super.onStart();
        handler.post(refreshRunnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacks(refreshRunnable);
    }

    private void getNetworkData() {

        Retrofit retrofit = NetworkClient.getRetrofitClient(getActivity());

        SharedPreferences sp = getActivity().getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");

        PostApi api = retrofit.create(PostApi.class);

        Call<PostList> call = api.getMyPostList("Bearer " + token, offset, limit);

        call.enqueue(new Callback<PostList>() {
            @Override
            public void onResponse(Call<PostList> call, Response<PostList> response) {
                swipeRefreshLayout.setRefreshing(false);

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



                        // 어댑터에 데이터 변경 알림
                        adapter.notifyDataSetChanged();
                        if(count > 0){
                            hideEmptyView();
                            getLikesData();
                        } else if(count == 0) {
                            showEmptyView();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<PostList> call, Throwable t) {
                Toast.makeText(getActivity(), "데이터를 불러오는데 실패했습니다. 네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                showEmptyView();
            }
        });
    }

    private void getUserInfo(int id){
        Retrofit retrofit = NetworkClient.getRetrofitClient(getActivity());


        UserApi api = retrofit.create(UserApi.class);

        Call<UserRes> call = api.userInfo(id);

        call.enqueue(new Callback<UserRes>() {
            @Override
            public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                if(response.isSuccessful()){
                    UserRes userRes = response.body();
                    Log.d("qwqrt", "success: " + response.body());
                    SharedPreferences sp = getActivity().getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("token", userRes.accessToken);
                    editor.putString("userId", userRes.userId);
                    editor.putString("nickname", userRes.nickname);
                    editor.putString("profileImg", userRes.profileImg);
                    editor.putInt("type", 0);
                    editor.apply();

                    // 데이터를 다시 가져와서 UI에 반영
                    String profileImgUrl = sp.getString("profileImg", null);
                    String nickname = sp.getString("nickname", null);

                    // UI 업데이트 로직 추가
                    updateUI(profileImgUrl, nickname);


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

    private void refreshData() {
        offset = 0;
        postingArrayList.clear();
        getNetworkData();
    }
    private void refreshUserData() {
        // SharedPreferences에서 사용자 정보를 새로 가져오기
        SharedPreferences sp = getContext().getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String userId = sp.getString("userId", null);
        String nickname = sp.getString("nickname", null);
        String profileImgUrl = sp.getString("profileImg", null);

        if (userId != null) {
            int id = Integer.parseInt(userId);
            Log.d("nick", "nickname: " + nickname);
            Log.d("nick", "img: " + profileImgUrl);

            // 사용자 정보에 따른 네트워크 데이터와 UI 갱신
            getNetworkData();
            getUserInfo(id);  // 사용자 정보를 새로 가져옴
            updateUI(profileImgUrl, nickname);  // UI를 새로 업데이트
        }
    }

    private void getLikesData() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(getActivity());
        SharedPreferences sp = getActivity().getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
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
                    } else {

                    }
                }

                @Override
                public void onFailure(Call<LikeList> call, Throwable t) {
                    Log.e("HomeFragment", "Failed to fetch likes", t);
                }
            });
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setCancelable(false);
        builder.setMessage("本当にログアウトしますか？");

        builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                onDestroyLogout();
            }
        });

        builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // GoogleSignInClient 초기화
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN);
    }

    private void onDestroyLogout() {
        SharedPreferences sp = getContext().getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);


        String token = sp.getString("token", "");
        token = "Bearer " + token;


        Retrofit retrofit = NetworkClient.getRetrofitClient(getContext());
        UserApi api = retrofit.create(UserApi.class);
        Call<Res> call = api.LogOut(token);
        call.enqueue(new Callback<Res>() {
            @Override
            public void onResponse(Call<Res> call, Response<Res> response) {
                if (response.isSuccessful()) {
                    // 쉐어드프리퍼런스의 token을 없애야 한다.
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("token", "");
                    editor.putInt("type", 0);
                    editor.apply();

                    FirebaseAuth.getInstance().signOut();

                    // Google 로그아웃
                    mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // 로그아웃 완료 후 로그인 화면으로 이동
                            Intent intent = new Intent(getContext(), LoginActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    });

                } else {
                    // 실패 처리
                }
            }

            @Override
            public void onFailure(Call<Res> call, Throwable t) {
                Log.i("AAA", "통신 오류");
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

    private void updateUI(String profileImgUrl, String nickname) {
        if (profileImg != null) {
            if (profileImgUrl != null) {
                Glide.with(this)
                        .load(profileImgUrl)
                        .into(profileImg);
            } else {
                Glide.with(this)
                        .load(R.drawable.person_gray)
                        .into(profileImg);
            }
        } else {
            Log.e("ProfileFragment", "Profile image view is null");
        }

        if (nickname != null) {
            nicknameTxt.setText(nickname);
        }
    }

}