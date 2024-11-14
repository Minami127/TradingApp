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
import com.example.carrotapp.model.PictureUpload;

import java.util.List;

public class PictureUploadAdapter extends RecyclerView.Adapter<PictureUploadAdapter.ViewHolder> {

    private List<PictureUpload> imageItems;
    private Context context;

    public PictureUploadAdapter(Context context, List<PictureUpload> imageItems) {
        this.context = context;
        this.imageItems = imageItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picture_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PictureUpload item = imageItems.get(position);
        Glide.with(context).load(item.getImgUrl()).into(holder.imageView); // imgUrl로 이미지 로드
    }

    @Override
    public int getItemCount() {
        return imageItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.add_image);
        }
    }
}