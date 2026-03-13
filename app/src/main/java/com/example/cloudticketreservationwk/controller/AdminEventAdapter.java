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

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.VH> {

    public static class AdminEvent {
        public String id;
        public String title;
        public String date;
        public String location;
        public String category;
        public int capacity;
        public String status;

        public AdminEvent(String id, String title, String date, String location, String category, int capacity, String status) {
            this.id = id;
            this.title = title;
            this.date = date;
            this.location = location;
            this.category = category;
            this.capacity = capacity;
            this.status = status;
        }
    }

    public interface Listener {
        void onEdit(AdminEvent e);
        void onCancel(AdminEvent e);
        void onUncancel(AdminEvent e);
    }

    private final List<AdminEvent> items;
    private final Listener listener;

    public AdminEventAdapter(List<AdminEvent> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvLocation, tvCategory, tvCapacity, tvStatus;
        MaterialButton btnEdit, btnCancel, btnUncancel;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAdminEventTitle);
            tvDate = itemView.findViewById(R.id.tvAdminEventDate);
            tvLocation = itemView.findViewById(R.id.tvAdminEventLocation);
            tvCategory = itemView.findViewById(R.id.tvAdminEventCategory);
            tvCapacity = itemView.findViewById(R.id.tvAdminEventCapacity);
            tvStatus = itemView.findViewById(R.id.tvAdminEventStatus);
            btnEdit = itemView.findViewById(R.id.btnAdminEdit);
            btnCancel = itemView.findViewById(R.id.btnAdminCancel);
            btnUncancel = itemView.findViewById(R.id.btnAdminUncancel);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_event, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminEvent e = items.get(position);

        h.tvTitle.setText(e.title);
        h.tvDate.setText("Date: " + (e.date != null ? e.date : "Not set"));
        h.tvLocation.setText("Location: " + (e.location != null ? e.location : "Not set"));
        h.tvCategory.setText("Category: " + (e.category != null ? e.category : "Not set"));

        if (e.capacity > 0) {
            h.tvCapacity.setText("Capacity: " + e.capacity);
            h.tvCapacity.setVisibility(View.VISIBLE);
        } else {
            h.tvCapacity.setVisibility(View.GONE);
        }

        if ("Canceled".equals(e.status)) {
            h.tvStatus.setText("STATUS: CANCELED");
            h.tvStatus.setVisibility(View.VISIBLE);
            h.btnCancel.setVisibility(View.GONE);
            h.btnUncancel.setVisibility(View.VISIBLE);
        } else {
            h.tvStatus.setVisibility(View.GONE);
            h.btnCancel.setVisibility(View.VISIBLE);
            h.btnUncancel.setVisibility(View.GONE);
        }

        h.btnEdit.setOnClickListener(v -> listener.onEdit(e));
        h.btnCancel.setOnClickListener(v -> listener.onCancel(e));
        h.btnUncancel.setOnClickListener(v -> listener.onUncancel(e));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}