package com.example.carrotapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carrotapp.adapter.ChatMessageAdapter;
import com.example.carrotapp.api.NetworkClient;
import com.example.carrotapp.api.PostApi;
import com.example.carrotapp.api.UserApi;
import com.example.carrotapp.config.Config;
import com.example.carrotapp.model.ChatMessage;
import com.example.carrotapp.model.ChatRoom;
import com.example.carrotapp.model.Post;
import com.example.carrotapp.model.PostDetail;
import com.example.carrotapp.model.PostList;
import com.example.carrotapp.model.UserRes;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ChatRoomActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageButton backBtn, sendBtn;
    EditText chatSend;
    TextView partnerNickname;
    DatabaseReference chatRoomRef;
    private String currentChatRoomId;
    private ChatMessageAdapter adapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private int currentUserId, postId, sellerId, buyerId, id;
    private Handler handler;
    private Runnable refreshRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        recyclerView = findViewById(R.id.chat_room_recylerview);
        backBtn = findViewById(R.id.chat_back_btn);
        partnerNickname = findViewById(R.id.chat_user);
        chatSend = findViewById(R.id.chat_send);
        sendBtn = findViewById(R.id.message_send_btn);

        Intent intent = getIntent();
        postId = intent.getIntExtra("postId", -1);
        sellerId = intent.getIntExtra("sellerId", -1);
        buyerId = intent.getIntExtra("buyerId", -1);
        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String userId = sp.getString("userId", null);
        currentUserId = Integer.parseInt(userId);
        currentChatRoomId = intent.getStringExtra("chatRoomId");

        chatRoomRef = FirebaseDatabase.getInstance().getReference("chat_rooms");

        getUserData(sellerId);

        backBtn.setOnClickListener(v -> finish());

        loadChatMessages(currentChatRoomId);


        adapter = new ChatMessageAdapter(this, chatMessages, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setAdapter(adapter);


        sendBtn.setOnClickListener(v -> {
            String messageText = chatSend.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            }
        });
        handler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                chatMessages.clear();
                loadChatMessages(currentChatRoomId);
                handler.postDelayed(this, 3000);
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable);
    }


    private void loadChatMessages(String currentChatRoomId) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chat_rooms")
                .child(currentChatRoomId)
                .child("messages");


        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatMessages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessage message = snapshot.getValue(ChatMessage.class);
                    if (message != null && !chatMessages.contains(message)) {
                        chatMessages.add(message);
                    }
                }
                adapter.notifyDataSetChanged();
                adapter.notifyItemInserted(chatMessages.size() - 1);
//                recyclerView.post(() -> recyclerView.scrollToPosition(chatMessages.size() - 1));
//                adapter.notifyItemInserted(chatMessages.size() - 1);
//                recyclerView.scrollToPosition(chatMessages.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatRoomActivity", "데이터 로딩 실패: " + databaseError.getMessage());
            }
        });


        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // 새로운 메시지가 추가될 때 호출됨
                ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);
                if (newMessage != null) {
                    // 이미 추가된 메시지인지 확인 (중복 방지)
                    boolean isDuplicate = false;
                    for (ChatMessage msg : chatMessages) {
                        if (msg.getTimestamp() == newMessage.getTimestamp()) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (!isDuplicate) {
                        chatMessages.add(newMessage);


                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // 메시지 변경이 발생했을 때 호출됩니다. 필요에 따라 구현하세요.
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // 메시지가 삭제되었을 때 호출됩니다. 필요에 따라 구현하세요.
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // 메시지 이동이 발생했을 때 호출됩니다. 필요에 따라 구현하세요.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatRoomActivity", "채팅 메시지를 불러오는 중 오류 발생: " + databaseError.getMessage());
            }
        });
    }


    private void getUserData(int id) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(ChatRoomActivity.this);
        UserApi api = retrofit.create(UserApi.class);
        Call<UserRes> call = api.userInfo(id);

        call.enqueue(new Callback<UserRes>() {
            @Override
            public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                if (response.isSuccessful()) {
                    UserRes userRes = response.body();
                    updateUI(userRes.nickname);
                }
            }
            @Override
            public void onFailure(Call<UserRes> call, Throwable t) {
                Log.d("getUserData", "fail: " + t.getMessage());
            }
        });
    }

    private void updateUI(String nickname) {
        if (nickname != null) {
            partnerNickname.setText(nickname);
        }
    }

    private void sendMessage(String messageText) {

        ChatMessage message = new ChatMessage();
        message.setMessage(messageText);
        message.setSenderId(currentUserId);
        long timestamp = System.currentTimeMillis();
        message.setTimestamp(timestamp);


        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("chat_rooms")
                .child(currentChatRoomId)
                .child("messages")
                .push();

        messageRef.setValue(message).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatSend.setText("");
                Map<String, Object> lastMessageData = new HashMap<>();
                lastMessageData.put("lastMessageTime", timestamp);
                chatRoomRef.child(currentChatRoomId).updateChildren(lastMessageData);
                adapter.notifyItemInserted(chatMessages.size() - 1);
                recyclerView.post(() -> recyclerView.scrollToPosition(chatMessages.size() - 1));

            } else {
                Log.e("ChatRoomActivity", "메시지 전송 실패");
            }
        });
    }
}