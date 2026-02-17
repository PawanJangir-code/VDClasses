package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddVideoActivity extends AppCompatActivity {

    EditText etVideoTitle, etVideoSubject, etYoutubeUrl;
    Button btnSaveVideo;
    FirebaseFirestore db;
    String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        etVideoTitle = findViewById(R.id.etVideoTitle);
        etVideoSubject = findViewById(R.id.etVideoSubject);
        etYoutubeUrl = findViewById(R.id.etYoutubeUrl);
        btnSaveVideo = findViewById(R.id.btnSaveVideo);
        db = FirebaseFirestore.getInstance();

        documentId = getIntent().getStringExtra("documentId");
        if (documentId != null) {
            // Edit mode — pre-fill fields
            TextView tvTitle = findViewById(R.id.tvAddVideoTitle);
            tvTitle.setText("Edit Video");
            btnSaveVideo.setText("Update Video");

            etVideoTitle.setText(getIntent().getStringExtra("title"));
            etVideoSubject.setText(getIntent().getStringExtra("subject"));
            etYoutubeUrl.setText(getIntent().getStringExtra("youtubeUrl"));
        }

        btnSaveVideo.setOnClickListener(v -> saveVideo());
    }

    private void saveVideo() {
        String title = etVideoTitle.getText().toString().trim();
        String subject = etVideoSubject.getText().toString().trim();
        String url = etYoutubeUrl.getText().toString().trim();

        if (title.isEmpty()) {
            etVideoTitle.setError("Title is required");
            return;
        }
        if (subject.isEmpty()) {
            etVideoSubject.setError("Subject is required");
            return;
        }
        if (url.isEmpty()) {
            etYoutubeUrl.setError("YouTube URL is required");
            return;
        }

        if (documentId != null) {
            // Update existing document
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", title);
            updates.put("subject", subject);
            updates.put("youtubeUrl", url);

            db.collection("videos").document(documentId).update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Video updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update video", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Add new document
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
