package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.firebase.AuthService;
import com.example.cloudticketreservationwk.service.InMemoryStore;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EventListActivity extends AppCompatActivity implements EventAdapter.Listener {

    private final List<EventAdapter.EventItem> events = new ArrayList<>();
    private EventAdapter adapter;

    private TextInputEditText etSearch;
    private TextInputLayout tilSearch;
    private View tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        RecyclerView rvEvents = findViewById(R.id.rvEvents);
        etSearch = findViewById(R.id.etSearch);
        tvEmpty = findViewById(R.id.tvEmptyEvents);
        MaterialButton btnMyReservations = findViewById(R.id.btnMyReservations);

        adapter = new EventAdapter(events, this);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(adapter);

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> confirmLogout());

        tilSearch = findViewById(R.id.tilSearch);
        if (tilSearch != null) {
            tilSearch.setStartIconOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(EventListActivity.this, v);
                menu.getMenu().add(0, 0, 0, "All");
                menu.getMenu().add(0, 1, 1, "Date");
                menu.getMenu().add(0, 2, 2, "Location");
                menu.getMenu().add(0, 3, 3, "Category");

                menu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) {
                        tilSearch.setHint("Search events");
                        etSearch.setText("");
                        return true;
                    }
                    if (item.getItemId() == 1) {
                        tilSearch.setHint("Pick a date");
                        openMaterialDatePicker();
                        return true;
                    }
                    if (item.getItemId() == 2) {
                        tilSearch.setHint("Search location");
                        etSearch.setText("");
                        return true;
                    }
                    if (item.getItemId() == 3) {
                        tilSearch.setHint("Search category");
                        etSearch.setText("");
                        return true;
                    }
                    return false;
                });

                menu.show();
            });
        }

        btnMyReservations.setOnClickListener(v ->
                startActivity(new Intent(this, MyReservationsActivity.class))
        );

        reloadFromStore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadFromStore();
    }

    private void reloadFromStore() {
        events.clear();

        for (InMemoryStore.EventItem e : InMemoryStore.EVENTS) {
            events.add(new EventAdapter.EventItem(
                    e.id,
                    e.title,
                    e.date,
                    e.location,
                    e.category,
                    e.description
            ));
        }

        if (adapter != null) adapter.notifyDataSetChanged();
        if (tvEmpty != null) tvEmpty.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openMaterialDatePicker() {
        if (getSupportFragmentManager().findFragmentByTag("DATE_PICKER_FILTER") != null) return;

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            etSearch.setText(sdf.format(new Date(selection)));
        });

        picker.show(getSupportFragmentManager(), "DATE_PICKER_FILTER");
    }

    @Override
    public void onViewClicked(EventAdapter.EventItem event) {
        Intent i = new Intent(this, EventDetailsActivity.class);
        i.putExtra("EVENT_ID", event.id);
        i.putExtra("TITLE", event.title);
        i.putExtra("DATE", event.date);
        i.putExtra("LOCATION", event.location);
        i.putExtra("CATEGORY", event.category);
        i.putExtra("DESCRIPTION", event.description);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "My Reservations");
        menu.add(0, 2, 1, "Logout");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            startActivity(new Intent(this, MyReservationsActivity.class));
            return true;
        }
        if (item.getItemId() == 2) {
            confirmLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
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