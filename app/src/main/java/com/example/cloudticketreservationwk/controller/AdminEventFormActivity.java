package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_form);

        etEventTitle = findViewById(R.id.etEventTitle);
        etEventDate = findViewById(R.id.etEventDate);
        etEventLocation = findViewById(R.id.etEventLocation);
        etEventCategory = findViewById(R.id.etEventCategory);
        etEventCapacity = findViewById(R.id.etEventCapacity);
        etEventDescription = findViewById(R.id.etEventDescription);

        tilEventDate = findViewById(R.id.tilEventDate);
        tvAdminFormTitle = findViewById(R.id.tvAdminFormTitle);

        MaterialButton btnBack = findViewById(R.id.btnBackFromForm);
        MaterialButton btnSave = findViewById(R.id.btnSaveEvent);

        String mode = getIntent().getStringExtra("MODE");
        if ("EDIT".equals(mode)) {
            tvAdminFormTitle.setText("Edit Event");
            btnSave.setText("Update");

            etEventTitle.setText(getIntent().getStringExtra("EVENT_TITLE"));
            etEventDate.setText(getIntent().getStringExtra("EVENT_DATE"));
            etEventLocation.setText(getIntent().getStringExtra("EVENT_LOCATION"));
            etEventCategory.setText(getIntent().getStringExtra("EVENT_CATEGORY"));

            int cap = getIntent().getIntExtra("EVENT_CAPACITY", 0);
            if (cap > 0) etEventCapacity.setText(String.valueOf(cap));
        } else {
            tvAdminFormTitle.setText("Add Event");
            btnSave.setText("Save");
        }

        if (etEventDate != null) {
            etEventDate.setInputType(InputType.TYPE_NULL);
            etEventDate.setKeyListener(null);
            etEventDate.setCursorVisible(false);
            etEventDate.setLongClickable(false);
            etEventDate.setFocusable(false);
            etEventDate.setFocusableInTouchMode(false);
            etEventDate.setClickable(true);
        }

        View.OnClickListener dateClick = v -> openMaterialCalendar();
        if (etEventDate != null) etEventDate.setOnClickListener(dateClick);
        if (tilEventDate != null) tilEventDate.setOnClickListener(dateClick);

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            setResult(RESULT_OK, new Intent());
            finish();
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
            if (etEventDate != null) etEventDate.setText(sdf.format(new Date(selection)));
        });

        picker.show(getSupportFragmentManager(), "DATE_PICKER");
    }
}