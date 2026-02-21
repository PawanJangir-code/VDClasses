package com.vd.vdclasses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private final List<AttendanceModel> attendanceList;
    private Map<String, String> nameMap;
    private final RecyclerViewAnimator animator = new RecyclerViewAnimator();

    public AttendanceAdapter(List<AttendanceModel> attendanceList, Map<String, String> nameMap) {
        this.attendanceList = attendanceList;
        this.nameMap = nameMap;
    }

    public void setNameMap(Map<String, String> nameMap) {
        this.nameMap = nameMap;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceModel attendance = attendanceList.get(position);

        String email = attendance.getEmail();
        String name = nameMap != null ? nameMap.get(email) : null;
        holder.tvAttendanceEmail.setText(name != null && !name.isEmpty() ? name : email);

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String time = "Checked in at " + sdf.format(new Date(attendance.getTimestamp()));
        holder.tvAttendanceTime.setText(time);

        animator.animateItem(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvAttendanceEmail, tvAttendanceTime;

        AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAttendanceEmail = itemView.findViewById(R.id.tvAttendanceEmail);
            tvAttendanceTime = itemView.findViewById(R.id.tvAttendanceTime);
        }
    }
}
