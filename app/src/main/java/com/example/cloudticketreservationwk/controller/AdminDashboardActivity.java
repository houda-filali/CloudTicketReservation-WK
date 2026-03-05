package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements AdminEventAdapter.Listener {

    private final List<AdminEventAdapter.AdminEvent> adminEvents = new ArrayList<>();
    private AdminEventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        View tvEmpty = findViewById(R.id.tvAdminEmpty);
        RecyclerView rv = findViewById(R.id.rvAdminEvents);
        MaterialButton fabAddEvent = findViewById(R.id.fabAddEvent);

        seedDummyAdminEvents();

        adapter = new AdminEventAdapter(adminEvents, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        tvEmpty.setVisibility(adminEvents.isEmpty() ? View.VISIBLE : View.GONE);

        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminEventFormActivity.class);
            intent.putExtra("MODE", "ADD");
            startActivity(intent);
        });
    }

    private void seedDummyAdminEvents() {
        adminEvents.clear();
        adminEvents.add(new AdminEventAdapter.AdminEvent("1", "Comedy Night", "2026-03-10", "Downtown", "Comedy", 100, "Active"));
        adminEvents.add(new AdminEventAdapter.AdminEvent("2", "Tech Meetup", "2026-03-12", "Campus Hall", "Tech", 50, "Active"));
        adminEvents.add(new AdminEventAdapter.AdminEvent("3", "Live Concert", "2026-03-20", "Main Arena", "Music", 500, "Canceled"));
    }

    @Override
    public void onEdit(AdminEventAdapter.AdminEvent e) {
        Intent intent = new Intent(this, AdminEventFormActivity.class);
        intent.putExtra("MODE", "EDIT");
        intent.putExtra("EVENT_TITLE", e.title);
        intent.putExtra("EVENT_DATE", e.date);
        intent.putExtra("EVENT_LOCATION", e.location);
        intent.putExtra("EVENT_CATEGORY", e.category);
        intent.putExtra("EVENT_CAPACITY", e.capacity);
        startActivity(intent);
    }

    @Override
    public void onCancel(AdminEventAdapter.AdminEvent e) {
        Snackbar.make(findViewById(android.R.id.content), "tba", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onUncancel(AdminEventAdapter.AdminEvent e) {
        Snackbar.make(findViewById(android.R.id.content), "tba", Snackbar.LENGTH_SHORT).show();
    }
}