package com.vd.vdclasses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentVideoAdapter extends RecyclerView.Adapter<StudentVideoAdapter.ViewHolder> {

    private final List<VideoModel> videoList;
    private OnVideoClickListener clickListener;
    private final RecyclerViewAnimator animator = new RecyclerViewAnimator();

    public interface OnVideoClickListener {
        void onClick(VideoModel video);
    }

    public StudentVideoAdapter(List<VideoModel> videoList, OnVideoClickListener clickListener) {
        this.videoList = videoList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoModel video = videoList.get(position);
        holder.tvVideoTitle.setText(video.getTitle());
        holder.tvVideoSubject.setText(video.getSubject());

        animator.animateItem(holder.itemView, position);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(video);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvVideoTitle, tvVideoSubject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVideoTitle = itemView.findViewById(R.id.tvVideoTitle);
            tvVideoSubject = itemView.findViewById(R.id.tvVideoSubject);
        }
    }
}
