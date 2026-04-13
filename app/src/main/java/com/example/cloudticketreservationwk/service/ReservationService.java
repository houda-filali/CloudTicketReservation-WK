package com.example.cloudticketreservationwk.service;

import android.content.Context;

import com.example.cloudticketreservationwk.model.User;
import com.example.cloudticketreservationwk.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Context context;

    public interface ReservationCallback {
        void onSuccess(String reservationId);
        void onFailure(String error);
    }

    public interface ReservationsCallback {
        void onSuccess(List<Map<String, Object>> reservations);
        void onFailure(String error);
    }

    public ReservationService() {
        try {
            this.db = FirebaseFirestore.getInstance();
            this.auth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ReservationService(Context context) {
        this.context = context;
        try {
            this.db = FirebaseFirestore.getInstance();
            this.auth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ReservationService(FirebaseFirestore db, FirebaseAuth mockAuth) {
        this.db = db;
        this.auth = mockAuth;
    }

    public boolean createReservation(User user, Event event) {
        if (event.getAvailableSeats() > 0 && !event.getIsCancelled()) {
            event.setAvailableSeats(event.getAvailableSeats() - 1);
            return true;
        }
        return false;
    }

    public void createReservation(String eventId, int numberOfTickets, ReservationCallback callback) {
        if (auth == null || auth.getCurrentUser() == null) {
            callback.onFailure("User not logged in");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DocumentReference eventRef = db.collection("events").document(eventId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snap = transaction.get(eventRef);
            if (!snap.exists()) {
                throw new FirebaseFirestoreException("Event not found", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            Boolean isCancelled = snap.getBoolean("isCancelled");
            if (isCancelled != null && isCancelled) {
                throw new FirebaseFirestoreException("Event has been cancelled", FirebaseFirestoreException.Code.ABORTED);
            }

            Long seats = snap.getLong("availableSeats");
            if (seats == null || seats < numberOfTickets) {
                throw new FirebaseFirestoreException("Not enough seats available", FirebaseFirestoreException.Code.ABORTED);
            }

            String eventTitle = snap.getString("title");
            String eventDate = snap.getString("date");

            transaction.update(eventRef, "availableSeats", seats - numberOfTickets);

            Map<String, Object> res = new HashMap<>();
            res.put("userId", userId);
            res.put("eventId", eventId);
            res.put("eventName", eventTitle);
            res.put("eventDate", eventDate);
            res.put("numberOfTickets", numberOfTickets);
            res.put("reservationDate", FieldValue.serverTimestamp());
            res.put("status", "Active");

            DocumentReference resRef = db.collection("reservations").document();
            transaction.set(resRef, res);

            return resRef.getId();
        }).addOnSuccessListener(callback::onSuccess)
        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void cancelReservation(String reservationId, ReservationCallback callback) {
        if (db == null) {
             callback.onFailure("DB not initialized");
             return;
        }
        DocumentReference resRef = db.collection("reservations").document(reservationId);

        db.runTransaction(transaction -> {
            DocumentSnapshot resSnap = transaction.get(resRef);
            if (!resSnap.exists()) {
                throw new FirebaseFirestoreException("Reservation not found", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            String status = resSnap.getString("status");
            if ("Cancelled".equals(status)) {
                throw new FirebaseFirestoreException("Reservation is already cancelled", FirebaseFirestoreException.Code.ABORTED);
            }

            String eventId = resSnap.getString("eventId");
            Long tickets = resSnap.getLong("numberOfTickets");
            final long finalTickets = tickets != null ? tickets : 0;

            transaction.update(resRef, "status", "Cancelled");

            if (eventId != null && finalTickets > 0) {
                DocumentReference eventRef = db.collection("events").document(eventId);
                transaction.update(eventRef, "availableSeats", FieldValue.increment(finalTickets));
            }

            return null;
        }).addOnSuccessListener(unused -> callback.onSuccess("Reservation cancelled successfully"))
        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getUserReservations(ReservationsCallback callback) {
        if (auth == null || auth.getCurrentUser() == null) {
            callback.onFailure("User not logged in");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("reservations")
                .whereEqualTo("userId", userId)
                .orderBy("reservationDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> reservations = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> reservation = doc.getData();
                        if (reservation != null) {
                            reservation.put("id", doc.getId());
                            reservations.add(reservation);
                        }
                    }
                    callback.onSuccess(reservations);
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to get reservations: " + e.getMessage()));
    }
}
