package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewStudentsActivity extends AppCompatActivity {

    RecyclerView recyclerStudents;
    EditText etSearchStudents;
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
        db = FirebaseFirestore.getInstance();

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

    private void loadStudents() {
        db.collection("students")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fullStudentList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        StudentModel student = doc.toObject(StudentModel.class);
                        fullStudentList.add(student);
                    }
                    Collections.sort(fullStudentList, (a, b) -> {
                        String emailA = a.getEmail() != null ? a.getEmail().toLowerCase() : "";
                        String emailB = b.getEmail() != null ? b.getEmail().toLowerCase() : "";
                        return emailA.compareTo(emailB);
                    });
                    filterStudents(etSearchStudents.getText().toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterStudents(String query) {
        studentList.clear();
        String lowerQuery = query.toLowerCase().trim();
        for (StudentModel student : fullStudentList) {
            String email = student.getEmail() != null ? student.getEmail().toLowerCase() : "";
            if (lowerQuery.isEmpty() || email.contains(lowerQuery)) {
                studentList.add(student);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
