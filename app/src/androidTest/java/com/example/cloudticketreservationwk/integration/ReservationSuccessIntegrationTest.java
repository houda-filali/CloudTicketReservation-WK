package com.example.cloudticketreservationwk.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.example.cloudticketreservationwk.firebase.AuthService;
import com.example.cloudticketreservationwk.model.Event;
import com.example.cloudticketreservationwk.service.ReservationService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ReservationSuccessIntegrationTest {

    private FirebaseUser createdUser;
    private String createdReservationId;

    @BeforeEach
    void setUp() {
        // Make sure Firebase is initialized before each test
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx);
        }

        // Start each test with no logged in user
        FirebaseAuth.getInstance().signOut();
    }

    @AfterEach
    void tearDown() {
        // Delete the reservation created by the test
        try {
            if (createdReservationId != null) {
                FirebaseFirestore.getInstance()
                        .collection("reservations")
                        .document(createdReservationId)
                        .delete();
            }
        } catch (Exception ignored) {}

        // Delete the Firestore user profile created by the test
        try {
            if (createdUser != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(createdUser.getUid())
                        .delete();
            }
        } catch (Exception ignored) {}

        // Delete the Firebase Auth user created by the test
        try {
            if (createdUser != null) {
                createdUser.delete();
            }
        } catch (Exception ignored) {}

        // Sign out after the test
        FirebaseAuth.getInstance().signOut();
    }

    @Test
    void createReservation_success_createsReservationAndDecreasesSeats() throws Exception {
        // Create a fresh customer account for this test
        String email = "reserve" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        String pass = "1234";

        createdUser = registerBlocking(email, pass);
        assertNotNull(createdUser);

        // Find an existing event that is active and has at least 1 seat left
        Event event = findReservableEventBlocking(1);
        assertNotNull(event, "No active event with available seats found in Firestore");

        // Save the seat count before making the reservation
        long seatsBefore = getAvailableSeatsBlocking(event.getId());

        ReservationService reservationService = new ReservationService();

        // Create a reservation for 1 ticket
        createdReservationId = createReservationBlocking(reservationService, event.getId(), 1);
        assertNotNull(createdReservationId);

        // Check that one seat was taken
        long seatsAfter = getAvailableSeatsBlocking(event.getId());
        assertEquals(seatsBefore - 1, seatsAfter);

        // Check that the reservation was really created in Firestore
        boolean reservationExists = reservationExistsBlocking(createdReservationId);
        assertTrue(reservationExists);
    }

    private FirebaseUser registerBlocking(String email, String pass) throws Exception {
        // Wait for async registration to finish before continuing
        CountDownLatch latch = new CountDownLatch(1);
        final FirebaseUser[] out = new FirebaseUser[1];
        final Exception[] err = new Exception[1];

        new AuthService().registerWithEmail(email, pass, new AuthService.UserCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                out[0] = user;
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                err[0] = e;
                latch.countDown();
            }
        });

        assertTrue(latch.await(60, TimeUnit.SECONDS));
        if (err[0] != null) throw err[0];
        return out[0];
    }

    private Event findReservableEventBlocking(int requiredTickets) throws Exception {
        // Find the first event that is not cancelled and has enough seats
        CountDownLatch latch = new CountDownLatch(1);
        final Event[] out = new Event[1];
        final Exception[] err = new Exception[1];

        FirebaseFirestore.getInstance()
                .collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Event event = doc.toObject(Event.class);
                        event.setId(doc.getId());

                        if (!event.getIsCancelled() && event.getAvailableSeats() >= requiredTickets) {
                            out[0] = event;
                            break;
                        }
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    err[0] = e;
                    latch.countDown();
                });

        assertTrue(latch.await(60, TimeUnit.SECONDS));
        if (err[0] != null) throw err[0];
        return out[0];
    }

    private String createReservationBlocking(ReservationService service, String eventId, int tickets) throws Exception {
        // Create a reservation and wait for the async callback
        CountDownLatch latch = new CountDownLatch(1);
        final String[] out = new String[1];
        final Exception[] err = new Exception[1];

        service.createReservation(eventId, tickets, new ReservationService.ReservationCallback() {
            @Override
            public void onSuccess(String reservationId) {
                out[0] = reservationId;
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                err[0] = new Exception(error);
                latch.countDown();
            }
        });

        assertTrue(latch.await(60, TimeUnit.SECONDS));
        if (err[0] != null) throw err[0];
        return out[0];
    }

    private long getAvailableSeatsBlocking(String eventId) throws Exception {
        // Read the current number of available seats for an event
        CountDownLatch latch = new CountDownLatch(1);
        final long[] out = new long[1];
        final Exception[] err = new Exception[1];

        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    Long seats = doc.getLong("availableSeats");
                    out[0] = seats == null ? -1 : seats;
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    err[0] = e;
                    latch.countDown();
                });

        assertTrue(latch.await(60, TimeUnit.SECONDS));
        if (err[0] != null) throw err[0];
        return out[0];
    }

    private boolean reservationExistsBlocking(String reservationId) throws Exception {
        // Check if the reservation document exists in Firestore
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] out = new boolean[1];
        final Exception[] err = new Exception[1];

        FirebaseFirestore.getInstance()
                .collection("reservations")
                .document(reservationId)
                .get()
                .addOnSuccessListener(doc -> {
                    out[0] = doc.exists();
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    err[0] = e;
                    latch.countDown();
                });

        assertTrue(latch.await(60, TimeUnit.SECONDS));
        if (err[0] != null) throw err[0];
        return out[0];
    }
}