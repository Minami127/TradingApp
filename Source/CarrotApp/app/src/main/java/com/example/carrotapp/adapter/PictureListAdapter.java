package com.example.carrotapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carrotapp.R;
import com.example.carrotapp.model.PostImageDetail;

import java.util.ArrayList;

public class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.PictureViewHolder> {

    private Context context;
    private ArrayList<PostImageDetail.ImageItem> imageItems;



    public PictureListAdapter(Context context, ArrayList<PostImageDetail.ImageItem> imageItems) {
        this.context = context;
        this.imageItems = imageItems;
    }

    @NonNull
    @Override
    public PictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_post_image, parent, false);
        return new PictureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureViewHolder holder, int position) {
        PostImageDetail.ImageItem imageItem = imageItems.get(position);

        // 이미지 URL을 Glide로 로드
        Glide.with(context)
                .load(imageItem.getProductImageUrl())
                .placeholder(R.drawable.miku) // 로딩 중 표시할 이미지
                .error(R.drawable.miku) // 오류 발생 시 표시할 이미지
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageItems.size();
    }

    public static class PictureViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PictureViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.postDetailImg);
        }
    }
}