package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewStudentsActivity extends AppCompatActivity {

    RecyclerView recyclerStudents;
    TextInputEditText etSearchStudents;
    ShimmerFrameLayout shimmerLayout;
    FirebaseFirestore db;
    List<StudentModel> studentList;
    List<StudentModel> fullStudentList;
    StudentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_students);

        recyclerStudents = findViewById(R.id.recyclerStudents);
        etSearchStudents = findViewById(R.id.etSearchStudents);
        shimmerLayout = findViewById(R.id.shimmerLayout);
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        studentList = new ArrayList<>();
        fullStudentList = new ArrayList<>();
        adapter = new StudentAdapter(studentList);

        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        recyclerStudents.setAdapter(adapter);

        etSearchStudents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadStudents();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void showShimmer() {
        shimmerLayout.setVisibility(android.view.View.VISIBLE);
        shimmerLayout.startShimmer();
        recyclerStudents.setVisibility(android.view.View.GONE);
    }

    private void hideShimmer() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(android.view.View.GONE);
        recyclerStudents.setVisibility(android.view.View.VISIBLE);
    }

    private void loadStudents() {
        showShimmer();
        db.collection("students")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fullStudentList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        StudentModel student = doc.toObject(StudentModel.class);
                        fullStudentList.add(student);
                    }
                    Collections.sort(fullStudentList, (a, b) -> {
                        String nameA = a.getName() != null && !a.getName().isEmpty()
                                ? a.getName().toLowerCase()
                                : (a.getEmail() != null ? a.getEmail().toLowerCase() : "");
                        String nameB = b.getName() != null && !b.getName().isEmpty()
                                ? b.getName().toLowerCase()
                                : (b.getEmail() != null ? b.getEmail().toLowerCase() : "");
                        return nameA.compareTo(nameB);
                    });
                    filterStudents(etSearchStudents.getText() != null ? etSearchStudents.getText().toString() : "");
                    hideShimmer();
                })
                .addOnFailureListener(e -> {
                    hideShimmer();
                    Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterStudents(String query) {
        studentList.clear();
        String lowerQuery = query.toLowerCase().trim();
        for (StudentModel student : fullStudentList) {
            String email = student.getEmail() != null ? student.getEmail().toLowerCase() : "";
            String name = student.getName() != null ? student.getName().toLowerCase() : "";
            if (lowerQuery.isEmpty() || name.contains(lowerQuery) || email.contains(lowerQuery)) {
                studentList.add(student);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
