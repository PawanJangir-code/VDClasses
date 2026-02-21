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
            RecyclerViewAnimator.animateButtonClick(v);
            v.postDelayed(() -> {
                startActivity(new Intent(this, ManageVideosActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }, 150);
        });

        cardViewStudents.setOnClickListener(v -> {
            RecyclerViewAnimator.animateButtonClick(v);
            v.postDelayed(() -> {
                startActivity(new Intent(this, ViewStudentsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }, 150);
        });

        cardViewAttendance.setOnClickListener(v -> {
            RecyclerViewAnimator.animateButtonClick(v);
            v.postDelayed(() -> {
                startActivity(new Intent(this, ViewAttendanceActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }, 150);
        });

        cardLogout.setOnClickListener(v -> {
            RecyclerViewAnimator.animateButtonClick(v);
            v.postDelayed(() -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }, 150);
        });
    }
}
