package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            autoRedirect(user.getEmail());
            return;
        }

        setContentView(R.layout.activity_welcome);
        setupButtons();
        playEntranceAnimation();
    }

    private void playEntranceAnimation() {
        ImageView ivBook = findViewById(R.id.ivBookIcon);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnSignUp = findViewById(R.id.btnSignUp);

        // Set initial state
        ivBook.setAlpha(0f);
        ivBook.setTranslationY(-30f);
        tvTitle.setAlpha(0f);
        tvTitle.setTranslationY(30f);
        tvSubtitle.setAlpha(0f);
        btnSignIn.setAlpha(0f);
        btnSignIn.setScaleX(0.8f);
        btnSignIn.setScaleY(0.8f);
        btnSignUp.setAlpha(0f);
        btnSignUp.setScaleX(0.8f);
        btnSignUp.setScaleY(0.8f);

        // Animate in sequence
        ivBook.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(200).start();
        tvTitle.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(400).start();
        tvSubtitle.animate().alpha(1f).setDuration(400).setStartDelay(700).start();
        btnSignIn.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(400).setStartDelay(900).start();
        btnSignUp.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(400).setStartDelay(1050).start();
    }

    private void setupButtons() {
        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnSignUp = findViewById(R.id.btnSignUp);

        btnSignIn.setOnClickListener(v -> {
            RecyclerViewAnimator.animateButtonClick(v);
            v.postDelayed(() -> {
                startActivity(new Intent(this, SignInActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }, 150);
        });

        btnSignUp.setOnClickListener(v -> {
            RecyclerViewAnimator.animateButtonClick(v);
            v.postDelayed(() -> {
                startActivity(new Intent(this, SignUpActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }, 150);
        });
    }

    private void autoRedirect(String email) {
        db.collection("admins").document(email).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        startActivity(new Intent(this, AdminDashboardActivity.class));
                        finish();
                    } else {
                        db.collection("students").document(email).get()
                                .addOnSuccessListener(studentDoc -> {
                                    if (studentDoc.exists()) {
                                        Boolean profileComplete = studentDoc.getBoolean("profileComplete");
                                        if (profileComplete != null && profileComplete) {
                                            startActivity(new Intent(this, StudentDashboardActivity.class));
                                        } else {
                                            startActivity(new Intent(this, ProfileSetupActivity.class));
                                        }
                                    } else {
                                        startActivity(new Intent(this, ProfileSetupActivity.class));
                                    }
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    setContentView(R.layout.activity_welcome);
                                    setupButtons();
                                    playEntranceAnimation();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    setContentView(R.layout.activity_welcome);
                    setupButtons();
                    playEntranceAnimation();
                });
    }
}
