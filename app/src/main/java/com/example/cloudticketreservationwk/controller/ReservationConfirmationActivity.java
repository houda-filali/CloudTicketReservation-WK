package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.firebase.AuthService;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ReservationConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_confirmation);

        String title = getIntent().getStringExtra("TITLE");
        String tickets = getIntent().getStringExtra("TICKETS");

        TextView tvDetails = findViewById(R.id.tvConfirmDetails);
        MaterialButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnMy = findViewById(R.id.btnGoMyReservations);
        MaterialButton btnEvents = findViewById(R.id.btnGoEvents);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        String line1 = (title == null || title.isEmpty()) ? "Reservation" : title;
        String line2 = (tickets == null || tickets.isEmpty()) ? "" : ("\nTickets: " + tickets);
        tvDetails.setText(line1 + line2);

        btnBack.setOnClickListener(v -> finish());

        btnMy.setOnClickListener(v -> {
            startActivity(new Intent(
                    ReservationConfirmationActivity.this,
                    com.example.cloudticketreservationwk.controller.MyReservationsActivity.class
            ));
            finish();
        });

        btnEvents.setOnClickListener(v -> {
            startActivity(new Intent(
                    ReservationConfirmationActivity.this,
                    com.example.cloudticketreservationwk.controller.EventListActivity.class
            ));
            finish();
        });

        if (btnLogout != null) btnLogout.setOnClickListener(v -> confirmLogout());
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