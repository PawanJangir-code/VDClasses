package com.vd.vdclasses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AttendanceFragment extends Fragment {

    private TextView tvMonth, tvAttendanceSummary, tvAttendancePercent, tvStreakCount;
    private RecyclerView recyclerCalendar;
    private ShimmerFrameLayout shimmerLayout;
    private MaterialCardView attendanceCard, streakCard;
    private FirebaseFirestore db;

    private final Calendar currentMonth = Calendar.getInstance();
    private List<CalendarDay> calendarDays;
    private CalendarDayAdapter calendarAdapter;
    private String userEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            userEmail = user.getEmail();
        }

        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        attendanceCard = view.findViewById(R.id.attendanceCard);
        streakCard = view.findViewById(R.id.streakCard);
        tvMonth = view.findViewById(R.id.tvMonth);
        tvAttendanceSummary = view.findViewById(R.id.tvAttendanceSummary);
        tvAttendancePercent = view.findViewById(R.id.tvAttendancePercent);
        tvStreakCount = view.findViewById(R.id.tvStreakCount);
        recyclerCalendar = view.findViewById(R.id.recyclerCalendar);

        calendarDays = new ArrayList<>();
        calendarAdapter = new CalendarDayAdapter(calendarDays);
        recyclerCalendar.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        recyclerCalendar.setAdapter(calendarAdapter);

        ImageButton btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        ImageButton btnNextMonth = view.findViewById(R.id.btnNextMonth);
        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            loadAttendance();
        });
        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            loadAttendance();
        });

        loadAttendance();
        loadStreak();

        return view;
    }

    private void showShimmer() {
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        attendanceCard.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
        attendanceCard.setVisibility(View.VISIBLE);
    }

    private void loadAttendance() {
        if (userEmail == null) return;

        showShimmer();

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonth.setText(monthFormat.format(currentMonth.getTime()));

        Calendar monthStart = (Calendar) currentMonth.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(monthStart.getTime());

        Calendar monthEnd = (Calendar) currentMonth.clone();
        monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(monthEnd.getTime());

        db.collection("attendance")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!isAdded()) return;
                    Map<String, Long> presentDates = new HashMap<>();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        AttendanceModel record = doc.toObject(AttendanceModel.class);
                        String d = record.getDate();
                        if (d != null && d.compareTo(startDate) >= 0 && d.compareTo(endDate) <= 0) {
                            presentDates.put(d, record.getTimestamp());
                        }
                    }
                    buildCalendar(presentDates);
                    hideShimmer();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    buildCalendar(new HashMap<>());
                    hideShimmer();
                    Toast.makeText(requireContext(), "Failed to load attendance", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadStreak() {
        if (userEmail == null) return;

        db.collection("attendance")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!isAdded()) return;

                    Set<String> allDates = new HashSet<>();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        AttendanceModel record = doc.toObject(AttendanceModel.class);
                        if (record.getDate() != null) {
                            allDates.add(record.getDate());
                        }
                    }

                    // Calculate streak going backwards from today
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Calendar cal = Calendar.getInstance();
                    int streak = 0;

                    while (true) {
                        String dateStr = sdf.format(cal.getTime());
                        if (allDates.contains(dateStr)) {
                            streak++;
                            cal.add(Calendar.DAY_OF_MONTH, -1);
                        } else {
                            // If today hasn't been marked yet, try starting from yesterday
                            if (streak == 0) {
                                cal.add(Calendar.DAY_OF_MONTH, -1);
                                String yesterday = sdf.format(cal.getTime());
                                if (allDates.contains(yesterday)) {
                                    streak++;
                                    cal.add(Calendar.DAY_OF_MONTH, -1);
                                    while (allDates.contains(sdf.format(cal.getTime()))) {
                                        streak++;
                                        cal.add(Calendar.DAY_OF_MONTH, -1);
                                    }
                                }
                            }
                            break;
                        }
                    }

                    if (streak > 0) {
                        streakCard.setVisibility(View.VISIBLE);
                        tvStreakCount.setText(streak + "-day streak!");
                    } else {
                        streakCard.setVisibility(View.GONE);
                    }
                });
    }

    private void buildCalendar(Map<String, Long> presentDates) {
        calendarDays.clear();

        String[] headers = {"S", "M", "T", "W", "T", "F", "S"};
        for (String h : headers) {
            calendarDays.add(new CalendarDay(h));
        }

        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarDays.add(new CalendarDay());
        }

        Calendar today = Calendar.getInstance();
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        int presentCount = 0;
        int pastDayCount = 0;

        for (int day = 1; day <= daysInMonth; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            String dateStr = dateFormat.format(cal.getTime());

            boolean isToday = dateStr.equals(todayStr);
            boolean isFuture = cal.after(today) && !isToday;
            boolean isPresent = presentDates.containsKey(dateStr);
            long timestamp = isPresent ? presentDates.get(dateStr) : 0;

            if (!isFuture) pastDayCount++;
            if (isPresent) presentCount++;

            calendarDays.add(new CalendarDay(day, dateStr, isPresent, isFuture, isToday, timestamp));
        }

        calendarAdapter.notifyDataSetChanged();

        if (pastDayCount > 0) {
            int percent = Math.round((presentCount * 100f) / pastDayCount);
            tvAttendanceSummary.setText("Present: " + presentCount + " / " + pastDayCount + " days");
            tvAttendancePercent.setText(percent + "%");

            if (percent >= 75) {
                tvAttendancePercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.accentGreen));
            } else if (percent >= 50) {
                tvAttendancePercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
            } else {
                tvAttendancePercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.accentRed));
            }
        } else {
            tvAttendanceSummary.setText("No past days yet");
            tvAttendancePercent.setText("");
        }
    }
}
