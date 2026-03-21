package com.example.cloudticketreservationwk.service;

import android.content.Context;
import android.widget.Toast;

import com.example.cloudticketreservationwk.model.User;
import com.example.cloudticketreservationwk.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

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

    // Default constructor for backward compatibility
    public ReservationService() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    // Constructor with context for better user feedback
    public ReservationService(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    // Original method (kept for compatibility)
    public boolean createReservation(User user, Event event) {
        if (event.getAvailableSeats() > 0 && !event.getIsCancelled()) {
            event.setAvailableSeats(event.getAvailableSeats() - 1);
            return true;
        }
        return false;
    }

    // Enhanced method with Firestore integration
    public void createReservation(String eventId, int numberOfTickets, ReservationCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailure("User not logged in");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // First check if event exists and has available seats
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onFailure("Event not found");
                        return;
                    }

                    Event event = documentSnapshot.toObject(Event.class);
                    if (event == null) {
                        callback.onFailure("Invalid event data");
                        return;
                    }

                    if (event.getIsCancelled()) {
                        callback.onFailure("Event has been cancelled");
                        return;
                    }

                    if (event.getAvailableSeats() < numberOfTickets) {
                        callback.onFailure("Not enough available seats. Only " +
                                event.getAvailableSeats() + " seats available.");
                        return;
                    }

                    // Create reservation
                    Map<String, Object> reservation = new HashMap<>();
                    reservation.put("userId", userId);
                    reservation.put("eventId", eventId);
                    reservation.put("eventName", event.getTitle());
                    reservation.put("numberOfTickets", numberOfTickets);
                    reservation.put("reservationDate", FieldValue.serverTimestamp());
                    reservation.put("status", "Active");

                    // Make final copies for lambda
                    String finalEventId = eventId;
                    int finalNumberOfTickets = numberOfTickets;
                    int originalAvailableSeats = event.getAvailableSeats();

                    db.collection("reservations")
                            .add(reservation)
                            .addOnSuccessListener(documentReference -> {
                                // Update available seats
                                int newAvailableSeats = originalAvailableSeats - finalNumberOfTickets;
                                db.collection("events").document(finalEventId)
                                        .update("availableSeats", newAvailableSeats)
                                        .addOnSuccessListener(aVoid -> {
                                            callback.onSuccess(documentReference.getId());
                                        })
                                        .addOnFailureListener(e -> {
                                            // Rollback: Delete the reservation if seat update fails
                                            documentReference.delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        callback.onFailure("Failed to update seat count. Reservation cancelled.");
                                                    })
                                                    .addOnFailureListener(deleteError -> {
                                                        callback.onFailure("Critical error: Reservation created but seat count inconsistent. Please contact support.");
                                                    });
                                        });
                            })
                            .addOnFailureListener(e ->
                                    callback.onFailure("Failed to create reservation: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to fetch event: " + e.getMessage()));
    }

    // Cancel a reservation and return seats to the event
    public void cancelReservation(String reservationId, ReservationCallback callback) {
        db.collection("reservations").document(reservationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onFailure("Reservation not found");
                        return;
                    }

                    String eventId = documentSnapshot.getString("eventId");
                    Long ticketsLong = documentSnapshot.getLong("numberOfTickets");
                    int numberOfTickets = ticketsLong != null ? ticketsLong.intValue() : 0;

                    // Check if already cancelled
                    String status = documentSnapshot.getString("status");
                    if ("Cancelled".equals(status)) {
                        callback.onFailure("Reservation is already cancelled");
                        return;
                    }

                    // Make final copies for lambda
                    final String finalEventId = eventId;
                    final int finalNumberOfTickets = numberOfTickets;

                    // Update reservation status
                    documentSnapshot.getReference()
                            .update("status", "Cancelled")
                            .addOnSuccessListener(aVoid -> {
                                // Return seats to event
                                if (finalEventId != null && finalNumberOfTickets > 0) {
                                    db.collection("events").document(finalEventId)
                                            .update("availableSeats",
                                                    FieldValue.increment(finalNumberOfTickets))
                                            .addOnSuccessListener(aVoid2 -> {
                                                callback.onSuccess("Reservation cancelled successfully");
                                            })
                                            .addOnFailureListener(e -> {
                                                // Still return success for reservation cancellation,
                                                // but log the error
                                                callback.onSuccess("Reservation cancelled but seat count may be incorrect. Please contact support.");
                                            });
                                } else {
                                    callback.onSuccess("Reservation cancelled successfully");
                                }
                            })
                            .addOnFailureListener(e ->
                                    callback.onFailure("Failed to cancel reservation: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to fetch reservation: " + e.getMessage()));
    }

    // Get user's reservations
    public void getUserReservations(ReservationsCallback callback) {
        if (auth.getCurrentUser() == null) {
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