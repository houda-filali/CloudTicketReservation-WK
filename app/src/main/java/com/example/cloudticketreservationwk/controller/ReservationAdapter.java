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

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.VH> {

    public static class ReservationItem {
        public final String id;
        public final String eventId;
        public final String title;
        public final String date;
        public final String tickets;
        public final String status;

        public ReservationItem(String id, String eventId, String title, String date, String tickets, String status) {
            this.id = id;
            this.eventId = eventId;
            this.title = title;
            this.date = date;
            this.tickets = tickets;
            this.status = status;
        }
    }

    public interface Listener {
        void onCancelClicked(ReservationItem r);
    }

    private final List<ReservationItem> items;
    private final Listener listener;

    public ReservationAdapter(List<ReservationItem> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvTickets, tvStatus;
        MaterialButton btnCancel;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvResEventTitle);
            tvDate = itemView.findViewById(R.id.tvResDate);
            tvTickets = itemView.findViewById(R.id.tvResTickets);
            tvStatus = itemView.findViewById(R.id.tvResStatus);
            btnCancel = itemView.findViewById(R.id.btnCancelReservation);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ReservationItem r = items.get(position);

        h.tvTitle.setText(r.title);
        h.tvDate.setText("Date: " + (r.date != null ? r.date : ""));
        h.tvTickets.setText("Tickets: " + r.tickets);
        h.tvStatus.setText("Status: " + r.status);

        // Fixed spelling to match Firestore "Cancelled"
        if ("Cancelled".equals(r.status)) {
            h.btnCancel.setEnabled(false);
            h.btnCancel.setText("Cancelled");
        } else {
            h.btnCancel.setEnabled(true);
            h.btnCancel.setText("Cancel");
        }

        h.btnCancel.setOnClickListener(v -> listener.onCancelClicked(r));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
