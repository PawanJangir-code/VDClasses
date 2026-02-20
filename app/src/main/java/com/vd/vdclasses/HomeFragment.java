package com.vd.vdclasses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private Button btnMarkAttendance;
    private TextView tvWelcome, tvAttendanceStatus;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String today;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnMarkAttendance = view.findViewById(R.id.btnMarkAttendance);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvAttendanceStatus = view.findViewById(R.id.tvAttendanceStatus);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            loadStudentName(user.getEmail());
        }

        checkTodayAttendance();
        btnMarkAttendance.setOnClickListener(v -> markAttendance());
    }

    private void loadStudentName(String email) {
        db.collection("students").document(email).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvWelcome.setText("Welcome, " + name + "!");
                        } else {
                            tvWelcome.setText("Welcome, " + email + "!");
                        }
                    }
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
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(requireContext(), "Attendance marked!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to mark attendance", Toast.LENGTH_SHORT).show();
                });
    }
}
