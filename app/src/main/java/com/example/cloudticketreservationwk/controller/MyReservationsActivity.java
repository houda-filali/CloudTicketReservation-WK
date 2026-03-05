package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MyReservationsActivity extends AppCompatActivity implements ReservationAdapter.Listener {

    private final List<ReservationAdapter.ReservationItem> reservations = new ArrayList<>();
    private ReservationAdapter adapter;
    private View tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        tvEmpty = findViewById(R.id.tvEmptyReservations);
        RecyclerView rv = findViewById(R.id.rvMyReservations);
        MaterialButton btnBack = findViewById(R.id.btnBackFromReservations);

        btnBack.setOnClickListener(v -> finish());

        seedDummyReservations();

        adapter = new ReservationAdapter(reservations, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        tvEmpty.setVisibility(reservations.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void seedDummyReservations() {
        reservations.clear();
        reservations.add(new ReservationAdapter.ReservationItem("Comedy Night", "2026-03-10", "2", "Active"));
        reservations.add(new ReservationAdapter.ReservationItem("Tech Meetup", "2026-03-12", "1", "Active"));
    }

    @Override
    public void onCancelClicked(ReservationAdapter.ReservationItem r) {

        Snackbar.make(findViewById(android.R.id.content), "tba", Snackbar.LENGTH_SHORT).show();
    }
}