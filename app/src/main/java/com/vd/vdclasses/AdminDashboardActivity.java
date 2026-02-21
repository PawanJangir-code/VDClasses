package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvAdminName, tvAdminEmail;
    private FirebaseFirestore db;
    private String adminEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();
        tvAdminName = findViewById(R.id.tvAdminName);
        tvAdminEmail = findViewById(R.id.tvAdminEmail);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            adminEmail = user.getEmail();
            tvAdminEmail.setText(adminEmail);
            loadAdminProfile();
        }

        // Tap account card to edit profile
        MaterialCardView cardAccountInfo = findViewById(R.id.cardAccountInfo);
        cardAccountInfo.setOnClickListener(v -> {
            RecyclerViewAnimator.animateButtonClick(v);
            v.postDelayed(this::showEditProfileDialog, 150);
        });

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

    private void loadAdminProfile() {
        db.collection("admins").document(adminEmail)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvAdminName.setText(name);
                        } else {
                            tvAdminName.setText(adminEmail.substring(0, adminEmail.indexOf("@")));
                        }
                    } else {
                        tvAdminName.setText(adminEmail.substring(0, adminEmail.indexOf("@")));
                    }
                })
                .addOnFailureListener(e -> {
                    tvAdminName.setText(adminEmail.substring(0, adminEmail.indexOf("@")));
                });
    }

    private void showEditProfileDialog() {
        if (adminEmail == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_admin_profile, null);
        dialog.setContentView(view);

        TextView tvDialogEmail = view.findViewById(R.id.tvDialogEmail);
        TextInputEditText etName = view.findViewById(R.id.etAdminName);
        TextInputEditText etPhone = view.findViewById(R.id.etAdminPhone);
        Button btnSave = view.findViewById(R.id.btnSaveProfile);

        tvDialogEmail.setText(adminEmail);

        // Pre-fill existing values
        db.collection("admins").document(adminEmail)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String phone = doc.getString("phone");
                        if (name != null) etName.setText(name);
                        if (phone != null) etPhone.setText(phone);
                    }
                });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("phone", phone);

            db.collection("admins").document(adminEmail)
                    .set(updates, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        tvAdminName.setText(name);
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }
}
