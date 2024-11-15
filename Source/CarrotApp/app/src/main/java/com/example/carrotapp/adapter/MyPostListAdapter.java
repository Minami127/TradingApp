package com.example.carrotapp.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.carrotapp.PostActivity;
import com.example.carrotapp.PostUpdateActivity;
import com.example.carrotapp.R;
import com.example.carrotapp.api.NetworkClient;
import com.example.carrotapp.api.PostApi;
import com.example.carrotapp.config.Config;
import com.example.carrotapp.model.Post;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyPostListAdapter extends RecyclerView.Adapter<MyPostListAdapter.ViewHolder> {
    private List<Post> itemList;
    private Context context;
    private int status;
    private int product_id;

    public MyPostListAdapter(Context context, List<Post> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ImageView thumbNail;
        public TextView title, price, time, likeCnt, categoty;
        public ImageButton imgBtn,option;


        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.my_postItem);
            thumbNail = itemView.findViewById(R.id.my_thumbNail);
            title = itemView.findViewById(R.id.my_title);
            price = itemView.findViewById(R.id.my_price);
            time = itemView.findViewById(R.id.my_time);
            categoty = itemView.findViewById(R.id.my_category);
            likeCnt = itemView.findViewById(R.id.my_likeCnt);
            imgBtn = itemView.findViewById(R.id.delete);
            option = itemView.findViewById(R.id.stauts_update);
        }

        public void bind(Post post) {
            likeCnt.setText(String.valueOf(post.getLikeCnt()));
        }
    }

    private String getRelativeTime(String inputDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime inputTime = LocalDateTime.parse(inputDateTime, formatter);
        ZonedDateTime utcTime = inputTime.atZone(ZoneId.of("UTC"));
        ZonedDateTime tokyoTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));

        Duration duration = Duration.between(tokyoTime, now);
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
        View itemView = LayoutInflater.from(context).inflate(R.layout.my_post_list_item, parent, false);
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

        holder.cardView.setOnClickListener(view -> {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("post", post);
            context.startActivity(intent);
        });

        holder.imgBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("削除確認");
            product_id = post.getId();
            builder.setMessage("この商品を本当に削除しますか？");
            builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    DeletePost(product_id);
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
        });

        holder.option.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setTitle("選んでください");


            builder.setItems(new String[]{"販売終了", "販売中", "予約中"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            product_id = post.getId();
                            status = 0;
                            statusUpdate(product_id,status);
                            break;
                        case 1:
                            product_id = post.getId();
                            status = 1;
                            statusUpdate(product_id,status);
                            break;
                        case 2:
                            product_id = post.getId();
                            status = 2;
                            statusUpdate(product_id,status);
                            break;
                    }
                }
            });

            // 다이얼로그 표시
            builder.show();
        });


    }

    public void DeletePost(int product_id) {

        Retrofit retrofit = NetworkClient.getRetrofitClient(context);
        SharedPreferences sp = context.getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
        String token = sp.getString("token", "");

        PostApi api = retrofit.create(PostApi.class);

        Call<Post> call = api.Delete(product_id,"Bearer " + token);

        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
                    Log.d("DeletePost", "Post 삭제 성공: product_id = " + product_id);
                } else {
                    Log.e("DeletePost", "Post 삭제 실패: " + response.code() + ", 메시지: " + response.message());
                }

            }
            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.e("DeletePost", "Post 삭제 중 네트워크 오류 발생: " + t.getMessage(), t);
            }
        });

    }
    public void statusUpdate(int product_id,int product_state) {

        Retrofit retrofit = NetworkClient.getRetrofitClient(context);
        SharedPreferences sp = context.getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
        String token = sp.getString("token", "");

        PostApi api = retrofit.create(PostApi.class);
        Log.d("abc", "statusUpdate: "+ product_state);
        Log.d("abc", "statusUpdate: "+ product_id);
        Post post = new Post(product_state);

        Call<Post> call = api.getStatus(product_id,"Bearer " + token, post);

        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful()) {
                    Log.d("DeletePost", "success: product_id = " );
                } else {
                    Log.e("DeletePost", "fail: " + response.code() + ", 메시지: " + response.message());
                }

            }
            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.e("DeletePost", "die: " + t.getMessage(), t);
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
