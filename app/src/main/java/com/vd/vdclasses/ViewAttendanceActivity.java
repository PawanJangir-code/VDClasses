package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViewAttendanceActivity extends AppCompatActivity {

    RecyclerView recyclerAttendance;
    TextView tvAttendanceDate, tvNoAttendance, tvAttendanceCount;
    ShimmerFrameLayout shimmerLayout;
    FirebaseFirestore db;
    List<AttendanceModel> attendanceList;
    AttendanceAdapter adapter;
    Calendar currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        recyclerAttendance = findViewById(R.id.recyclerAttendance);
        tvAttendanceDate = findViewById(R.id.tvAttendanceDate);
        tvNoAttendance = findViewById(R.id.tvNoAttendance);
        tvAttendanceCount = findViewById(R.id.tvAttendanceCount);
        shimmerLayout = findViewById(R.id.shimmerLayout);
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        currentDate = Calendar.getInstance();
        updateDateDisplay();

        attendanceList = new ArrayList<>();
        adapter = new AttendanceAdapter(attendanceList, new HashMap<>());

        recyclerAttendance.setLayoutManager(new LinearLayoutManager(this));
        recyclerAttendance.setAdapter(adapter);

        // Day navigation
        ImageButton btnPrevDay = findViewById(R.id.btnPrevDay);
        ImageButton btnNextDay = findViewById(R.id.btnNextDay);
        btnPrevDay.setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, -1);
            updateDateDisplay();
            loadStudentNamesThenAttendance(getDateString());
        });
        btnNextDay.setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
            updateDateDisplay();
            loadStudentNamesThenAttendance(getDateString());
        });

        loadStudentNamesThenAttendance(getDateString());
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private String getDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate.getTime());
    }

    private void updateDateDisplay() {
        tvAttendanceDate.setText("Date: " + getDateString());
    }

    private void showShimmer() {
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        recyclerAttendance.setVisibility(View.GONE);
        tvNoAttendance.setVisibility(View.GONE);
        tvAttendanceCount.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
    }

    private void loadStudentNamesThenAttendance(String date) {
        showShimmer();
        db.collection("students")
                .get()
                .addOnSuccessListener(studentSnapshots -> {
                    Map<String, String> nameMap = new HashMap<>();
                    for (QueryDocumentSnapshot doc : studentSnapshots) {
                        String email = doc.getString("email");
                        String name = doc.getString("name");
                        if (email != null) {
                            nameMap.put(email, name);
                        }
                    }
                    adapter.setNameMap(nameMap);
                    loadAttendance(date);
                })
                .addOnFailureListener(e -> {
                    loadAttendance(date);
                });
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
                    hideShimmer();

                    if (attendanceList.isEmpty()) {
                        tvNoAttendance.setVisibility(View.VISIBLE);
                        tvNoAttendance.setText("No check-ins for this date.");
                        recyclerAttendance.setVisibility(View.GONE);
                        tvAttendanceCount.setVisibility(View.GONE);
                    } else {
                        tvNoAttendance.setVisibility(View.GONE);
                        recyclerAttendance.setVisibility(View.VISIBLE);
                        tvAttendanceCount.setVisibility(View.VISIBLE);
                        int count = attendanceList.size();
                        tvAttendanceCount.setText(count + " student" + (count != 1 ? "s" : "") + " checked in");
                    }
                })
                .addOnFailureListener(e -> {
                    hideShimmer();
                    Toast.makeText(this, "Failed to load attendance: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
