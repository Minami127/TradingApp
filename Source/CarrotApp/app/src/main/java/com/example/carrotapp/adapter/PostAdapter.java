package com.example.carrotapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.carrotapp.PostActivity;
import com.example.carrotapp.R;
import com.example.carrotapp.api.LikeApi;
import com.example.carrotapp.api.NetworkClient;
import com.example.carrotapp.config.Config;
import com.example.carrotapp.model.LikeList;
import com.example.carrotapp.model.Post;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> itemList;
    private Context context;

    public PostAdapter(Context context, List<Post> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ImageView thumbNail;
        public TextView title, price, time, likeCnt,categoty;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.postItem);
            thumbNail = itemView.findViewById(R.id.thumbNail);
            title = itemView.findViewById(R.id.title);
            price = itemView.findViewById(R.id.price);
            time = itemView.findViewById(R.id.time);
            categoty = itemView.findViewById(R.id.category);
            likeCnt = itemView.findViewById(R.id.likeCnt);
        }

        public void bind(Post post) {
            likeCnt.setText(String.valueOf(post.getLikeCnt()));
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = itemList.get(position);
        holder.title.setText(post.getTitle());
        holder.categoty.setText(post.getCategory());

        // likeCnt를 설정
        holder.bind(post);

        // 가격 포맷팅
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);
        String formattedPrice = numberFormat.format(post.getPrice());
        holder.price.setText(formattedPrice + "￥");


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
                .placeholder(R.drawable.baseline_photo_24)
                .error(R.drawable.baseline_photo_24)
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