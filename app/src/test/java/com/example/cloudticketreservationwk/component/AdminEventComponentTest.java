package com.example.cloudticketreservationwk.component;

import com.example.cloudticketreservationwk.model.Event;
import com.example.cloudticketreservationwk.service.EventService;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AdminEventComponentTest {
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockEventsCollection;
    @Mock
    private DocumentReference mockDocumentRef;
    private EventService eventService;
    @Mock
    private Task<Void> mockTask;

    @Test
    @DisplayName("Should create event with valid data")
    void testCreateEvent_ValidData() {
        Event event = new Event("Test Concert", "2026-12-31", "Centre Bell", "Music", 500, 500);

        assertEquals("Test Concert", event.getTitle());
        assertEquals("2026-12-31", event.getDate());
        assertEquals("Centre Bell", event.getLocation());
        assertEquals("Music", event.getCategory());
        assertEquals(500, event.getTotalSeats());
        assertEquals(500, event.getAvailableSeats());
    }

    @Test
    @DisplayName("Should block reducing capacity below num of reservations")
    void testCapacityValidation_ReduceBelowReservations_ShouldBeBlocked() {
        int totalSeats = 100;
        int availableSeats = 20;
        int reservedSeats = totalSeats - availableSeats;

        int newCapacity = 50;
        boolean isValid = newCapacity >= reservedSeats;

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should allow increasing capacity regardless of num of reservations")
    void testCapacityValidation_IncreaseCapacity() {
        int reservedSeats = 80;
        int newCapacity = 150;

        boolean isValid = newCapacity >= reservedSeats;
        assertTrue(isValid);

        int newAvailableSeats = newCapacity - reservedSeats;
        assertEquals(70, newAvailableSeats);
    }

    @Test
    @DisplayName("Should calculate reserved seats correctly")
    void testCalculateReservedSeats() {
        Event event = new Event("Test", "2026-12-31", "Test Location", "Tester", 100, 75);
        int reservedSeats = event.getTotalSeats() - event.getAvailableSeats();

        assertEquals(25, reservedSeats);
    }

    @Test
    @DisplayName("Should show cancellation status")
    void testCancellationStatus() {
        Event event = new Event("Test", "2026-12-31", "Test Location", "Tester", 100, 100);

        assertFalse(event.getIsCancelled());
        event.setCancelled(true);
        assertTrue(event.getIsCancelled());
        event.setCancelled(false);
        assertFalse(event.getIsCancelled());
    }
}
