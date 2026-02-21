package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView btnMarkAttendance;
    private TextView tvAttendanceStatus;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String today;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        tvAttendanceStatus = findViewById(R.id.tvAttendanceStatus);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        checkTodayAttendance();
        btnMarkAttendance.setOnClickListener(v -> {
            RecyclerViewAnimator.animateButtonClick(v);
            v.postDelayed(() -> markAttendance(), 150);
        });

        bottomNav = findViewById(R.id.bottomNav);

        if (savedInstanceState == null) {
            loadFragment(new VideoListFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_videos) {
                fragment = new VideoListFragment();
            } else if (itemId == R.id.nav_attendance) {
                fragment = new AttendanceFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else {
                return false;
            }

            loadFragment(fragment);
            return true;
        });
    }

    private void checkTodayAttendance() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        db.collection("attendance")
                .whereEqualTo("email", user.getEmail())
                .whereEqualTo("date", today)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        setMarkedState();
                    } else {
                        tvAttendanceStatus.setText("Tap to mark your attendance");
                    }
                });
    }

    private void markAttendance() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();
        AttendanceModel record = new AttendanceModel(user.getEmail(), today, timestamp);

        db.collection("attendance").add(record)
                .addOnSuccessListener(documentReference -> {
                    setMarkedState();
                    Toast.makeText(this, "Attendance marked!", Toast.LENGTH_SHORT).show();
                    // Update badge
                    bottomNav.getOrCreateBadge(R.id.nav_attendance).setVisible(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to mark attendance", Toast.LENGTH_SHORT).show();
                });
    }

    private void setMarkedState() {
        btnMarkAttendance.setEnabled(false);
        btnMarkAttendance.setText("Done");
        btnMarkAttendance.setBackgroundResource(R.drawable.bg_mark_done);
        btnMarkAttendance.setTextColor(getResources().getColor(R.color.white, null));
        tvAttendanceStatus.setText("Attendance marked for today");
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_in)
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
