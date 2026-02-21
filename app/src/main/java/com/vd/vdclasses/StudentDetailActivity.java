package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
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

public class StudentDetailActivity extends AppCompatActivity {

    private TextView tvStudentName, tvStudentEmail, tvStudentPhone, tvStudentClass, tvStudentDob;
    private TextView tvMonth, tvAttendanceSummary, tvAttendancePercent;
    private RecyclerView recyclerCalendar;
    private ShimmerFrameLayout shimmerLayout;
    private ScrollView contentScroll;
    private FirebaseFirestore db;

    private String studentEmail;
    private final Calendar currentMonth = Calendar.getInstance();
    private List<CalendarDay> calendarDays;
    private CalendarDayAdapter calendarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        studentEmail = getIntent().getStringExtra("studentEmail");
        if (studentEmail == null) {
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        // Bind views
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentEmail = findViewById(R.id.tvStudentEmail);
        tvStudentPhone = findViewById(R.id.tvStudentPhone);
        tvStudentClass = findViewById(R.id.tvStudentClass);
        tvStudentDob = findViewById(R.id.tvStudentDob);
        tvMonth = findViewById(R.id.tvMonth);
        tvAttendanceSummary = findViewById(R.id.tvAttendanceSummary);
        tvAttendancePercent = findViewById(R.id.tvAttendancePercent);
        recyclerCalendar = findViewById(R.id.recyclerCalendar);
        shimmerLayout = findViewById(R.id.shimmerLayout);
        contentScroll = findViewById(R.id.contentScroll);

        // Calendar setup
        calendarDays = new ArrayList<>();
        calendarAdapter = new CalendarDayAdapter(calendarDays);
        recyclerCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        recyclerCalendar.setAdapter(calendarAdapter);

        // Month navigation
        ImageButton btnPrevMonth = findViewById(R.id.btnPrevMonth);
        ImageButton btnNextMonth = findViewById(R.id.btnNextMonth);
        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            loadAttendance();
        });
        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            loadAttendance();
        });

        loadProfile();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void showShimmer() {
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        contentScroll.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
        contentScroll.setVisibility(View.VISIBLE);
    }

    private void loadProfile() {
        showShimmer();
        db.collection("students").document(studentEmail)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        StudentModel student = doc.toObject(StudentModel.class);
                        if (student != null) {
                            String name = student.getName();
                            tvStudentName.setText(name != null && !name.isEmpty() ? name : studentEmail);
                            tvStudentEmail.setText(student.getEmail() != null ? student.getEmail() : studentEmail);
                            tvStudentPhone.setText(student.getPhone() != null && !student.getPhone().isEmpty() ? student.getPhone() : "Not provided");
                            tvStudentClass.setText(student.getStudentClass() != null && !student.getStudentClass().isEmpty() ? student.getStudentClass() : "Not set");
                            tvStudentDob.setText(student.getDateOfBirth() != null && !student.getDateOfBirth().isEmpty() ? student.getDateOfBirth() : "Not set");
                        }
                    } else {
                        tvStudentName.setText(studentEmail);
                        tvStudentEmail.setText(studentEmail);
                        tvStudentPhone.setText("Not provided");
                        tvStudentClass.setText("Not set");
                        tvStudentDob.setText("Not set");
                    }
                    hideShimmer();
                    loadAttendance();
                })
                .addOnFailureListener(e -> {
                    hideShimmer();
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAttendance() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonth.setText(monthFormat.format(currentMonth.getTime()));

        // Calculate month date range
        Calendar monthStart = (Calendar) currentMonth.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(monthStart.getTime());

        Calendar monthEnd = (Calendar) currentMonth.clone();
        monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(monthEnd.getTime());

        db.collection("attendance")
                .whereEqualTo("email", studentEmail)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    Map<String, Long> presentDates = new HashMap<>();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        AttendanceModel record = doc.toObject(AttendanceModel.class);
                        String d = record.getDate();
                        if (d != null && d.compareTo(startDate) >= 0 && d.compareTo(endDate) <= 0) {
                            presentDates.put(d, record.getTimestamp());
                        }
                    }
                    buildCalendar(presentDates);
                })
                .addOnFailureListener(e -> {
                    buildCalendar(new HashMap<>());
                    Toast.makeText(this, "Failed to load attendance", Toast.LENGTH_SHORT).show();
                });
    }

    private void buildCalendar(Map<String, Long> presentDates) {
        calendarDays.clear();

        // Headers
        String[] headers = {"S", "M", "T", "W", "T", "F", "S"};
        for (String h : headers) {
            calendarDays.add(new CalendarDay(h));
        }

        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sunday
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Empty cells before 1st
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarDays.add(new CalendarDay());
        }

        Calendar today = Calendar.getInstance();
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        int presentCount = 0;
        int pastDayCount = 0;

        for (int day = 1; day <= daysInMonth; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            String dateStr = dateFormat.format(cal.getTime());

            boolean isToday = dateStr.equals(todayStr);
            boolean isFuture = cal.after(today) && !isToday;
            boolean isPresent = presentDates.containsKey(dateStr);
            long timestamp = isPresent ? presentDates.get(dateStr) : 0;

            if (!isFuture) pastDayCount++;
            if (isPresent) presentCount++;

            calendarDays.add(new CalendarDay(day, dateStr, isPresent, isFuture, isToday, timestamp));
        }

        calendarAdapter.notifyDataSetChanged();

        // Summary
        if (pastDayCount > 0) {
            int percent = Math.round((presentCount * 100f) / pastDayCount);
            tvAttendanceSummary.setText("Present: " + presentCount + " / " + pastDayCount + " days");
            tvAttendancePercent.setText(percent + "%");

            if (percent >= 75) {
                tvAttendancePercent.setTextColor(ContextCompat.getColor(this, R.color.accentGreen));
            } else if (percent >= 50) {
                tvAttendancePercent.setTextColor(ContextCompat.getColor(this, R.color.secondary));
            } else {
                tvAttendancePercent.setTextColor(ContextCompat.getColor(this, R.color.accentRed));
            }
        } else {
            tvAttendanceSummary.setText("No past days yet");
            tvAttendancePercent.setText("");
        }
    }
}
