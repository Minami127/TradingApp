package com.example.carrotapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrotapp.R;
import com.example.carrotapp.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder> {

    private List<ChatMessage> chatMessages;
    private int currentUserId;
    private Context context;


    public ChatMessageAdapter(Context context, List<ChatMessage> chatMessages, int currentUserId) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.currentUserId = currentUserId;
    }

    // ViewHolder 정의
    public static class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftChatLayout;
        LinearLayout rightChatLayout;
        TextView leftChatTextView;
        TextView rightChatTextView;
        TextView leftTime;
        TextView rightTime;

        public ChatMessageViewHolder(View itemView) {
            super(itemView);
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextView = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextView = itemView.findViewById(R.id.right_chat_textview);
            leftTime = itemView.findViewById(R.id.left_time);
            rightTime = itemView.findViewById(R.id.right_time);
        }
    }

    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_my_low, parent, false);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatMessageViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);


        Log.d("CMA", "Position: " + chatMessages.size());
        Log.d("CMA", "SenderId: " + chatMessage.getSenderId());
        Log.d("CMA", "Message: " + chatMessage.getMessage());
        Log.d("CMA", "Timestamp: " + chatMessage.getTimestamp());

        if (chatMessage.getSenderId() == currentUserId) {
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatTextView.setText(chatMessage.getMessage());
            holder.rightTime.setText(formatTimestamp(chatMessage.getTimestamp()));
        } else {
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatTextView.setText(chatMessage.getMessage());
            holder.leftTime.setText(formatTimestamp(chatMessage.getTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    private String formatTimestamp(long timestamp) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
        calendar.setTimeInMillis(timestamp);
        Date dateInTokyo = calendar.getTime();

        Calendar nowCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
        long currentTime = nowCalendar.getTimeInMillis();


        long diffInMillis = currentTime - dateInTokyo.getTime();
        long diffInHours = diffInMillis / (60 * 60 * 1000);

        if (diffInHours < 24) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.JAPAN);
            timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            return timeFormat.format(dateInTokyo);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN);
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            return dateFormat.format(dateInTokyo);
        }
    }
}