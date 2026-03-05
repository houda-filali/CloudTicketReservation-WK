package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

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

        String line1 = (title == null || title.isEmpty()) ? "Reservation" : title;
        String line2 = (tickets == null || tickets.isEmpty()) ? "" : ("\nTickets: " + tickets);
        tvDetails.setText(line1 + line2);

        btnBack.setOnClickListener(v -> finish());

        btnMy.setOnClickListener(v -> {
            startActivity(new Intent(this, MyReservationsActivity.class));
            finish();
        });

        btnEvents.setOnClickListener(v -> {
            startActivity(new Intent(this, EventListActivity.class));
            finish();
        });
    }
}