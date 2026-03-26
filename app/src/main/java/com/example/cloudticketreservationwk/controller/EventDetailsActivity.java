package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.firebase.AuthService;
import com.example.cloudticketreservationwk.service.EventService;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cloudticketreservationwk.service.ReservationService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class EventDetailsActivity extends AppCompatActivity {
    private EventService eventService;
    private String eventId;
    private ReservationService reservationService;
    private TextView tvSeats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        reservationService = new ReservationService(this);
        eventService = new EventService(this);

        eventId = getIntent().getStringExtra("EVENT_ID");
        String title = getIntent().getStringExtra("TITLE");
        String date = getIntent().getStringExtra("DATE");
        String location = getIntent().getStringExtra("LOCATION");
        String category = getIntent().getStringExtra("CATEGORY");
        String desc = getIntent().getStringExtra("DESCRIPTION");

        TextView tvTitle = findViewById(R.id.tvDetailsTitle);
        TextView tvDate = findViewById(R.id.tvDetailsDate);
        TextView tvLocation = findViewById(R.id.tvDetailsLocation);
        TextView tvCategory = findViewById(R.id.tvDetailsCategory);
        TextView tvDesc = findViewById(R.id.tvDetailsDescription);
        tvSeats = findViewById(R.id.tvSeats);

        TextInputEditText etTickets = findViewById(R.id.etTickets);
        MaterialButton btnReserve = findViewById(R.id.btnReserve);
        MaterialButton btnBackToEvents = findViewById(R.id.btnBackToEvents);

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> confirmLogout());

        btnBackToEvents.setOnClickListener(v -> finish());

        tvTitle.setText(title == null ? "" : title);
        tvDate.setText(date == null ? "" : date);
        tvLocation.setText(location == null ? "" : location);
        tvCategory.setText(category == null ? "" : category);
        tvDesc.setText(desc == null ? "" : desc);

        // Fetch available seats
        if (eventId != null) {
            FirebaseFirestore.getInstance().collection("events").document(eventId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;
                    Long seats = snapshot.getLong("availableSeats");
                    tvSeats.setText("Available seats: " + (seats != null ? seats : "0"));
                });
        }

        btnReserve.setOnClickListener(v -> {
            String ticketsText = etTickets.getText() == null ? "" : etTickets.getText().toString().trim();
            if (ticketsText.isEmpty()) { etTickets.setError("Enter number of tickets"); return; }

            int tickets;
            try { tickets = Integer.parseInt(ticketsText); }
            catch (NumberFormatException ex) { etTickets.setError("Enter a valid number"); return; }
            if (tickets <= 0) { etTickets.setError("Enter at least 1 ticket"); return; }

            btnReserve.setEnabled(false);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                btnReserve.setEnabled(true);
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }

            DocumentReference eventRef = db.collection("events").document(eventId);
            final int finalTickets = tickets;
            final String finalTitle = title;
            final String finalDate = date;

            db.runTransaction(transaction -> {
                DocumentSnapshot snap = transaction.get(eventRef);
                Long seats = snap.getLong("availableSeats");
                if (seats == null || seats < finalTickets) {
                    throw new FirebaseFirestoreException("Not enough seats", FirebaseFirestoreException.Code.ABORTED);
                }
                transaction.update(eventRef, "availableSeats", seats - finalTickets);
                return null;
            }).addOnSuccessListener(unused -> {
                Map<String, Object> res = new HashMap<>();
                res.put("userId", currentUser.getUid());
                res.put("eventId", eventId);
                res.put("eventName", finalTitle);
                res.put("eventDate", finalDate);
                res.put("numberOfTickets", finalTickets);
                res.put("reservationDate", FieldValue.serverTimestamp());
                res.put("status", "Active");

                db.collection("reservations").add(res)
                    .addOnSuccessListener(ref -> {
                        btnReserve.setEnabled(true);
                        Intent i = new Intent(this, ReservationConfirmationActivity.class);
                        i.putExtra("TITLE", finalTitle);
                        i.putExtra("TICKETS", String.valueOf(finalTickets));
                        i.putExtra("RESERVATION_ID", ref.getId());
                        startActivity(i);
                    })
                    .addOnFailureListener(e -> {
                        btnReserve.setEnabled(true);
                        Toast.makeText(this, "Failed to save reservation: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
            }).addOnFailureListener(e -> {
                btnReserve.setEnabled(true);
                String msg = e.getMessage() != null && e.getMessage().contains("Not enough seats")
                    ? "Not enough seats available" : "Reservation failed: " + e.getMessage();
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            });
        });
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
