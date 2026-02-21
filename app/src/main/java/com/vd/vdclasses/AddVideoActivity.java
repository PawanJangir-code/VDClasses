package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddVideoActivity extends AppCompatActivity {

    TextInputEditText etVideoTitle, etVideoSubject, etVideoUrl;
    Button btnSaveVideo;
    FirebaseFirestore db;
    String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        etVideoTitle = findViewById(R.id.etVideoTitle);
        etVideoSubject = findViewById(R.id.etVideoSubject);
        etVideoUrl = findViewById(R.id.etVideoUrl);
        btnSaveVideo = findViewById(R.id.btnSaveVideo);
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        documentId = getIntent().getStringExtra("documentId");
        if (documentId != null) {
            TextView tvTitle = findViewById(R.id.tvAddVideoTitle);
            tvTitle.setText("Edit Video");
            btnSaveVideo.setText("Update Video");

            etVideoTitle.setText(getIntent().getStringExtra("title"));
            etVideoSubject.setText(getIntent().getStringExtra("subject"));
            etVideoUrl.setText(getIntent().getStringExtra("videoUrl"));
        }

        btnSaveVideo.setOnClickListener(v -> {
            RecyclerViewAnimator.animateButtonClick(v);
            v.postDelayed(() -> saveVideo(), 150);
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void saveVideo() {
        String title = etVideoTitle.getText() != null ? etVideoTitle.getText().toString().trim() : "";
        String subject = etVideoSubject.getText() != null ? etVideoSubject.getText().toString().trim() : "";
        String url = etVideoUrl.getText() != null ? etVideoUrl.getText().toString().trim() : "";

        if (title.isEmpty()) {
            etVideoTitle.setError("Title is required");
            return;
        }
        if (subject.isEmpty()) {
            etVideoSubject.setError("Subject is required");
            return;
        }
        if (url.isEmpty()) {
            etVideoUrl.setError("Video URL is required");
            return;
        }

        if (documentId != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", title);
            updates.put("subject", subject);
            updates.put("videoUrl", url);

            db.collection("videos").document(documentId).update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Video updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update video", Toast.LENGTH_SHORT).show();
                    });
        } else {
            VideoModel video = new VideoModel(title, subject, url);

            db.collection("videos").add(video)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Video added", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to add video", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
