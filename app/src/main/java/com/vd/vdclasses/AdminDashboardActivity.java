package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        MaterialCardView cardManageVideos = findViewById(R.id.cardManageVideos);
        MaterialCardView cardViewStudents = findViewById(R.id.cardViewStudents);
        MaterialCardView cardViewAttendance = findViewById(R.id.cardViewAttendance);
        MaterialCardView cardLogout = findViewById(R.id.cardLogout);

        cardManageVideos.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageVideosActivity.class));
        });

        cardViewStudents.setOnClickListener(v -> {
            startActivity(new Intent(this, ViewStudentsActivity.class));
        });

        cardViewAttendance.setOnClickListener(v -> {
            startActivity(new Intent(this, ViewAttendanceActivity.class));
        });

        cardLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
