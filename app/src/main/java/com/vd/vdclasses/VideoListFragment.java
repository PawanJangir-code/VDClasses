package com.vd.vdclasses;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class VideoListFragment extends Fragment {

    private RecyclerView rvVideos;
    private ChipGroup chipGroupSubjects;
    private ShimmerFrameLayout shimmerLayout;
    private View contentLayout;
    private SwipeRefreshLayout swipeRefresh;
    private FirebaseFirestore db;

    private ImageButton btnSearch, btnFilter;
    private EditText etSearch;
    private View filterContainer;
    private TextView tvEmpty;

    private final List<VideoModel> allVideos = new ArrayList<>();
    private final List<VideoModel> filteredVideos = new ArrayList<>();
    private StudentVideoAdapter adapter;
    private String selectedSubject = null;
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvVideos = view.findViewById(R.id.rvVideos);
        chipGroupSubjects = view.findViewById(R.id.chipGroupSubjects);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        contentLayout = view.findViewById(R.id.contentLayout);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnFilter = view.findViewById(R.id.btnFilter);
        etSearch = view.findViewById(R.id.etSearch);
        filterContainer = view.findViewById(R.id.filterContainer);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        db = FirebaseFirestore.getInstance();

        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadVideos);

        adapter = new StudentVideoAdapter(filteredVideos, video -> {
            Intent intent = new Intent(requireContext(), VideoPlayerActivity.class);
            intent.putExtra("videoUrl", video.getVideoUrl());
            intent.putExtra("title", video.getTitle());
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        rvVideos.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvVideos.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> toggleSearch());
        btnFilter.setOnClickListener(v -> toggleFilter());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        showShimmer();
        loadVideos();
    }

    private void toggleSearch() {
        if (etSearch.getVisibility() == View.VISIBLE) {
            etSearch.animate()
                    .alpha(0f)
                    .translationY(-etSearch.getHeight())
                    .setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        etSearch.setVisibility(View.GONE);
                        etSearch.setAlpha(1f);
                        etSearch.setTranslationY(0f);
                    })
                    .start();
            etSearch.setText("");
            searchQuery = "";
            applyFilter();
        } else {
            etSearch.setVisibility(View.VISIBLE);
            etSearch.setAlpha(0f);
            etSearch.setTranslationY(-etSearch.getHeight());
            etSearch.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
            etSearch.requestFocus();
        }
    }

    private void toggleFilter() {
        if (filterContainer.getVisibility() == View.VISIBLE) {
            filterContainer.animate()
                    .alpha(0f)
                    .translationY(-filterContainer.getHeight())
                    .setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        filterContainer.setVisibility(View.GONE);
                        filterContainer.setAlpha(1f);
                        filterContainer.setTranslationY(0f);
                    })
                    .start();
        } else {
            filterContainer.setVisibility(View.VISIBLE);
            filterContainer.setAlpha(0f);
            filterContainer.setTranslationY(-filterContainer.getHeight());
            filterContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
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

    private void loadVideos() {
        db.collection("videos")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allVideos.clear();
                    Set<String> subjects = new LinkedHashSet<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        VideoModel video = doc.toObject(VideoModel.class);
                        allVideos.add(video);
                        if (video.getSubject() != null && !video.getSubject().isEmpty()) {
                            subjects.add(video.getSubject());
                        }
                    }

                    buildSubjectChips(subjects);
                    applyFilter();
                    hideShimmer();
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void buildSubjectChips(Set<String> subjects) {
        chipGroupSubjects.removeAllViews();

        Chip allChip = new Chip(requireContext());
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setOnClickListener(v -> {
            selectedSubject = null;
            applyFilter();
        });
        chipGroupSubjects.addView(allChip);

        for (String subject : subjects) {
            Chip chip = new Chip(requireContext());
            chip.setText(subject);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                selectedSubject = subject;
                applyFilter();
            });
            chipGroupSubjects.addView(chip);
        }
    }

    private void applyFilter() {
        filteredVideos.clear();
        String query = searchQuery.toLowerCase();

        for (VideoModel video : allVideos) {
            boolean matchesSubject = selectedSubject == null || selectedSubject.equals(video.getSubject());
            boolean matchesSearch = query.isEmpty() ||
                    (video.getTitle() != null && video.getTitle().toLowerCase().contains(query));

            if (matchesSubject && matchesSearch) {
                filteredVideos.add(video);
            }
        }

        adapter.notifyDataSetChanged();

        // Show/hide empty state
        if (filteredVideos.isEmpty() && !allVideos.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }

        // Update chip checked states
        for (int i = 0; i < chipGroupSubjects.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupSubjects.getChildAt(i);
            if (selectedSubject == null) {
                chip.setChecked(i == 0);
            } else {
                chip.setChecked(selectedSubject.equals(chip.getText().toString()));
            }
        }
    }
}
