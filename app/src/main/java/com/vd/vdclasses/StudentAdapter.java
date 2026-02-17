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

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private final List<StudentModel> studentList;

    public StudentAdapter(List<StudentModel> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        StudentModel student = studentList.get(position);
        holder.tvStudentEmail.setText(student.getEmail());

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String joinDate = "Joined: " + sdf.format(new Date(student.getCreatedAt()));
        holder.tvStudentJoinDate.setText(joinDate);
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentEmail, tvStudentJoinDate;

        StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentEmail = itemView.findViewById(R.id.tvStudentEmail);
            tvStudentJoinDate = itemView.findViewById(R.id.tvStudentJoinDate);
        }
    }
}
