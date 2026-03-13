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

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements AdminEventAdapter.Listener {

    private final List<AdminEventAdapter.AdminEvent> adminEvents = new ArrayList<>();
    private AdminEventAdapter adapter;
    private View tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvEmpty = findViewById(R.id.tvAdminEmpty);
        RecyclerView rv = findViewById(R.id.rvAdminEvents);

        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        adapter = new AdminEventAdapter(adminEvents, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        if (btnLogout != null) btnLogout.setOnClickListener(v -> confirmLogout());

        View addBtn = findViewByIdName("btnAddEvent");
        if (addBtn == null) addBtn = findViewByIdName("fabAddEvent");

        if (addBtn != null) {
            addBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminEventFormActivity.class);
                intent.putExtra("MODE", "ADD");
                startActivity(intent);
            });
        }

        reloadFromStore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadFromStore();
    }

    private void reloadFromStore() {
        adminEvents.clear();
        for (InMemoryStore.EventItem e : InMemoryStore.EVENTS) {
            adminEvents.add(new AdminEventAdapter.AdminEvent(
                    e.id, e.title, e.date, e.location, e.category, e.capacity, e.status
            ));
        }
        if (adapter != null) adapter.notifyDataSetChanged();
        if (tvEmpty != null) tvEmpty.setVisibility(adminEvents.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private View findViewByIdName(String idName) {
        int id = getResources().getIdentifier(idName, "id", getPackageName());
        if (id == 0) return null;
        return findViewById(id);
    }

    @Override
    public void onEdit(AdminEventAdapter.AdminEvent e) {
        Intent intent = new Intent(this, AdminEventFormActivity.class);
        intent.putExtra("MODE", "EDIT");
        intent.putExtra("EVENT_ID", e.id);
        intent.putExtra("EVENT_TITLE", e.title);
        intent.putExtra("EVENT_DATE", e.date);
        intent.putExtra("EVENT_LOCATION", e.location);
        intent.putExtra("EVENT_CATEGORY", e.category);
        intent.putExtra("EVENT_CAPACITY", e.capacity);
        startActivity(intent);
    }

    @Override
    public void onCancel(AdminEventAdapter.AdminEvent e) {
        InMemoryStore.EventItem real = InMemoryStore.findEventById(e.id);
        if (real != null) real.status = InMemoryStore.STATUS_CANCELED;
        reloadFromStore();
    }

    @Override
    public void onUncancel(AdminEventAdapter.AdminEvent e) {
        InMemoryStore.EventItem real = InMemoryStore.findEventById(e.id);
        if (real != null) real.status = InMemoryStore.STATUS_ACTIVE;
        reloadFromStore();
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