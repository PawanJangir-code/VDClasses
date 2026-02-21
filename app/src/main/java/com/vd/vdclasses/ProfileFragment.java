package com.vd.vdclasses;

import android.app.DatePickerDialog;
import android.content.Intent;
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

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvEmail;
    private TextInputEditText etName, etPhone, etClass, etDob;
    private Button btnEditProfile, btnSaveProfile, btnLogout;
    private ShimmerFrameLayout shimmerLayout;
    private View contentLayout;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isEditing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEmail = view.findViewById(R.id.tvEmail);
        etName = view.findViewById(R.id.etName);
        etPhone = view.findViewById(R.id.etPhone);
        etClass = view.findViewById(R.id.etClass);
        etDob = view.findViewById(R.id.etDob);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        contentLayout = view.findViewById(R.id.contentLayout);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        showShimmer();
        loadProfile();

        etDob.setOnClickListener(v -> {
            if (isEditing) showDatePicker();
        });

        btnEditProfile.setOnClickListener(v -> {
            RecyclerViewAnimator.animateButtonClick(v);
            v.postDelayed(() -> toggleEdit(true), 150);
        });
        btnSaveProfile.setOnClickListener(v -> {
            RecyclerViewAnimator.animateButtonClick(v);
            v.postDelayed(() -> saveProfile(), 150);
        });
        btnLogout.setOnClickListener(v -> {
            RecyclerViewAnimator.animateButtonClick(v);
            v.postDelayed(() -> logout(), 150);
        });
    }

    private void showShimmer() {
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        contentLayout.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
    }

    private void loadProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        tvEmail.setText(user.getEmail());

        db.collection("students").document(user.getEmail()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String phone = doc.getString("phone");
                        String studentClass = doc.getString("studentClass");
                        String dob = doc.getString("dateOfBirth");

                        if (name != null) etName.setText(name);
                        if (phone != null) etPhone.setText(phone);
                        if (studentClass != null) etClass.setText(studentClass);
                        if (dob != null) etDob.setText(dob);
                    }
                    hideShimmer();
                })
                .addOnFailureListener(e -> hideShimmer());
    }

    private void toggleEdit(boolean editing) {
        isEditing = editing;
        etName.setEnabled(editing);
        etPhone.setEnabled(editing);
        etClass.setEnabled(editing);
        etDob.setEnabled(editing);

        btnEditProfile.setVisibility(editing ? View.GONE : View.VISIBLE);
        btnSaveProfile.setVisibility(editing ? View.VISIBLE : View.GONE);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            etDob.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String studentClass = etClass.getText() != null ? etClass.getText().toString().trim() : "";
        String dob = etDob.getText() != null ? etDob.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("studentClass", studentClass);
        updates.put("dateOfBirth", dob);

        db.collection("students").document(user.getEmail())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    toggleEdit(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(requireContext(), WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
