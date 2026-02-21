package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Account info
        TextView tvAdminName = findViewById(R.id.tvAdminName);
        TextView tvAdminEmail = findViewById(R.id.tvAdminEmail);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String email = user.getEmail();
            tvAdminEmail.setText(email);

            // Try to load admin name from Firestore
            FirebaseFirestore.getInstance().collection("admins").document(email)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            if (name != null && !name.isEmpty()) {
                                tvAdminName.setText(name);
                            } else {
                                // Use part before @ as fallback
                                tvAdminName.setText(email.substring(0, email.indexOf("@")));
                            }
                        } else {
                            tvAdminName.setText(email.substring(0, email.indexOf("@")));
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvAdminName.setText(email.substring(0, email.indexOf("@")));
                    });
        }

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
