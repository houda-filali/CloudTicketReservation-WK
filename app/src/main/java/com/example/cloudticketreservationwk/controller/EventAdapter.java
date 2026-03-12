package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.VH> {

    public static class EventItem {
        public final String id;
        public final String title;
        public final String date;
        public final String location;
        public final String category;
        public final String description;

        public EventItem(String id, String title, String date, String location, String category, String description) {
            this.id = id;
            this.title = title;
            this.date = date;
            this.location = location;
            this.category = category;
            this.description = description;
        }
    }

    public interface Listener {
        void onViewClicked(EventItem event);
    }

    private final List<EventItem> items;
    private final Listener listener;

    public EventAdapter(List<EventItem> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvLocation, tvCategory;
        MaterialButton btnView;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvLocation = itemView.findViewById(R.id.tvEventLocation);
            tvCategory = itemView.findViewById(R.id.tvEventCategory);
            btnView = itemView.findViewById(R.id.btnViewDetails);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EventItem e = items.get(position);

        h.tvTitle.setText(e.title);
        h.tvDate.setText(e.date);
        h.tvLocation.setText(e.location);
        h.tvCategory.setText(e.category);

        h.btnView.setOnClickListener(v -> listener.onViewClicked(e));
        h.itemView.setOnClickListener(v -> listener.onViewClicked(e));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}