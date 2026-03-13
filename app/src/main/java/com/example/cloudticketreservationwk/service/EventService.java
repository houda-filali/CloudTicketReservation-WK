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
        db.collection("events")
                .add(event)
                .addOnSuccessListener(documentReference -> {
                    event.setId(documentReference.getId());
                    callback.onSuccess("Event added successfully with ID: " + documentReference.getId());
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
        updates.put("name", event.getName());
        updates.put("date", event.getDate());
        updates.put("location", event.getLocation());
        updates.put("category", event.getCategory());
        updates.put("totalSeats", event.getTotalSeats());
        updates.put("availableSeats", event.getAvailableSeats());

        editEvent(event.getId(), updates, callback);
    }




}
