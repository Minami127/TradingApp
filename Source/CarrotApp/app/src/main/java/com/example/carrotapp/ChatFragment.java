package com.example.carrotapp;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carrotapp.adapter.ChatRoomAdapter;
import com.example.carrotapp.adapter.ChatMessageAdapter;
import com.example.carrotapp.api.NetworkClient;
import com.example.carrotapp.api.UserApi;
import com.example.carrotapp.config.Config;
import com.example.carrotapp.model.ChatMessage;
import com.example.carrotapp.model.ChatRoom;
import com.example.carrotapp.model.ChatRoomList;
import com.example.carrotapp.model.UserRes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatRoomAdapter adapter;
    private ArrayList<ChatRoom> chatRoomArrayList = new ArrayList<>();
    private DatabaseReference chatRoomRef;
    private String user;
    private int userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.chat_recyclerView);

        chatRoomRef = FirebaseDatabase.getInstance().getReference("chat_rooms");


        SharedPreferences sp = getActivity().getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
        user = sp.getString("userId", null);
        userId = Integer.parseInt(user);


        adapter = new ChatRoomAdapter(getContext(), chatRoomArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


        loadChatRooms();


        return view;
    }

    private void loadChatRooms() {
        chatRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    chatRoomArrayList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);
                        if (chatRoom != null) {


                            loadLatestMessage(chatRoom);

                            if (userId == chatRoom.getBuyerId()) {
                                getUserData(chatRoom.getSellerId(),chatRoom);

                            } else if (userId == chatRoom.getSellerId()){
                                getUserData(chatRoom.getBuyerId(),chatRoom);

                            }

                        }

                    }
                } else {
                    Log.d("ChatFragment", "chat_rooms 데이터가 없습니다.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatFragment", "데이터를 읽는 중 오류 발생: " + databaseError.getMessage());
            }
        });
    }

    private void loadLatestMessage(ChatRoom chatRoom) {
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("chat_rooms")
                .child(chatRoom.getChatRoomId())
                .child("messages");
        messageRef.orderByChild("timestamp")
                .limitToLast(1)  // 가장 최신 메시지 하나만 가져옵니다.
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                ChatMessage message = snapshot.getValue(ChatMessage.class);
                                if (message != null) {
                                    Log.d("lkj", "ChatRoom ID: " + chatRoom.getChatRoomId());
                                    Log.d("lkj", "Latest Message: " + message.getMessage());
                                    Log.d("lkj", "Sender ID: " + message.getSenderId());
                                    Log.d("lkj", "Timestamp: " + message.getTimestamp());
                                    chatRoom.setLastMessage(message.getMessage());
                                    chatRoom.setLastMessageTime(message.getTimestamp());

                                }
                            }
                        } else {
                            Log.d("lkj", "No messages found for ChatRoom ID: " + chatRoom.getChatRoomId());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("ChatFragment", "Error loading latest message: " + databaseError.getMessage());
                    }
                });
    }


    private void getUserData(int id,ChatRoom chatRoom) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(getActivity());
        UserApi api = retrofit.create(UserApi.class);

        Call<UserRes> call = api.userInfo(id);

        call.enqueue(new Callback<UserRes>() {
            @Override
            public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                if (response.isSuccessful()) {
                    UserRes userRes = response.body();
                    if (userRes != null) {
                        chatRoom.setUserNickname(userRes.nickname);
                        chatRoom.setUserProfilePic(userRes.profileImg);
                        Log.d("seaa", "onResponse: "+ userRes.nickname);
                        chatRoomArrayList.add(chatRoom);
                        adapter.notifyDataSetChanged();

                    }
                } else {
                    Log.d("lks", "fail: " + response.code() +"  "+ response.message());
                }
            }

            @Override
            public void onFailure(Call<UserRes> call, Throwable t) {
                Log.d("getUserData", "fail: " + t.getMessage());
            }
        });
    }

}