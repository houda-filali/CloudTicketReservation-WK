package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.firebase.AuthService;
import com.example.cloudticketreservationwk.service.InMemoryStore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> confirmLogout());

        btnBack.setOnClickListener(v -> finish());

        adapter = new ReservationAdapter(reservations, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        reloadFromStore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadFromStore();
    }

    private void reloadFromStore() {
        reservations.clear();

        for (InMemoryStore.ReservationItem r : InMemoryStore.MY_RESERVATIONS) {
            reservations.add(new ReservationAdapter.ReservationItem(
                    r.id,
                    r.event.title,
                    r.event.date,
                    String.valueOf(r.tickets),
                    r.status
            ));
        }

        if (adapter != null) adapter.notifyDataSetChanged();
        if (tvEmpty != null) tvEmpty.setVisibility(reservations.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCancelClicked(ReservationAdapter.ReservationItem r) {
        InMemoryStore.cancelReservation(r.id);
        reloadFromStore();
        Snackbar.make(findViewById(android.R.id.content), "Reservation canceled", Snackbar.LENGTH_SHORT).show();
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