package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.firebase.AuthService;
import com.example.cloudticketreservationwk.service.EventService;
import com.example.cloudticketreservationwk.service.InMemoryStore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cloudticketreservationwk.service.ReservationService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class EventDetailsActivity extends AppCompatActivity {
    private EventService eventService;
    private String eventId;
    private String eventTitle;
    private ReservationService reservationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        reservationService = new ReservationService(this);
        eventService = new EventService(this);

        String eventId = getIntent().getStringExtra("EVENT_ID");
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

        btnReserve.setOnClickListener(v -> {
            String ticketsText = etTickets.getText() == null ? "" : etTickets.getText().toString().trim();

            if (ticketsText.isEmpty()) {
                etTickets.setError("Enter number of tickets");
                return;
            }

            int tickets;
            try {
                tickets = Integer.parseInt(ticketsText);
            } catch (NumberFormatException e) {
                etTickets.setError("Enter a valid number");
                return;
            }

            if (tickets <= 0) {
                etTickets.setError("Enter at least 1 ticket");
                return;
            }

            InMemoryStore.addReservation(eventId, tickets);

            Intent i = new Intent(this, ReservationConfirmationActivity.class);
            i.putExtra("TITLE", title);
            i.putExtra("TICKETS", String.valueOf(tickets));
            startActivity(i);
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