package com.example.cloudticketreservationwk;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.cloudticketreservationwk.firebase.AuthService;
import com.example.cloudticketreservationwk.model.Event;
import com.example.cloudticketreservationwk.service.EventService;
import com.example.cloudticketreservationwk.service.ReservationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ReservationServiceIntegrationTest {

    private AuthService authService;
    private EventService eventService;
    private ReservationService reservationService;
    private FirebaseFirestore db;
    private String eventId;

    @Before
    public void setUp() throws Exception {
        authService = new AuthService();
        eventService = new EventService(InstrumentationRegistry.getInstrumentation().getTargetContext());
        reservationService = new ReservationService();
        db = FirebaseFirestore.getInstance();

        String email = "test_" + UUID.randomUUID().toString().substring(0, 8) + ".admin@test.com";
        register(email, "password123");
        eventId = createEvent();
        assertNotNull("eventId should not be null after setup. Check internet status.", eventId);
    }

    @After
    public void tearDown() {
        if (eventId != null) db.collection("events").document(eventId).delete();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.delete();
            FirebaseAuth.getInstance().signOut();
        }
    }

    @Test
    public void testReservationCycle() throws Exception {
        int startSeats = getSeats();
        assertTrue("Initial seats should be greater than 0", startSeats > 0);
        
        String resId = reserve(3);
        assertNotNull("Reservation ID should not be null", resId);
        assertEquals("Seats should decrement by 3", startSeats - 3, getSeats());

        cancel(resId);
        assertEquals("Seats should return to original count", startSeats, getSeats());
    }

    private void register(String email, String pass) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        authService.registerWithEmail(email, pass, new AuthService.UserCallback() {
            @Override
            public void onSuccess(FirebaseUser user) { latch.countDown(); }
            @Override
            public void onError(Exception e) { 
                e.printStackTrace();
                latch.countDown(); 
            }
        });
        assertTrue("Registration timed out", latch.await(20, TimeUnit.SECONDS));
    }

    private String createEvent() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final String[] id = new String[1];
        Event e = new Event("Test", "2025-01-01", "Loc", "Cat", 10, 10);
        eventService.addEvent(e, new EventService.EventCallback() {
            @Override
            public void onSuccess(String m) { 
                id[0] = e.getId(); 
                latch.countDown(); 
            }
            @Override
            public void onFailure(String err) { 
                System.err.println("Event creation failed: " + err);
                latch.countDown(); 
            }
        });
        assertTrue("Event creation timed out", latch.await(20, TimeUnit.SECONDS));
        return id[0];
    }

    private int getSeats() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final int[] s = new int[1];
        db.collection("events").document(eventId).get().addOnSuccessListener(d -> {
            Long val = d.getLong("availableSeats");
            s[0] = val != null ? val.intValue() : -1;
            latch.countDown();
        }).addOnFailureListener(e -> {
            e.printStackTrace();
            latch.countDown();
        });
        assertTrue("Fetching seats timed out", latch.await(20, TimeUnit.SECONDS));
        return s[0];
    }

    private String reserve(int n) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final String[] id = new String[1];
        reservationService.createReservation(eventId, n, new ReservationService.ReservationCallback() {
            @Override
            public void onSuccess(String r) { id[0] = r; latch.countDown(); }
            @Override
            public void onFailure(String e) { 
                System.err.println("Reservation failed: " + e);
                latch.countDown(); 
            }
        });
        assertTrue("Reservation timed out", latch.await(20, TimeUnit.SECONDS));
        return id[0];
    }

    private void cancel(String id) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        reservationService.cancelReservation(id, new ReservationService.ReservationCallback() {
            @Override
            public void onSuccess(String m) { latch.countDown(); }
            @Override
            public void onFailure(String e) { 
                System.err.println("Cancellation failed: " + e);
                latch.countDown(); 
            }
        });
        assertTrue("Cancellation timed out", latch.await(20, TimeUnit.SECONDS));
    }
}
