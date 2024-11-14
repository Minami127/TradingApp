package com.example.carrotapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.carrotapp.PostActivity;
import com.example.carrotapp.PostUpdateActivity;
import com.example.carrotapp.R;
import com.example.carrotapp.model.Post;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class FavoriteListAdapter extends RecyclerView.Adapter<FavoriteListAdapter.ViewHolder> {

    private List<Post> itemList;
    private Context context;

    public FavoriteListAdapter(Context context, List<Post> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ImageView thumbNail;
        public TextView title, price, time, category;
        public ImageButton imgBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.f_postItem);
            thumbNail = itemView.findViewById(R.id.f_thumbNail);
            title = itemView.findViewById(R.id.f_title);
            price = itemView.findViewById(R.id.f_price);
            time = itemView.findViewById(R.id.f_time);
            category = itemView.findViewById(R.id.f_category);
        }



    }



    private String getRelativeTime(String inputDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime inputTime = LocalDateTime.parse(inputDateTime, formatter);
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(inputTime, now);
        long seconds = duration.getSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();
        long months = ChronoUnit.MONTHS.between(inputTime.toLocalDate(), now.toLocalDate());
        long years = ChronoUnit.YEARS.between(inputTime.toLocalDate(), now.toLocalDate());

        if (seconds < 60) {
            return seconds + " 秒前";
        } else if (minutes < 60) {
            return minutes + " 分前";
        } else if (hours < 24) {
            return hours + " 時間前";
        } else if (days < 30) {
            return days + " 日前";
        } else if (months < 12) {
            return months + " ヶ月前";
        } else {
            return years + " 年前";
        }
    }

    @NonNull
    @Override
    public FavoriteListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.favorite_list_row, parent, false);
        return new FavoriteListAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteListAdapter.ViewHolder holder, int position) {
        Post post = itemList.get(position);
        Log.d("asdf", "onBindViewHolder: "+ post.getTitle());
        Log.d("asdf", "onBindViewHolder: "+ post.getCreatedAt());
        Log.d("asdf", "onBindViewHolder: "+ post.getProductImageUrl());
        Log.d("asdf", "onBindViewHolder: "+ post.getPrice());
        holder.title.setText(post.getTitle());



        // 가격 포맷팅
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);
        String formattedPrice = numberFormat.format(post.getPrice());
        holder.price.setText(formattedPrice + "￥");

        holder.category.setText(post.getCategory());

        // 시간 정보 설정
        String createdAt = post.getCreatedAt();
        if (createdAt != null && createdAt.contains("T")) {
            String formattedDate = createdAt.replace("T", " ");
            String relativeTime = getRelativeTime(formattedDate);
            holder.time.setText(relativeTime);
        } else {
            holder.time.setText("시간 정보 없음");
        }

        // Glide로 이미지 로드
        Glide.with(context)
                .load(post.getProductImageUrl())
                .placeholder(R.drawable.cuteboy)
                .error(R.drawable.miku)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(32)))
                .into(holder.thumbNail);

        // 아이템 클릭 시 동작
        holder.cardView.setOnClickListener(view -> {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("post", post); // Post 객체 전달
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

}
