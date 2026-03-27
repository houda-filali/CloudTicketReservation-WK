package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.firebase.AuthService;
import com.example.cloudticketreservationwk.model.Event;
import com.example.cloudticketreservationwk.service.EventService;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AdminEventFormActivity extends AppCompatActivity {

    private TextInputEditText etEventTitle;
    private TextInputEditText etEventDate;
    private TextInputEditText etEventLocation;
    private TextInputEditText etEventCategory;
    private TextInputEditText etEventCapacity;
    private TextInputEditText etEventDescription;

    private TextInputLayout tilEventDate;
    private TextView tvAdminFormTitle;
    private MaterialButton btnSave;
    private EventService eventService;
    private String mode;
    private String eventId;
    private int oldAvailableSeats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_form);

        initializeViews();
        initializeServices();
        getIntentData();
        setupFormBasedOnMode();
        setupDatePicker();
        setupClickListeners();
    }

    private void initializeViews() {
        etEventTitle = findViewById(R.id.etEventTitle);
        etEventDate = findViewById(R.id.etEventDate);
        etEventLocation = findViewById(R.id.etEventLocation);
        etEventCategory = findViewById(R.id.etEventCategory);
        etEventCapacity = findViewById(R.id.etEventCapacity);
        etEventDescription = findViewById(R.id.etEventDescription);
        tilEventDate = findViewById(R.id.tilEventDate);
        tvAdminFormTitle = findViewById(R.id.tvAdminFormTitle);
        btnSave = findViewById(R.id.btnSaveEvent);

        MaterialButton btnBack = findViewById(R.id.btnBackFromForm);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        btnBack.setOnClickListener(v -> finish());
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> confirmLogout());
        }
    }

    private void initializeServices() {
        eventService = new EventService(this);
    }

    private void getIntentData() {
        mode = getIntent().getStringExtra("MODE");
        eventId = getIntent().getStringExtra("EVENT_ID");
        oldAvailableSeats = getIntent().getIntExtra("EVENT_AVAILABLE", 0);
    }

    private void setupFormBasedOnMode() {
        if ("EDIT".equals(mode)) {
            // EDIT MODE configuration
            tvAdminFormTitle.setText("Edit Event");
            btnSave.setText("Update Event");

            // Populate fields with existing event data
            etEventTitle.setText(getIntent().getStringExtra("EVENT_TITLE"));
            etEventDate.setText(getIntent().getStringExtra("EVENT_DATE"));
            etEventLocation.setText(getIntent().getStringExtra("EVENT_LOCATION"));
            etEventCategory.setText(getIntent().getStringExtra("EVENT_CATEGORY"));

            int cap = getIntent().getIntExtra("EVENT_CAPACITY", 0);
            if (cap > 0) {
                etEventCapacity.setText(String.valueOf(cap));
            }

            String desc = getIntent().getStringExtra("EVENT_DESCRIPTION");
            if (desc != null && !desc.isEmpty()) {
                etEventDescription.setText(desc);
            }
        } else {
            // ADD MODE configuration
            tvAdminFormTitle.setText("Add New Event");
            btnSave.setText("Create Event");
            // Fields remain empty for user input
        }
    }

    private void setupDatePicker() {
        if (etEventDate != null) {
            etEventDate.setInputType(InputType.TYPE_NULL);
            etEventDate.setKeyListener(null);
            etEventDate.setCursorVisible(false);
            etEventDate.setFocusable(false);
            etEventDate.setClickable(true);
        }

        View.OnClickListener dateClick = v -> openMaterialCalendar();
        if (etEventDate != null) {
            etEventDate.setOnClickListener(dateClick);
        }
        if (tilEventDate != null) {
            tilEventDate.setOnClickListener(dateClick);
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                if ("EDIT".equals(mode)) {
                    updateEvent();
                } else {
                    addNewEvent();
                }
            }
        });
    }

    private boolean validateInputs() {
        if (etEventTitle.getText().toString().trim().isEmpty()) {
            etEventTitle.setError("Title is required");
            return false;
        }
        if (etEventDate.getText().toString().trim().isEmpty()) {
            etEventDate.setError("Date is required");
            return false;
        }
        if (etEventLocation.getText().toString().trim().isEmpty()) {
            etEventLocation.setError("Location is required");
            return false;
        }
        if (etEventCategory.getText().toString().trim().isEmpty()) {
            etEventCategory.setError("Category is required");
            return false;
        }

        String capacityStr = etEventCapacity.getText().toString().trim();
        if (capacityStr.isEmpty()) {
            etEventCapacity.setError("Capacity is required");
            return false;
        }

        try {
            int capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                etEventCapacity.setError("Capacity must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            etEventCapacity.setError("Invalid capacity");
            return false;
        }

        return true;
    }

    private void addNewEvent() {
        String title = etEventTitle.getText().toString().trim();
        String date = etEventDate.getText().toString().trim();
        String location = etEventLocation.getText().toString().trim();
        String category = etEventCategory.getText().toString().trim();
        int capacity = Integer.parseInt(etEventCapacity.getText().toString().trim());

        Event event = new Event(title, date, location, category, capacity, capacity);

        eventService.addEvent(event, new EventService.EventCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(AdminEventFormActivity.this,
                        "Event created successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String error) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Failed to create event: " + error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void updateEvent() {
        String title = etEventTitle.getText().toString().trim();
        String date = etEventDate.getText().toString().trim();
        String location = etEventLocation.getText().toString().trim();
        String category = etEventCategory.getText().toString().trim();
        int newTotalSeats = Integer.parseInt(etEventCapacity.getText().toString().trim());

        // Calculate new available seats based on existing reservations
        int oldTotalSeats = getIntent().getIntExtra("EVENT_CAPACITY", 0);
        int reservedSeats = oldTotalSeats - oldAvailableSeats;
        int newAvailableSeats = newTotalSeats - reservedSeats;

        // If new capacity is less than existing reservations, we set available to 0
        // (This prevents negative available seats but shows "Full")
        if (newAvailableSeats < 0) newAvailableSeats = 0;

        Event event = new Event(title, date, location, category, newAvailableSeats, newTotalSeats);
        event.setId(eventId);

        eventService.editEvent(event, new EventService.EventCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(AdminEventFormActivity.this,
                        "Event updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String error) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Failed to update event: " + error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void openMaterialCalendar() {
        if (getSupportFragmentManager().findFragmentByTag("DATE_PICKER") != null) return;

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            if (etEventDate != null) {
                etEventDate.setText(sdf.format(new Date(selection)));
            }
        });

        picker.show(getSupportFragmentManager(), "DATE_PICKER");
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
