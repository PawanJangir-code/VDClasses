package com.vd.vdclasses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private final List<VideoModel> videoList;
    private final List<String> documentIds;
    private final OnVideoDeleteListener deleteListener;
    private final OnVideoEditListener editListener;
    private final RecyclerViewAnimator animator = new RecyclerViewAnimator();

    public interface OnVideoDeleteListener {
        void onDelete(String documentId, int position);
    }

    public interface OnVideoEditListener {
        void onEdit(String documentId, VideoModel video);
    }

    public interface OnVideoClickListener {
        void onClick(VideoModel video);
    }

    private OnVideoClickListener clickListener;

    public VideoAdapter(List<VideoModel> videoList, List<String> documentIds,
                        OnVideoDeleteListener deleteListener, OnVideoEditListener editListener) {
        this.videoList = videoList;
        this.documentIds = documentIds;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    public void setOnVideoClickListener(OnVideoClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoModel video = videoList.get(position);
        holder.tvVideoTitle.setText(video.getTitle());
        holder.tvVideoSubject.setText(video.getSubject());

        animator.animateItem(holder.itemView, position);

        holder.btnDeleteVideo.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                deleteListener.onDelete(documentIds.get(pos), pos);
            }
        });

        holder.btnEditVideo.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                editListener.onEdit(documentIds.get(pos), videoList.get(pos));
            }
        });

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                clickListener.onClick(videoList.get(pos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView tvVideoTitle, tvVideoSubject;
        ImageButton btnDeleteVideo, btnEditVideo;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVideoTitle = itemView.findViewById(R.id.tvVideoTitle);
            tvVideoSubject = itemView.findViewById(R.id.tvVideoSubject);
            btnDeleteVideo = itemView.findViewById(R.id.btnDeleteVideo);
            btnEditVideo = itemView.findViewById(R.id.btnEditVideo);
        }
    }
}
