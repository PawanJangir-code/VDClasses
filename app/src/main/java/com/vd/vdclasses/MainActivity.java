package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    EditText etEmail, etPassword;
    Button btnLogin, btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnLogin.setOnClickListener(v -> loginUser());
        btnSignUp.setOnClickListener(v -> signUpUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkRoleAndRedirect(email);
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signUpUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Register as student in Firestore, then check role to redirect
                        db.collection("students")
                                .document(email)
                                .set(new StudentModel(email))
                                .addOnSuccessListener(aVoid -> {
                                    checkRoleAndRedirect(email);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to register student",
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Sign up failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkRoleAndRedirect(String email) {
        db.collection("admins")
                .document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        startActivity(new Intent(this, AdminDashboardActivity.class));
                    } else {
                        startActivity(new Intent(this, StudentDashboardActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking role. Try again.",
                            Toast.LENGTH_SHORT).show();
                });
    }
}
