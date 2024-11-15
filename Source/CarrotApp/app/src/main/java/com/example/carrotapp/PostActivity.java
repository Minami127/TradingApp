package com.example.carrotapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RestrictTo;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.carrotapp.adapter.PictureListAdapter;
import com.example.carrotapp.api.LikeApi;
import com.example.carrotapp.api.NetworkClient;
import com.example.carrotapp.api.PostApi;
import com.example.carrotapp.config.Config;
import com.example.carrotapp.model.ChatRoom;
import com.example.carrotapp.model.Like;
import com.example.carrotapp.model.Post;
import com.example.carrotapp.model.PostDetail;
import com.example.carrotapp.model.PostImageDetail;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PostActivity extends AppCompatActivity {

    ImageButton imgBtn, btn, isLikeBtn;
    TextView postStatus, postTitle, postTitle2, priceTag, description, category, time, viewCnt;
    private ArrayList<PostImageDetail.ImageItem> postImageDetailArrayList = new ArrayList<>();
    PictureListAdapter adapter;
    ViewPager2 viewPager2;

    int id;
    int product_id;
    int isLike = 0;
    int isValid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        imgBtn = findViewById(R.id.img_back_btn_post);
        viewPager2 = findViewById(R.id.product_image);
        postStatus = findViewById(R.id.status);
        postTitle = findViewById(R.id.post_title);
        postTitle2 = findViewById(R.id.title2);
        priceTag = findViewById(R.id.price_tag);
        description = findViewById(R.id.description);
        btn = findViewById(R.id.chat_button);
        isLikeBtn = findViewById(R.id.isLikeBtn);
        category = findViewById(R.id.post_category);
        time = findViewById(R.id.post_time);
        viewCnt = findViewById(R.id.post_viewCnt);

        Post post = (Post) getIntent().getSerializableExtra("post");
        id = post.getId();
        product_id = post.getId();


        Bundle bundle = new Bundle();
        bundle.putSerializable("post_data", post); // post 객체를 Bundle에 담기


        ChatFragment chatFragment = new ChatFragment();
        chatFragment.setArguments(bundle); // Fragment에 bundle 전달
        

        getNetworkLikeList(product_id);
        getNetworkViewCntData(id);

        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostActivity.this, ChatRoomActivity.class);
                intent.putExtra("post", post);

                SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                int postId = post.getId();
                String user = sp.getString("userId", null);
                int userId = Integer.parseInt(user);
                int sellerId = post.getSellerId();
                int buyerId = userId;
                String chatRoomTitle = postId + "_" + sellerId + "_" + userId;  // chatRoomTitle을 고유한 값으로 설정
                intent.putExtra("postId", postId);
                intent.putExtra("sellerId", sellerId);
                intent.putExtra("buyerId", buyerId);

                DatabaseReference chatRoomRef = FirebaseDatabase.getInstance().getReference("chat_rooms");

                // Firebase에서 채팅방 검색
                chatRoomRef.orderByChild("chatRoomTitle").equalTo(chatRoomTitle)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // 이미 채팅방이 존재할 경우 해당 채팅방으로 이동
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        String chatRoomId = snapshot.getKey();  // 기존 채팅방의 ID를 가져오기
                                        intent.putExtra("chatRoomId", chatRoomId);  // chatRoomId 전달
                                        startActivity(intent);
                                        finish();
                                        return;
                                    }
                                } else {
                                    // 채팅방이 없을 경우 새로 생성
                                    ChatRoom chatRoom = new ChatRoom(postId, sellerId, buyerId, chatRoomTitle);

                                    // Firebase에서 새로운 채팅방을 생성
                                    DatabaseReference newChatRoomRef = chatRoomRef.push();
                                    String chatRoomId = newChatRoomRef.getKey();  // Firebase에서 생성된 고유 ID

                                    // chatRoomId를 설정
                                    chatRoom.setChatRoomId(chatRoomId);
                                    newChatRoomRef.setValue(chatRoom)
                                            .addOnSuccessListener(aVoid -> {
                                                intent.putExtra("chatRoomId", chatRoomId);  // 생성된 chatRoomId 전달
                                                startActivity(intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(PostActivity.this, "채팅방 생성 실패", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(PostActivity.this, "데이터베이스 오류", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });



        isLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLike == 0) {
                    isLikeBtn.setImageResource(R.drawable.filled_heart);
                    isLike += 1;

                    Like like = new Like(product_id);
                    Log.d("like", "success" + product_id);

                    Retrofit retrofit = NetworkClient.getRetrofitClient(PostActivity.this);

                    LikeApi api = retrofit.create(LikeApi.class);

                    SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                    String token = sp.getString("token", "");

                    Call<Like> call = api.getLike("Bearer " + token, like);

                    call.enqueue(new Callback<Like>() {
                        @Override
                        public void onResponse(Call<Like> call, Response<Like> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Like like = response.body();
                                SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putInt("saved_post_id", like.getProductId());
                                editor.putInt("saved_is_valid", like.getIsValid());
                                editor.apply();

                                Log.d("like", "success");

                            } else {
                                Log.e("like", "error");
                            }

                        }

                        @Override
                        public void onFailure(Call<Like> call, Throwable t) {
                            Log.e("like", "failure");
                        }
                    });
                } else {
                    isLikeBtn.setImageResource(R.drawable.empty_heart);
                    isLike -= 1;

                    Retrofit retrofit = NetworkClient.getRetrofitClient(PostActivity.this);

                    LikeApi api = retrofit.create(LikeApi.class);

                    SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                    String token = sp.getString("token", "");

                    Call<Like> call = api.getDisLike("Bearer " + token, product_id);

                    call.enqueue(new Callback<Like>() {
                        @Override
                        public void onResponse(Call<Like> call, Response<Like> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Like like = response.body();
                                SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putInt("saved_post_id", like.getProductId());
                                editor.putInt("saved_is_valid", 0);
                                editor.apply();

                                Log.d("Dislike", "성공");
                            } else {
                                Log.e("Dislike", "오류");
                            }
                        }

                        @Override
                        public void onFailure(Call<Like> call, Throwable t) {
                            Log.e("Dislike", "실패");
                        }
                    });

                }

            }
        });
        adapter = new PictureListAdapter(this, postImageDetailArrayList);
        viewPager2.setAdapter(adapter);

        getNetworkData();
        getNetworkDataImg();
    }

    private void getNetworkData() {

        Retrofit retrofit = NetworkClient.getRetrofitClient(PostActivity.this);

        PostApi api = retrofit.create(PostApi.class);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");

        Call<PostDetail> call = api.getPostDetail(id, "Bearer " + token);
        Log.d("asdf", "asdf: " + id);

        call.enqueue(new Callback<PostDetail>() {
            @Override
            public void onResponse(Call<PostDetail> call, Response<PostDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PostDetail postDetail = response.body();

                    // items 배열의 첫 번째 아이템을 가져옴
                    if (postDetail.items != null && !postDetail.items.isEmpty()) {
                        PostDetail.Item firstItem = postDetail.items.get(0);
                        int price = firstItem.getPrice();
                        NumberFormat formatter = NumberFormat.getInstance(Locale.JAPAN);
                        String formattedPrice = formatter.format(price) + " ¥ ";
                        postTitle.setText(firstItem.getTitle());
                        postTitle2.setText(firstItem.getTitle());
                        priceTag.setText(formattedPrice);
                        description.setText(firstItem.getDescription());
                        category.setText(firstItem.getCategory());
                        time.setText(formatTime(firstItem.getCreated_at()));
                        category.setText(firstItem.getCategory());
                        String viewCount = String.valueOf(firstItem.getViewCnt());
                        viewCnt.setText(viewCount);

                        // product_state에 따라 postStatus 텍스트 설정
                        int productState = firstItem.getProduct_state();
                        switch (productState) {
                            case 2:
                                postStatus.setText("予約中");
                                break;
                            case 1:
                                postStatus.setText("販売中");
                                break;
                            case 0:
                                postStatus.setText("販売終了");
                                break;
                        }

                    } else {
                        Log.d("PostActivity", "items가 비어 있습니다.");
                    }
                } else {
                    Log.e("PostActivity", "failure: " + response.code() + " - " + response.message());
                    Toast.makeText(PostActivity.this, "응답 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<PostDetail> call, Throwable t) {
                Log.e("PostActivity", "die " + t.getMessage());
                Toast.makeText(PostActivity.this, "실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getNetworkDataImg() {

        Retrofit retrofit = NetworkClient.getRetrofitClient(PostActivity.this);
        PostApi api = retrofit.create(PostApi.class);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");
        Log.d("getNetworkDataImg", "Token: " + token);

        Log.d("getNetworkDataImg", "Requesting with product_id: " + product_id);

        Call<PostImageDetail> call = api.getPostImageDetail(product_id, "Bearer " + token);

        call.enqueue(new Callback<PostImageDetail>() {
            @Override
            public void onResponse(Call<PostImageDetail> call, Response<PostImageDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PostImageDetail postImageDetail = response.body();
                    Log.d("getNetworkDataImg", "Response Body: " + postImageDetail);
                    ArrayList<PostImageDetail.ImageItem> items = postImageDetail.getImageItems();
                    if (items != null && !items.isEmpty()) {
                        postImageDetailArrayList.clear();
                        postImageDetailArrayList.addAll(items);


                        for (PostImageDetail.ImageItem item : items) {
                            Log.d("getNetworkDataImg", "ID: " + item.getId());
                        }

                        // ID로 내림차순 정렬
                        Collections.sort(postImageDetailArrayList, new Comparator<PostImageDetail.ImageItem>() {
                            @Override
                            public int compare(PostImageDetail.ImageItem item1, PostImageDetail.ImageItem item2) {
                                return Integer.compare(item2.getId(), item1.getId()); // ID 기준 내림차순
                            }
                        });

                        Collections.reverse(postImageDetailArrayList);


                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("getNetworkDataImg", "No image items found in response");
                    }

                } else {
                    Log.e("getNetworkDataImg", "Response failed: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PostImageDetail> call, Throwable t) {
                Log.e("getNetworkDataImg", "API call failed", t);
            }
        });
    }

    private void getNetworkLikeList(int product_id) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(PostActivity.this);
        LikeApi api = retrofit.create(LikeApi.class);
        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");
        Like like1 = new Like(product_id);

        Call<Like> call = api.getLikeStatus("Bearer " + token, like1);

        call.enqueue(new Callback<Like>() {
            @Override
            public void onResponse(Call<Like> call, Response<Like> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Like like1 = response.body();
                    isValid = like1.getIsValid();
                    Log.d("zxcv", "onResponse: " + like1);
                    if (isValid == 1) {
                        isLikeBtn.setImageResource(R.drawable.filled_heart);
                        isLike = 1;
                    } else {
                        isLikeBtn.setImageResource(R.drawable.empty_heart);
                        isLike = 0;
                    }
                } else {
                    isValid = 0;
                    isLike = 0;
                    isLikeBtn.setImageResource(R.drawable.empty_heart);
                }

            }

            @Override
            public void onFailure(Call<Like> call, Throwable t) {
                Log.e("PostActivity", "네트워크 요청 실패", t);
                isValid = 0;
                isLike = 0;
            }
        });
    }

    private void getNetworkViewCntData(int id) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(PostActivity.this);
        PostApi api = retrofit.create(PostApi.class);
        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");

        Call<PostDetail> call = api.getViewCnt(id, "Bearer " + token);

        call.enqueue(new Callback<PostDetail>() {
            @Override
            public void onResponse(Call<PostDetail> call, Response<PostDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("asdf", "success: " + response.body());
                } else {
                    Log.d("asdf", "failure: " + response.body());
                }

            }

            @Override
            public void onFailure(Call<PostDetail> call, Throwable t) {
                Log.e("asdf", "failure check ur server ");
            }
        });

    }

    public String formatTime(String createdAt) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(createdAt);

            SimpleDateFormat outputFormatForConversion = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            outputFormatForConversion.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            String convertedDateStr = outputFormatForConversion.format(date);
            Date convertedDate = outputFormatForConversion.parse(convertedDateStr);

            long diffInMillis = System.currentTimeMillis() - convertedDate.getTime();
            long diffInMinutes = diffInMillis / (60 * 1000);
            long diffInHours = diffInMinutes / 60;


            if (diffInHours < 24) {
                if (diffInHours < 1) {
                    return diffInMinutes + "分前";
                } else {
                    return diffInHours + " 時間前";
                }
            } else {
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy年-MM月-dd日", Locale.getDefault());
                outputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                return outputFormat.format(convertedDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return createdAt;
        }
    }



}