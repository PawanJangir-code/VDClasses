package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageVideosActivity extends AppCompatActivity {

    RecyclerView recyclerVideos;
    FloatingActionButton fabAddVideo;
    TextInputEditText etSearchVideos;
    ShimmerFrameLayout shimmerLayout;
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
        shimmerLayout = findViewById(R.id.shimmerLayout);
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        videoList = new ArrayList<>();
        documentIds = new ArrayList<>();
        fullVideoList = new ArrayList<>();
        fullDocumentIds = new ArrayList<>();

        adapter = new VideoAdapter(videoList, documentIds,
                (documentId, position) -> {
                    db.collection("videos").document(documentId).delete()
                            .addOnSuccessListener(aVoid -> {
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
                    intent.putExtra("videoUrl", video.getVideoUrl());
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                });

        adapter.setOnVideoClickListener(video -> {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("videoUrl", video.getVideoUrl());
            intent.putExtra("title", video.getTitle());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        recyclerVideos.setLayoutManager(new LinearLayoutManager(this));
        recyclerVideos.setAdapter(adapter);

        fabAddVideo.setOnClickListener(v -> {
            startActivity(new Intent(this, AddVideoActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void showShimmer() {
        shimmerLayout.setVisibility(android.view.View.VISIBLE);
        shimmerLayout.startShimmer();
        recyclerVideos.setVisibility(android.view.View.GONE);
    }

    private void hideShimmer() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(android.view.View.GONE);
        recyclerVideos.setVisibility(android.view.View.VISIBLE);
    }

    private void loadVideos() {
        showShimmer();
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
                    filterVideos(etSearchVideos.getText() != null ? etSearchVideos.getText().toString() : "");
                    hideShimmer();
                })
                .addOnFailureListener(e -> {
                    hideShimmer();
                    Toast.makeText(this, "Failed to load videos: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
