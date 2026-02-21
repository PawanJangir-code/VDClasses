package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvAdminName, tvAdminEmail;
    private TextView tvTotalStudents, tvTodayCheckins;
    private SharedPreferences prefs;
    private String adminEmail;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("admin_profile", MODE_PRIVATE);
        tvAdminName = findViewById(R.id.tvAdminName);
        tvAdminEmail = findViewById(R.id.tvAdminEmail);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvTodayCheckins = findViewById(R.id.tvTodayCheckins);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            adminEmail = user.getEmail();
            tvAdminEmail.setText(adminEmail);
            loadAdminProfile();
        }

        loadStats();

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
            v.postDelayed(this::showLogoutConfirmation, 150);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadAdminProfile() {
        String savedName = prefs.getString("name", "");
        if (!savedName.isEmpty()) {
            tvAdminName.setText(savedName);
        } else {
            tvAdminName.setText(adminEmail.substring(0, adminEmail.indexOf("@")));
        }
    }

    private void loadStats() {
        // Total students
        db.collection("students").get()
                .addOnSuccessListener(snap -> tvTotalStudents.setText(String.valueOf(snap.size())));

        // Today's check-ins
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        db.collection("attendance").whereEqualTo("date", today).get()
                .addOnSuccessListener(snap -> tvTodayCheckins.setText(String.valueOf(snap.size())));
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    getSharedPreferences("user_session", MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
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

        String savedName = prefs.getString("name", "");
        String savedPhone = prefs.getString("phone", "");
        if (!savedName.isEmpty()) etName.setText(savedName);
        if (!savedPhone.isEmpty()) etPhone.setText(savedPhone);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                    .putString("name", name)
                    .putString("phone", phone)
                    .apply();

            tvAdminName.setText(name);
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}
