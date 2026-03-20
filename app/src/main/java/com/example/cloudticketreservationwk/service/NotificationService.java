// Create NotificationService.java
package com.example.cloudticketreservationwk.service;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class NotificationService {

    private FirebaseFirestore db;
    private Context context;

    public interface NotificationCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public NotificationService(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.context = context;
    }

    public void notifyEventCancellation(String eventId, String eventName, NotificationCallback callback) {
        // Query all reservations for this event
        db.collection("reservations")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "Active")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int notificationCount = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String userId = doc.getString("userId");
                        String reservationId = doc.getId();

                        // Create notification for user
                        createNotification(userId, eventId, eventName, reservationId);

                        // Update reservation status
                        doc.getReference().update("status", "Cancelled");

                        notificationCount++;
                    }

                    callback.onSuccess("Cancelled event and notified " + notificationCount + " users");
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to notify users: " + e.getMessage()));
    }

    private void createNotification(String userId, String eventId, String eventName, String reservationId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("eventId", eventId);
        notification.put("eventName", eventName);
        notification.put("reservationId", reservationId);
        notification.put("message", "Event '" + eventName + "' has been cancelled. Your reservation has been cancelled.");
        notification.put("timestamp", com.google.firebase.Timestamp.now());
        notification.put("read", false);
        notification.put("type", "EVENT_CANCELLED");

        db.collection("notifications").add(notification);
    }
}
