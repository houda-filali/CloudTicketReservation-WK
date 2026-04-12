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
import com.google.firebase.FirebaseOptions;
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
import java.util.concurrent.atomic.AtomicReference;

public class ReservationEdgeCasesIntegrationTest {

    private FirebaseUser createdUser1;
    private FirebaseUser createdUser2;

    private String setupReservationId;
    private String createdReservationId1;
    private String createdReservationId2;

    @BeforeEach
    void setUp() {
        // Make sure Firebase is initialized before each test
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx);
        }

        // Start each test signed out
        FirebaseAuth.getInstance().signOut();
    }

    @AfterEach
    void tearDown() {
        // Cancel reservations created by the test so seats are restored
        try {
            if (createdUser1 != null) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                        createdUser1.getEmail(),
                        "test1234"
                ).addOnSuccessListener(result -> {});
                Thread.sleep(1000);
                ReservationService service = new ReservationService();
                if (createdReservationId1 != null) {
                    cancelReservationBlocking(service, createdReservationId1);
                }
                if (setupReservationId != null) {
                    cancelReservationBlocking(service, setupReservationId);
                }
            }
        } catch (Exception ignored) {}

        // Cancel the second user's reservation if one was created
        try {
            if (createdUser2 != null) {
                Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
                FirebaseApp secondApp = null;
                for (FirebaseApp app : FirebaseApp.getApps(ctx)) {
                    if (!app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                        secondApp = app;
                        break;
                    }
                }

                if (secondApp != null) {
                    FirebaseAuth auth2 = FirebaseAuth.getInstance(secondApp);
                    auth2.signInWithEmailAndPassword(createdUser2.getEmail(), "test1234")
                            .addOnSuccessListener(result -> {});
                    Thread.sleep(1000);

                    ReservationService service2 = new ReservationService(
                            FirebaseFirestore.getInstance(secondApp),
                            auth2
                    );

                    if (createdReservationId2 != null) {
                        cancelReservationBlocking(service2, createdReservationId2);
                    }

                    auth2.signOut();
                    secondApp.delete();
                }
            }
        } catch (Exception ignored) {}

        // Delete Firestore user profiles
        try {
            if (createdUser1 != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(createdUser1.getUid())
                        .delete();
            }
        } catch (Exception ignored) {}

        try {
            if (createdUser2 != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(createdUser2.getUid())
                        .delete();
            }
        } catch (Exception ignored) {}

        // Delete Auth users
        try {
            if (createdUser1 != null) createdUser1.delete();
        } catch (Exception ignored) {}

        try {
            if (createdUser2 != null) createdUser2.delete();
        } catch (Exception ignored) {}

        // Sign out after the test
        FirebaseAuth.getInstance().signOut();
    }

    @Test
    void reserveLastAvailableSeat() throws Exception {
        // Create a test customer
        createdUser1 = registerBlocking("lastseat" + randomSuffix() + "@example.com", "test1234");
        assertNotNull(createdUser1);

        // Find an existing active event with at least 1 seat
        Event event = findReservableEventBlocking(1);
        assertNotNull(event, "No active event with available seats found in Firestore");

        // Save the current seat count
        long seatsBefore = getAvailableSeatsBlocking(event.getId());

        ReservationService reservationService = new ReservationService();

        // Reserve all remaining seats so the event becomes full
        createdReservationId1 = createReservationBlocking(reservationService, event.getId(), (int) seatsBefore);
        assertNotNull(createdReservationId1);

        // Check that the event now has 0 seats left
        long seatsAfter = getAvailableSeatsBlocking(event.getId());
        assertEquals(0, seatsAfter);
    }

    @Test
    void reserveWhenEventIsFull() throws Exception {
        // Create a test customer
        createdUser1 = registerBlocking("full_" + randomSuffix() + "@example.com", "test1234");
        assertNotNull(createdUser1);

        // Find an existing active event with at least 1 seat
        Event event = findReservableEventBlocking(1);
        assertNotNull(event, "No active event with available seats found in Firestore");

        // Save the current seat count
        long seatsBefore = getAvailableSeatsBlocking(event.getId());

        ReservationService reservationService = new ReservationService();

        // Reserve all remaining seats to make the event full
        createdReservationId1 = createReservationBlocking(reservationService, event.getId(), (int) seatsBefore);
        assertNotNull(createdReservationId1);

        // Try reserving one more seat and capture the error
        String error = createReservationExpectFailureBlocking(reservationService, event.getId(), 1);

        assertEquals("Not enough seats available", error);
    }

    @Test
    void twoUsersReserveSameLastSeat() throws Exception {
        // Create first test customer
        createdUser1 = registerBlocking("user1" + randomSuffix() + "@example.com", "test1234");
        assertNotNull(createdUser1);

        // Find an existing active event with at least 2 seats
        Event event = findReservableEventBlocking(2);
        assertNotNull(event, "No active event with at least 2 seats found in Firestore");

        // Save the current seat count
        long seatsBefore = getAvailableSeatsBlocking(event.getId());

        // Reserve seats first so only 1 seat remains
        ReservationService setupService = new ReservationService();
        setupReservationId = createReservationBlocking(setupService, event.getId(), (int) seatsBefore - 1);
        assertNotNull(setupReservationId);

        // Create second Firebase app for a second real logged in user
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        FirebaseOptions options = FirebaseApp.getInstance().getOptions();
        FirebaseApp secondApp = FirebaseApp.initializeApp(ctx, options, "secondApp" + randomSuffix());

        FirebaseAuth auth1 = FirebaseAuth.getInstance();
        FirebaseFirestore db1 = FirebaseFirestore.getInstance();

        FirebaseAuth auth2 = FirebaseAuth.getInstance(secondApp);
        FirebaseFirestore db2 = FirebaseFirestore.getInstance(secondApp);

        // Create second test customer
        createdUser2 = registerBlocking(auth2, "user2" + randomSuffix() + "@example.com", "test1234");
        assertNotNull(createdUser2);

        ReservationService service1 = new ReservationService(db1, auth1);
        ReservationService service2 = new ReservationService(db2, auth2);

        AtomicReference<String> success1 = new AtomicReference<>();
        AtomicReference<String> success2 = new AtomicReference<>();
        AtomicReference<String> error1 = new AtomicReference<>();
        AtomicReference<String> error2 = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(2);

        // Two users try to reserve the same last seat at the same time
        new Thread(() -> {
            service1.createReservation(event.getId(), 1, new ReservationService.ReservationCallback() {
                @Override
                public void onSuccess(String reservationId) {
                    createdReservationId1 = reservationId;
                    success1.set(reservationId);
                    latch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    error1.set(error);
                    latch.countDown();
                }
            });
        }).start();

        new Thread(() -> {
            service2.createReservation(event.getId(), 1, new ReservationService.ReservationCallback() {
                @Override
                public void onSuccess(String reservationId) {
                    createdReservationId2 = reservationId;
                    success2.set(reservationId);
                    latch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    error2.set(error);
                    latch.countDown();
                }
            });
        }).start();

        // Wait for both reservation attempts to finish
        assertTrue(latch.await(60, TimeUnit.SECONDS));

        int successCount = 0;
        if (success1.get() != null) successCount++;
        if (success2.get() != null) successCount++;

        int failureCount = 0;
        if (error1.get() != null) failureCount++;
        if (error2.get() != null) failureCount++;

        // Only one user should get the last seat
        assertEquals(1, successCount);
        assertEquals(1, failureCount);

        // Check that the event is now full
        long seatsAfter = getAvailableSeatsBlocking(event.getId());
        assertEquals(0, seatsAfter);
    }

    private FirebaseUser registerBlocking(String email, String pass) throws Exception {
        // Wait for async registration to finish
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

    private FirebaseUser registerBlocking(FirebaseAuth auth, String email, String pass) throws Exception {
        // Wait for async registration on the second auth instance
        CountDownLatch latch = new CountDownLatch(1);
        final FirebaseUser[] out = new FirebaseUser[1];
        final Exception[] err = new Exception[1];

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    out[0] = result.getUser();
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

    private Event findReservableEventBlocking(int requiredTickets) throws Exception {
        // Find the first active event with enough seats
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
        // Create a reservation and wait for the callback
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

    private String createReservationExpectFailureBlocking(ReservationService service, String eventId, int tickets) throws Exception {
        // Try creating a reservation and return the failure message
        CountDownLatch latch = new CountDownLatch(1);
        final String[] out = new String[1];

        service.createReservation(eventId, tickets, new ReservationService.ReservationCallback() {
            @Override
            public void onSuccess(String reservationId) {
                out[0] = null;
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                out[0] = error;
                latch.countDown();
            }
        });

        assertTrue(latch.await(60, TimeUnit.SECONDS));
        return out[0];
    }

    private void cancelReservationBlocking(ReservationService service, String reservationId) throws Exception {
        // Cancel a reservation and wait for the callback
        CountDownLatch latch = new CountDownLatch(1);
        final Exception[] err = new Exception[1];

        service.cancelReservation(reservationId, new ReservationService.ReservationCallback() {
            @Override
            public void onSuccess(String message) {
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
    }

    private long getAvailableSeatsBlocking(String eventId) throws Exception {
        // Read the current seat count for an event
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

    private String randomSuffix() {
        // Generate a small random suffix for unique test emails/app names
        return UUID.randomUUID().toString().substring(0, 8);
    }
}