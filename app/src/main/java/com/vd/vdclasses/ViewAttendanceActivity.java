package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewAttendanceActivity extends AppCompatActivity {

    RecyclerView recyclerAttendance;
    TextView tvAttendanceDate, tvNoAttendance;
    FirebaseFirestore db;
    List<AttendanceModel> attendanceList;
    AttendanceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        recyclerAttendance = findViewById(R.id.recyclerAttendance);
        tvAttendanceDate = findViewById(R.id.tvAttendanceDate);
        tvNoAttendance = findViewById(R.id.tvNoAttendance);
        db = FirebaseFirestore.getInstance();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvAttendanceDate.setText("Date: " + today);

        attendanceList = new ArrayList<>();
        adapter = new AttendanceAdapter(attendanceList);

        recyclerAttendance.setLayoutManager(new LinearLayoutManager(this));
        recyclerAttendance.setAdapter(adapter);

        loadAttendance(today);
    }

    private void loadAttendance(String date) {
        db.collection("attendance")
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    attendanceList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        AttendanceModel record = doc.toObject(AttendanceModel.class);
                        attendanceList.add(record);
                    }
                    adapter.notifyDataSetChanged();

                    if (attendanceList.isEmpty()) {
                        tvNoAttendance.setVisibility(View.VISIBLE);
                        recyclerAttendance.setVisibility(View.GONE);
                    } else {
                        tvNoAttendance.setVisibility(View.GONE);
                        recyclerAttendance.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load attendance", Toast.LENGTH_SHORT).show();
                });
    }
}
