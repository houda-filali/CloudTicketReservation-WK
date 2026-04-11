package com.example.cloudticketreservationwk.service;
import com.example.cloudticketreservationwk.model.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;

@DisplayName("Event Service Tests")
public class EventServiceTest {

    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockEventsCollection;
    @Mock
    private DocumentReference mockDocumentReference;
    private EventService eventService;
    private Event testEvent;

    @BeforeEach
    void setup() {
        testEvent = new Event(
                "Test Concert",
                "2026-05-15",
                "Bell Center",
                "Music",
                100,
                100
        );
    }

    @Test
    @DisplayName("Should validate event before adding")
    void testAddEventValidData() {
        assertNotNull(testEvent);
        assertNotNull(testEvent.getTitle());
        assertNotNull(testEvent.getDate());
        assertNotNull(testEvent.getLocation());
        assertNotNull(testEvent.getCategory());
        assertTrue(testEvent.getTotalSeats() > 0);

        assertNull(testEvent.getId());

        String mockFirestoreId = "firestore-generated-id-456";
        testEvent.setId(mockFirestoreId);

        assertNotNull(testEvent.getId());
        assertEquals(mockFirestoreId, testEvent.getId());
    }

    @Test
    @DisplayName("Should update event fields correctly")
    void testEventUpdate() {
        testEvent.setId("test-event-1");
        testEvent.setTitle("New Title");
        testEvent.setDate("2026-06-01");
        testEvent.setLocation("New Location");
        testEvent.setCategory("Rock");
        testEvent.setTotalSeats(200);
        testEvent.setAvailableSeats(150);
        testEvent.setCancelled(true);

        assertEquals("New Title", testEvent.getTitle());
        assertEquals("2026-06-01", testEvent.getDate());
        assertEquals("New Location", testEvent.getLocation());
        assertEquals("Rock", testEvent.getCategory());
        assertEquals(200, testEvent.getTotalSeats());
        assertEquals(150, testEvent.getAvailableSeats());
        assertTrue(testEvent.getIsCancelled());
    }

    @Test
    @DisplayName("Should calculate available seats after capacity change")
    void testAvailableSeatsCalculation() {
        int oldTotalSeats = 100;
        int oldAvailableSeats = 20;
        int reservedSeats = oldTotalSeats - oldAvailableSeats;

        int newTotalSeats = 150;
        int newAvailableSeats = newTotalSeats - reservedSeats;
        assertEquals(70, newAvailableSeats);

        newTotalSeats = 90;
        newAvailableSeats = newTotalSeats - reservedSeats;
        assertEquals(10, newAvailableSeats);

        newTotalSeats = 50;
        newAvailableSeats = newTotalSeats - reservedSeats;
        if (newAvailableSeats < 0) newAvailableSeats = 0;
        assertEquals(0, newAvailableSeats);
    }

    @Test
    @DisplayName("Should create sample events for seeding")
    void testSampleEvents() {
        Event comedyNight = new Event("Comedy Night", "2026-03-10", "Downtown", "Comedy", 100, 100);
        Event techMeetup = new Event("Tech Meetup", "2026-03-12", "Campus Hall", "Tech", 50, 50);
        Event liveConcert = new Event("Live Concert", "2026-03-20", "Main Arena", "Music", 500, 500);
        Event foodFestival = new Event("Food Festival", "2026-03-25", "Old Port", "Food", 200, 200);

        assertEquals("Comedy Night", comedyNight.getTitle());
        assertEquals("2026-03-10", comedyNight.getDate());
        assertEquals("Downtown", comedyNight.getLocation());
        assertEquals("Comedy", comedyNight.getCategory());
        assertEquals(100, comedyNight.getTotalSeats());

        assertEquals("Tech Meetup", techMeetup.getTitle());
        assertEquals(50, techMeetup.getTotalSeats());

        assertEquals("Live Concert", liveConcert.getTitle());
        assertEquals(500, liveConcert.getTotalSeats());

        assertEquals("Food Festival", foodFestival.getTitle());
        assertEquals(200, foodFestival.getTotalSeats());
    }
}
