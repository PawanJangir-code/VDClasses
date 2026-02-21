package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private TextInputLayout tilConfirmPassword;
    private Button btnSignIn, btnSignUp;
    private TextView tvToggleMode;

    private boolean isSignUpMode = false;

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
        initViews();
        setupListeners();
        playEntranceAnimation();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvToggleMode = findViewById(R.id.tvToggleMode);
    }

    private void setupListeners() {
        btnSignIn.setOnClickListener(v -> signIn());
        btnSignUp.setOnClickListener(v -> signUp());
        tvToggleMode.setOnClickListener(v -> toggleMode());
    }

    private void toggleMode() {
        isSignUpMode = !isSignUpMode;
        if (isSignUpMode) {
            tilConfirmPassword.setVisibility(View.VISIBLE);
            btnSignIn.setVisibility(View.GONE);
            btnSignUp.setVisibility(View.VISIBLE);
            tvToggleMode.setText("Already have an account? Sign In");
        } else {
            tilConfirmPassword.setVisibility(View.GONE);
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignUp.setVisibility(View.GONE);
            tvToggleMode.setText("Don't have an account? Sign Up");
        }
    }

    private void signIn() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkRoleAndRedirect(email);
                    } else {
                        Toast.makeText(this, "Login failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signUp() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        StudentModel student = new StudentModel(email);
                        db.collection("students").document(email).set(student)
                                .addOnSuccessListener(aVoid -> {
                                    Intent intent = new Intent(this, ProfileSetupActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to create student record",
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Sign up failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkRoleAndRedirect(String email) {
        db.collection("admins").document(email).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Intent intent = new Intent(this, AdminDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else {
                        db.collection("students").document(email).get()
                                .addOnSuccessListener(studentDoc -> {
                                    Intent intent;
                                    if (studentDoc.exists()) {
                                        Boolean profileComplete = studentDoc.getBoolean("profileComplete");
                                        if (profileComplete != null && profileComplete) {
                                            intent = new Intent(this, StudentDashboardActivity.class);
                                        } else {
                                            intent = new Intent(this, ProfileSetupActivity.class);
                                        }
                                    } else {
                                        intent = new Intent(this, ProfileSetupActivity.class);
                                    }
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error checking profile", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking role. Try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void playEntranceAnimation() {
        ImageView ivBook = findViewById(R.id.ivBookIcon);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        View tilEmail = findViewById(R.id.tilEmail);
        View tilPassword = findViewById(R.id.tilPassword);

        View[] views = {ivBook, tvTitle, tvSubtitle, tilEmail, tilPassword, btnSignIn, tvToggleMode};

        for (View v : views) {
            v.setAlpha(0f);
            v.setTranslationY(30f);
        }

        // Book icon drops down from above
        ivBook.setTranslationY(-30f);

        int delay = 200;
        for (View v : views) {
            v.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(delay).start();
            delay += 120;
        }
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
                                    initViews();
                                    setupListeners();
                                    playEntranceAnimation();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    setContentView(R.layout.activity_welcome);
                    initViews();
                    setupListeners();
                    playEntranceAnimation();
                });
    }
}
