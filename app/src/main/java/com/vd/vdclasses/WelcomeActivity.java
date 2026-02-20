package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

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

        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnSignUp = findViewById(R.id.btnSignUp);

        btnSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
        });

        btnSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });
    }

    private void autoRedirect(String email) {
        db.collection("admins").document(email).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        startActivity(new Intent(this, AdminDashboardActivity.class));
                        finish();
                    } else {
                        // Check if student profile is complete
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
                                    // Fall through to welcome screen on error
                                    setContentView(R.layout.activity_welcome);
                                    setupButtons();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    setContentView(R.layout.activity_welcome);
                    setupButtons();
                });
    }

    private void setupButtons() {
        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnSignUp = findViewById(R.id.btnSignUp);

        btnSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
        });

        btnSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });
    }
}
