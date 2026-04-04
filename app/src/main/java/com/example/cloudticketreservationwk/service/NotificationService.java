package com.example.cloudticketreservationwk.service;

import android.content.Context;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

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
        db.collection("reservations")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "Active")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    int count = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String userId = doc.getString("userId");
                        String reservationId = doc.getId();

                        // update reservation status
                        batch.update(doc.getReference(), "status", "Cancelled");

                        // create in-app notification
                        Map<String, Object> notification = new HashMap<>();
                        notification.put("userId", userId);
                        notification.put("eventId", eventId);
                        notification.put("eventName", eventName);
                        notification.put("reservationId", reservationId);
                        notification.put("message", "Your reservation for \"" + eventName + "\" was cancelled because the event was cancelled by the administrator.");
                        notification.put("timestamp", com.google.firebase.Timestamp.now());
                        notification.put("read", false);
                        notification.put("type", "EVENT_CANCELLED");

                        batch.set(db.collection("notifications").document(), notification);
                        count++;
                    }

                    int finalCount = count;
                    batch.commit()
                            .addOnSuccessListener(unused ->
                                    callback.onSuccess("Cancelled " + finalCount + " reservations and notified users"))
                            .addOnFailureListener(e ->
                                    callback.onFailure("Batch cancellation failed: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to fetch reservations: " + e.getMessage()));
    }
}