package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        RecyclerView rvEvents = findViewById(R.id.rvEvents);
        etSearch = findViewById(R.id.etSearch);
        View tvEmpty = findViewById(R.id.tvEmptyEvents);
        MaterialButton btnMyReservations = findViewById(R.id.btnMyReservations);

        seedDummyEvents();

        adapter = new EventAdapter(events, this);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(adapter);

        tvEmpty.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);

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

    private void seedDummyEvents() {
        events.clear();
        events.add(new EventAdapter.EventItem("Comedy Night", "2026-03-10", "Downtown", "Comedy", "A fun comedy show."));
        events.add(new EventAdapter.EventItem("Tech Meetup", "2026-03-12", "Campus Hall", "Tech", "Talks + networking."));
        events.add(new EventAdapter.EventItem("Live Concert", "2026-03-20", "Main Arena", "Music", "Live performance night."));
        events.add(new EventAdapter.EventItem("Food Festival", "2026-03-25", "Old Port", "Food", "Local food + vendors."));
    }

    @Override
    public void onViewClicked(EventAdapter.EventItem event) {
        Intent i = new Intent(this, EventDetailsActivity.class);
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
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}