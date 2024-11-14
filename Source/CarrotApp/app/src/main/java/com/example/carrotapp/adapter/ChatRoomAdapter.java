package com.example.carrotapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carrotapp.ChatRoomActivity;
import com.example.carrotapp.R;
import com.example.carrotapp.model.ChatRoom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ViewHolder> {

    private Context context;
    private List<ChatRoom> chatRoomList;

    public ChatRoomAdapter(Context context, List<ChatRoom> chatRoomList) {
        this.context = context;
        this.chatRoomList = chatRoomList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView chatNickname;
        TextView lastMessage;
        TextView lastMessageTime;
        CardView cardView;
        ImageView profilePic;

        public ViewHolder(View itemView) {
            super(itemView);
            chatNickname = itemView.findViewById(R.id.chat_nickname);
            lastMessage = itemView.findViewById(R.id.last_chat);
            lastMessageTime = itemView.findViewById(R.id.chat_time);
            cardView = itemView.findViewById(R.id.chatItem);
            profilePic = itemView.findViewById(R.id.profile_pic);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatRoom chatRoom = chatRoomList.get(position);

        Log.d("seafa", "onBindViewHolder: "+chatRoom.getUserNickname());
        holder.chatNickname.setText(chatRoom.getUserNickname());
        holder.lastMessage.setText(chatRoom.getLastMessage());
        holder.lastMessageTime.setText(formatTimestamp(chatRoom.getLastMessageTime()));


        Glide.with(context)
                .load(chatRoom.getUserProfilePic())
                .error(R.drawable.person_gray)
                .into(holder.profilePic);


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatRoomActivity.class);
                intent.putExtra("postId", chatRoom.getPostId());
                intent.putExtra("sellerId", chatRoom.getSellerId());
                intent.putExtra("buyerId", chatRoom.getBuyerId());
                intent.putExtra("chatRoomId", chatRoom.getChatRoomId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }


}