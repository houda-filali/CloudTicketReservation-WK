package com.example.cloudticketreservationwk.service;
import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.cloudticketreservationwk.model.Event;

public class EventService {
    private FirebaseFirestore db;
    private Context context;  // For showing Toasts if needed

    // Interface for callbacks (so activities know when operations complete)
    public interface EventCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public interface EventsCallback {
        void onSuccess(List<Event> events);
        void onFailure(String error);
    }

    public EventService(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.context = context;
    }
    // Adding an event
    public void addEvent(Event event, EventCallback callback) {
        String eventId = db.collection("events").document().getId();
        event.setId(eventId);

        db.collection("events")
                .document(eventId)
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess("Event added successfully with ID: " + eventId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Error adding event: " + e.getMessage());
                });
    }
    public void getAllEvents(EventsCallback callback) {
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Event> eventList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            event.setId(document.getId());
                            eventList.add(event);
                        }
                        callback.onSuccess(eventList);
                    } else {
                        callback.onFailure("Error getting events: " + task.getException().getMessage());
                    }
                });
    }
    public void editEvent(String eventId, Map<String, Object> updates, EventCallback callback) {
        db.collection("events").document(eventId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess("Event updated successfully");
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Error updating event: " + e.getMessage());
                });
    }

    // Overloaded method for full event update
    public void editEvent(Event event, EventCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", event.getTitle());
        updates.put("date", event.getDate());
        updates.put("location", event.getLocation());
        updates.put("category", event.getCategory());
        updates.put("totalSeats", event.getTotalSeats());
        updates.put("availableSeats", event.getAvailableSeats());

        editEvent(event.getId(), updates, callback);
    }

    public void seedInitialEvents(EventCallback callback) {
        // Check if events already exist
        db.collection("events").limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No events exist, create sample events
                        List<Event> sampleEvents = new ArrayList<>();
                        sampleEvents.add(new Event("Comedy Night", "2026-03-10", "Downtown", "Comedy", 100, 100));
                        sampleEvents.add(new Event("Tech Meetup", "2026-03-12", "Campus Hall", "Tech", 50, 50));
                        sampleEvents.add(new Event("Live Concert", "2026-03-20", "Main Arena", "Music", 500, 500));
                        sampleEvents.add(new Event("Food Festival", "2026-03-25", "Old Port", "Food", 200, 200));

                        for (Event event : sampleEvents) {
                            String eventId = db.collection("events").document().getId();
                            event.setId(eventId);
                            db.collection("events").document(eventId)
                                    .set(event)
                                    .addOnFailureListener(e ->
                                            System.out.println("Failed to seed event: " + e.getMessage()));
                        }
                        callback.onSuccess("Sample events created");
                    } else {
                        callback.onSuccess("Events already exist");
                    }
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to check events: " + e.getMessage()));
    }





}
