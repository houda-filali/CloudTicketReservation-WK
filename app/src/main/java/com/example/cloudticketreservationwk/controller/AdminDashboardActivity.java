package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.firebase.AuthService;
import com.example.cloudticketreservationwk.service.EventService;
import com.example.cloudticketreservationwk.service.NotificationService;
import com.example.cloudticketreservationwk.service.ReservationService;
import com.example.cloudticketreservationwk.model.Event;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity implements AdminEventAdapter.Listener {

    private static final int REQUEST_CODE_ADD_EDIT = 1001;
    private final List<AdminEventAdapter.AdminEvent> adminEvents = new ArrayList<>();
    private AdminEventAdapter adapter;
    private View tvEmpty;
    private EventService eventService;
    private NotificationService notificationService;
    private ReservationService reservationService;
    private FirebaseFirestore db;

    public void setEventService(EventService service) { this.eventService = service; }
    public void setNotificationService(NotificationService service) { this.notificationService = service; }
    public void setReservationService(ReservationService service) { this.reservationService = service; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initializeServices();
        initializeViews();
        setupAddButton();
        
        loadEventsFromFirestore();
    }

    private void initializeViews() {
        tvEmpty = findViewById(R.id.tvAdminEmpty);
        RecyclerView rv = findViewById(R.id.rvAdminEvents);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        adapter = new AdminEventAdapter(adminEvents, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> confirmLogout());
        }
    }

    private void initializeServices() {
        eventService = new EventService(this);
        notificationService = new NotificationService(this);
        reservationService = new ReservationService(this);
        db = FirebaseFirestore.getInstance();
    }

    private void setupAddButton() {
        View addBtn = findViewByIdName("btnAddEvent");
        if (addBtn == null) {
            addBtn = findViewByIdName("fabAddEvent");
        }

        if (addBtn != null) {
            addBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminEventFormActivity.class);
                intent.putExtra("MODE", "ADD");
                startActivityForResult(intent, REQUEST_CODE_ADD_EDIT);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEventsFromFirestore();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_EDIT && resultCode == RESULT_OK) {
            loadEventsFromFirestore();
            Toast.makeText(this, "Events refreshed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadEventsFromFirestore() {
        // Seed first if empty, then fetch
        eventService.seedInitialEvents(new EventService.EventCallback() {
            @Override
            public void onSuccess(String message) {
                fetchEvents();
            }

            @Override
            public void onFailure(String error) {
                fetchEvents(); // Still try to fetch even if seed check fails
            }
        });
    }

    private void fetchEvents() {
        eventService.getAllEvents(new EventService.EventsCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                adminEvents.clear();
                for (Event event : events) {
                    adminEvents.add(new AdminEventAdapter.AdminEvent(
                            event.getId(),
                            event.getTitle(),
                            event.getDate(),
                            event.getLocation(),
                            event.getCategory(),
                            event.getTotalSeats(),
                            event.getAvailableSeats(),
                            event.getIsCancelled() ? "Canceled" : "Active"
                    ));
                }
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(adminEvents.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Failed to load events: " + error, Toast.LENGTH_SHORT).show();

                if (error.contains("permission") || error.contains("denied")) {
                    showFirestorePermissionDialog();
                }

                adminEvents.clear();
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
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
        intent.putExtra("EVENT_AVAILABLE", e.availableSeats);
        startActivityForResult(intent, REQUEST_CODE_ADD_EDIT);
    }

    @Override
    public void onCancel(AdminEventAdapter.AdminEvent e) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel Event")
                .setMessage("Are you sure you want to cancel this event?\n\n" +
                        "This will:\n" +
                        "• Cancel all active reservations\n" +
                        "• Notify all users with reservations\n" +
                        "• Make the event unavailable for new bookings")
                .setNegativeButton("No", (d, w) -> d.dismiss())
                .setPositiveButton("Yes, Cancel Event", (d, w) -> {
                    Toast.makeText(this, "Processing cancellation...", Toast.LENGTH_SHORT).show();

                    notificationService.notifyEventCancellation(e.id, e.title,
                            new NotificationService.NotificationCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    updateEventStatus(e.id, true);
                                }

                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(AdminDashboardActivity.this,
                                            "Failed to notify users: " + error, Toast.LENGTH_SHORT).show();
                                    updateEventStatus(e.id, true);
                                }
                            });
                })
                .show();
    }

    private void updateEventStatus(String eventId, boolean isCancelled) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isCancelled", isCancelled);

        db.collection("events").document(eventId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminDashboardActivity.this, "Status updated", Toast.LENGTH_SHORT).show();
                    loadEventsFromFirestore();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(AdminDashboardActivity.this, "Update failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    loadEventsFromFirestore();
                });
    }

    @Override
    public void onUncancel(AdminEventAdapter.AdminEvent e) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Reactivate Event")
                .setMessage("Are you sure you want to reactivate this event?")
                .setNegativeButton("No", (d, w) -> d.dismiss())
                .setPositiveButton("Yes, Reactivate", (d, w) -> updateEventStatus(e.id, false))
                .show();
    }

    public void onDelete(AdminEventAdapter.AdminEvent e) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to permanently delete this event?")
                .setNegativeButton("No", (d, w) -> d.dismiss())
                .setPositiveButton("Yes, Delete", (d, w) -> {
                    db.collection("events").document(e.id)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AdminDashboardActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                                loadEventsFromFirestore();
                            })
                            .addOnFailureListener(error -> {
                                Toast.makeText(AdminDashboardActivity.this, "Delete failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .show();
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

    private void showFirestorePermissionDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Firestore Permission Error")
                .setMessage("Unable to access Firestore. Please check your Firebase rules.")
                .setPositiveButton("OK", (d, w) -> d.dismiss())
                .show();
    }
}
