package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.firebase.AuthService;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MyReservationsActivity extends AppCompatActivity implements ReservationAdapter.Listener {

    private final List<ReservationAdapter.ReservationItem> reservations = new ArrayList<>();
    private ReservationAdapter adapter;
    private View tvEmpty;
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        tvEmpty = findViewById(R.id.tvEmptyReservations);
        RecyclerView rv = findViewById(R.id.rvMyReservations);
        MaterialButton btnBack = findViewById(R.id.btnBackFromReservations);

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> confirmLogout());

        btnBack.setOnClickListener(v -> finish());

        adapter = new ReservationAdapter(reservations, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        setupSnapshotListener();
    }

    private void setupSnapshotListener() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        if (registration != null) return;

        registration = FirebaseFirestore.getInstance()
                .collection("reservations")
                .whereEqualTo("userId", user.getUid())
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    reservations.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String status = doc.getString("status");
                        Long ticketsLong = doc.getLong("numberOfTickets");
                        String eventDate = doc.getString("eventDate");
                        String eventId = doc.getString("eventId");
                        reservations.add(new ReservationAdapter.ReservationItem(
                                doc.getId(),
                                eventId != null ? eventId : "",
                                doc.getString("eventName"),
                                eventDate != null ? eventDate : "",
                                ticketsLong != null ? String.valueOf(ticketsLong) : "1",
                                status != null ? status : "Active"
                        ));
                    }
                    if (adapter != null) adapter.notifyDataSetChanged();
                    if (tvEmpty != null) tvEmpty.setVisibility(reservations.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

    @Override
    public void onCancelClicked(ReservationAdapter.ReservationItem r) {
        if ("Cancelled".equals(r.status)) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference resRef = db.collection("reservations").document(r.id);

        db.runTransaction(transaction -> {
            DocumentSnapshot resSnap = transaction.get(resRef);
            if (!resSnap.exists()) return null;

            String eventId = resSnap.getString("eventId");
            Long tickets = resSnap.getLong("numberOfTickets");
            final long finalTickets = tickets != null ? tickets : 0;

            DocumentSnapshot eventSnap = null;
            DocumentReference eventRef = null;
            if (eventId != null && finalTickets > 0) {
                eventRef = db.collection("events").document(eventId);
                eventSnap = transaction.get(eventRef);
            }

            transaction.update(resRef, "status", "Cancelled");

            if (eventSnap != null && eventSnap.exists() && eventRef != null) {
                long currentAvailable = eventSnap.getLong("availableSeats") != null ? eventSnap.getLong("availableSeats") : 0;
                long totalSeats = eventSnap.getLong("totalSeats") != null ? eventSnap.getLong("totalSeats") : 0;
                
                // Cap available seats to total capacity
                long newAvailable = Math.min(totalSeats, currentAvailable + finalTickets);
                transaction.update(eventRef, "availableSeats", newAvailable);
            }
            return null;
        }).addOnSuccessListener(unused ->
                Snackbar.make(findViewById(android.R.id.content), "Reservation cancelled", Snackbar.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Snackbar.make(findViewById(android.R.id.content), "Cancel failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show()
        );
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Logout", (d, w) -> doLogout())
                .show();
    }

    private void doLogout() {
        new AuthService().logout();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
