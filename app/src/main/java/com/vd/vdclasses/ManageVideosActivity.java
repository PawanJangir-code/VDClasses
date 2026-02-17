package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageVideosActivity extends AppCompatActivity {

    RecyclerView recyclerVideos;
    FloatingActionButton fabAddVideo;
    EditText etSearchVideos;
    FirebaseFirestore db;
    List<VideoModel> videoList;
    List<String> documentIds;
    List<VideoModel> fullVideoList;
    List<String> fullDocumentIds;
    VideoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_videos);

        recyclerVideos = findViewById(R.id.recyclerVideos);
        fabAddVideo = findViewById(R.id.fabAddVideo);
        etSearchVideos = findViewById(R.id.etSearchVideos);
        db = FirebaseFirestore.getInstance();

        videoList = new ArrayList<>();
        documentIds = new ArrayList<>();
        fullVideoList = new ArrayList<>();
        fullDocumentIds = new ArrayList<>();

        adapter = new VideoAdapter(videoList, documentIds,
                (documentId, position) -> {
                    db.collection("videos").document(documentId).delete()
                            .addOnSuccessListener(aVoid -> {
                                // Remove from full lists as well
                                int fullIndex = fullDocumentIds.indexOf(documentId);
                                if (fullIndex != -1) {
                                    fullVideoList.remove(fullIndex);
                                    fullDocumentIds.remove(fullIndex);
                                }
                                videoList.remove(position);
                                documentIds.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(this, "Video deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                            });
                },
                (documentId, video) -> {
                    Intent intent = new Intent(this, AddVideoActivity.class);
                    intent.putExtra("documentId", documentId);
                    intent.putExtra("title", video.getTitle());
                    intent.putExtra("subject", video.getSubject());
                    intent.putExtra("youtubeUrl", video.getYoutubeUrl());
                    startActivity(intent);
                });

        recyclerVideos.setLayoutManager(new LinearLayoutManager(this));
        recyclerVideos.setAdapter(adapter);

        fabAddVideo.setOnClickListener(v -> {
            startActivity(new Intent(this, AddVideoActivity.class));
        });

        etSearchVideos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVideos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVideos();
    }

    private void loadVideos() {
        db.collection("videos")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fullVideoList.clear();
                    fullDocumentIds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        VideoModel video = doc.toObject(VideoModel.class);
                        fullVideoList.add(video);
                        fullDocumentIds.add(doc.getId());
                    }
                    filterVideos(etSearchVideos.getText().toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load videos", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterVideos(String query) {
        videoList.clear();
        documentIds.clear();
        String lowerQuery = query.toLowerCase().trim();
        for (int i = 0; i < fullVideoList.size(); i++) {
            VideoModel video = fullVideoList.get(i);
            if (lowerQuery.isEmpty()
                    || video.getTitle().toLowerCase().contains(lowerQuery)
                    || video.getSubject().toLowerCase().contains(lowerQuery)) {
                videoList.add(video);
                documentIds.add(fullDocumentIds.get(i));
            }
        }
        adapter.notifyDataSetChanged();
    }
}
