package com.vd.vdclasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarDayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_EMPTY = 1;
    private static final int TYPE_DAY = 2;

    private final List<CalendarDay> items;

    public CalendarDayAdapter(List<CalendarDay> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        switch (items.get(position).getType()) {
            case HEADER: return TYPE_HEADER;
            case EMPTY:  return TYPE_EMPTY;
            default:     return TYPE_DAY;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderVH(inflater.inflate(R.layout.item_calendar_header, parent, false));
        } else if (viewType == TYPE_EMPTY) {
            return new EmptyVH(inflater.inflate(R.layout.item_calendar_empty, parent, false));
        } else {
            return new DayVH(inflater.inflate(R.layout.item_calendar_day, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CalendarDay item = items.get(position);

        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).tvLabel.setText(item.getLabel());
        } else if (holder instanceof DayVH) {
            DayVH dayVH = (DayVH) holder;
            Context ctx = dayVH.itemView.getContext();
            dayVH.tvDay.setText(String.valueOf(item.getDayNumber()));

            // Set background based on state
            if (item.isFuture()) {
                dayVH.tvDay.setBackgroundResource(R.drawable.bg_calendar_future);
                dayVH.tvDay.setTextColor(ContextCompat.getColor(ctx, R.color.textSecondary));
            } else if (item.isPresent()) {
                dayVH.tvDay.setBackgroundResource(R.drawable.bg_calendar_present);
                dayVH.tvDay.setTextColor(ContextCompat.getColor(ctx, R.color.white));
            } else {
                dayVH.tvDay.setBackgroundResource(R.drawable.bg_calendar_absent);
                dayVH.tvDay.setTextColor(ContextCompat.getColor(ctx, R.color.accentRed));
            }

            // Today ring overlay
            if (item.isToday()) {
                dayVH.tvDay.setForeground(ContextCompat.getDrawable(ctx, R.drawable.bg_calendar_today));
            } else {
                dayVH.tvDay.setForeground(null);
            }

            // Tap on present day shows check-in time
            if (item.isPresent() && item.getCheckInTimestamp() > 0) {
                dayVH.itemView.setOnClickListener(v -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    String time = sdf.format(new Date(item.getCheckInTimestamp()));
                    Toast.makeText(ctx, "Checked in at " + time, Toast.LENGTH_SHORT).show();
                });
            } else {
                dayVH.itemView.setOnClickListener(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvLabel;
        HeaderVH(View v) {
            super(v);
            tvLabel = v.findViewById(R.id.tvDayLabel);
        }
    }

    static class EmptyVH extends RecyclerView.ViewHolder {
        EmptyVH(View v) { super(v); }
    }

    static class DayVH extends RecyclerView.ViewHolder {
        TextView tvDay;
        DayVH(View v) {
            super(v);
            tvDay = v.findViewById(R.id.tvDayNumber);
        }
    }
}
