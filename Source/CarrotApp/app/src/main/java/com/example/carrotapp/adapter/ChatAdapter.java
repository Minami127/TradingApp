package com.example.carrotapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carrotapp.R;
import com.example.carrotapp.model.ChatUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.UserModelViewHolder> {

    private List<ChatUser> chatUser;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ChatAdapter(Context context, List<ChatUser> chatUser) {
        this.context = context;
        this.chatUser = chatUser;
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder {
        TextView nickname, location, time, lastChat;
        ImageView profilePic;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            nickname = itemView.findViewById(R.id.chat_nickname);
            location = itemView.findViewById(R.id.chat_location);
            time = itemView.findViewById(R.id.chat_time);
            lastChat = itemView.findViewById(R.id.last_chat);
            profilePic = itemView.findViewById(R.id.profile_pic);
        }
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_row, parent, false);
        return new UserModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserModelViewHolder holder, int position) {
        ChatUser user = chatUser.get(position);

        holder.nickname.setText(user.getNickname());
        holder.lastChat.setText(user.getLastChat());
        holder.time.setText(user.getTime());

        // 프로필 이미지 로딩
        Glide.with(context)
                .load(user.getProfileImg())
                .placeholder(R.drawable.person_icon)
                .into(holder.profilePic);
    }

    @Override
    public int getItemCount() {
        return chatUser != null ? chatUser.size() : 0;
    }

    // 데이터 가져오기
    public void fetchChatUsers(RecyclerView recyclerView) {
        db.collection("chatUser") // Firestore 컬렉션 이름
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ChatUser> chatUsers = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ChatUser user = document.toObject(ChatUser.class); // ChatUser 클래스에 맞게 변환
                            chatUsers.add(user);
                        }
                        // 어댑터에 데이터 설정
                        chatUser.clear(); // 기존 데이터 클리어
                        chatUser.addAll(chatUsers); // 새로운 데이터 추가
                        notifyDataSetChanged(); // 어댑터에 변경 사항 알리기
                    } else {
                        Log.w("Firebase", "Error getting documents.", task.getException());
                    }
                });
    }
}