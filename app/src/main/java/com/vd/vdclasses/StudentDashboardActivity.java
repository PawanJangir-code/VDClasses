package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentDashboardActivity extends AppCompatActivity {

    Button btnMarkAttendance;
    TextView tvWelcome, tvAttendanceStatus;
    FirebaseFirestore db;
    FirebaseAuth auth;
    String today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvAttendanceStatus = findViewById(R.id.tvAttendanceStatus);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            tvWelcome.setText("Welcome, " + user.getEmail() + "!");
        }

        checkTodayAttendance();

        btnMarkAttendance.setOnClickListener(v -> markAttendance());
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
                        btnMarkAttendance.setEnabled(false);
                        btnMarkAttendance.setText("Attendance Marked");
                        tvAttendanceStatus.setText("You have already marked attendance for today.");
                        tvAttendanceStatus.setVisibility(View.VISIBLE);
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
                    btnMarkAttendance.setEnabled(false);
                    btnMarkAttendance.setText("Attendance Marked");
                    tvAttendanceStatus.setText("Attendance marked successfully!");
                    tvAttendanceStatus.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Attendance marked!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to mark attendance", Toast.LENGTH_SHORT).show();
                });
    }
}
