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
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class VideoListFragment extends Fragment {

    private RecyclerView rvVideos;
    private ShimmerFrameLayout shimmerLayout;
    private View contentLayout;
    private SwipeRefreshLayout swipeRefresh;
    private FirebaseFirestore db;

    private ImageButton btnSearch, btnFilter;
    private EditText etSearch;
    private TextView tvEmpty;

    private final List<VideoModel> allVideos = new ArrayList<>();
    private final List<VideoModel> filteredVideos = new ArrayList<>();
    private StudentVideoAdapter adapter;
    private Set<String> subjectList = new LinkedHashSet<>();
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
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        contentLayout = view.findViewById(R.id.contentLayout);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnFilter = view.findViewById(R.id.btnFilter);
        etSearch = view.findViewById(R.id.etSearch);
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
        btnFilter.setOnClickListener(v -> showFilterMenu());

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

    private void showFilterMenu() {
        PopupMenu popup = new PopupMenu(requireContext(), btnFilter);

        // "All" as first item
        popup.getMenu().add(0, 0, 0, "All");

        int id = 1;
        for (String subject : subjectList) {
            popup.getMenu().add(0, id++, id, subject);
        }

        // Show checkmark on the currently selected item
        for (int i = 0; i < popup.getMenu().size(); i++) {
            android.view.MenuItem item = popup.getMenu().getItem(i);
            if (selectedSubject == null && i == 0) {
                item.setChecked(true);
            } else if (selectedSubject != null && selectedSubject.equals(item.getTitle().toString())) {
                item.setChecked(true);
            }
            item.setCheckable(true);
        }

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) {
                selectedSubject = null;
            } else {
                selectedSubject = item.getTitle().toString();
            }
            applyFilter();
            return true;
        });

        popup.show();
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

                    buildSubjectList(subjects);
                    applyFilter();
                    hideShimmer();
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void buildSubjectList(Set<String> subjects) {
        subjectList = subjects;
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
    }
}
