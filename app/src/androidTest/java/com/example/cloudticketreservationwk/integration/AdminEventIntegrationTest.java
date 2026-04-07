package com.example.cloudticketreservationwk.integration;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.cloudticketreservationwk.model.Event;
import com.example.cloudticketreservationwk.service.EventService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Admin Event Integration Tests")
class AdminEventIntegrationTest {
    private FirebaseFirestore firestore;
    private EventService eventService;
    private Context context;
    private List<String> createdEventIds;
    final String[] errorMessage = {null};

    @BeforeEach
    void setUp() {
        context = ApplicationProvider.getApplicationContext();

        for (FirebaseApp app : FirebaseApp.getApps(context)) {
            app.delete();
        }
        FirebaseApp.initializeApp(context);
        firestore = FirebaseFirestore.getInstance();
        firestore.useEmulator("10.0.2.2", 8080);

        eventService = new EventService(context, firestore);
        createdEventIds = new ArrayList<>();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(createdEventIds.size());

        for (String eventId : createdEventIds) {
            firestore.collection("events").document(eventId)
                    .delete()
                    .addOnSuccessListener(aVoid -> latch.countDown())
                    .addOnFailureListener(e -> latch.countDown());
        }

        latch.await(10, TimeUnit.SECONDS);
    }

    // Create event tests
    @Nested
    @DisplayName("Create Event Tests")
    class CreateEventTests {

        @Test
        @DisplayName("Should create event successfully")
        void testCreateEvent_Success() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] success = {false};
            final String[] createdEventId = {null};

            Event event = new Event(
                    "Integration Test Concert",
                    "2026-12-31",
                    "Test Arena",
                    "Music",
                    500,
                    500
            );

            eventService.addEvent(event, new EventService.EventCallback() {
                @Override
                public void onSuccess(String message) {
                    success[0] = true;
                    createdEventId[0] = event.getId();
                    createdEventIds.add(event.getId());
                    latch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(10, TimeUnit.SECONDS), "Event creation timed out");
            assertTrue(success[0], "Event should be created successfully");
            assertNotNull(createdEventId[0], "Event ID should not be null");

            CountDownLatch verifyLatch = new CountDownLatch(1);
            final boolean[] eventExists = {false};

            firestore.collection("events").document(createdEventId[0])
                    .get()
                    .addOnSuccessListener(doc -> {
                        eventExists[0] = doc.exists();
                        if (doc.exists()) {
                            assertEquals("Integration Test Concert", doc.getString("title"));
                            assertEquals("Test Arena", doc.getString("location"));
                            assertEquals(500, doc.getLong("totalSeats").intValue());
                        }
                        verifyLatch.countDown();
                    });

            assertTrue(verifyLatch.await(5, TimeUnit.SECONDS));
            assertTrue(eventExists[0], "Event should exist in Firestore");
        }

        @Test
        @DisplayName("Should create multiple events successfully")
        void testCreateEvent_MultipleEvents() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(3);
            final int[] successCount = {0};

            String[] titles = {"Event 1", "Event 2", "Event 3"};

            for (String title : titles) {
                Event event = new Event(title, "2026-12-31", "Location", "Category", 100, 100);

                eventService.addEvent(event, new EventService.EventCallback() {
                    @Override
                    public void onSuccess(String message) {
                        successCount[0]++;
                        createdEventIds.add(event.getId());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(String error) {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(15, TimeUnit.SECONDS));
            assertEquals(3, successCount[0], "All 3 events should be created");
        }

        @Test
        @DisplayName("Should create events with same title (different IDs)")
        void testCreateEvent_WithSameTitle() throws InterruptedException {
            String sameTitle = "Duplicate Title Event";

            String eventId1 = createTestEvent(sameTitle, 100);

            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] secondCreated = {false};
            final String[] eventId2 = {null};

            Event event2 = new Event(sameTitle, "2026-12-31", "Different Location", "Different Category", 200, 200);

            eventService.addEvent(event2, new EventService.EventCallback() {
                @Override
                public void onSuccess(String message) {
                    secondCreated[0] = true;
                    eventId2[0] = event2.getId();
                    createdEventIds.add(event2.getId());
                    latch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertTrue(secondCreated[0], "Second event with same title should be allowed");
            assertNotEquals(eventId1, eventId2[0], "Event IDs should be different");
        }
    }

    // Edit events tests
    @Nested
    @DisplayName("Edit Event Tests")
    class EditEventTests {

        @Test
        @DisplayName("Should update event title and capacity")
        void testEditEvent_UpdateTitleAndCapacity() throws InterruptedException {
            String eventId = createTestEvent("Original Title", 100);

            if (eventId == null) {
                fail("Failed to create test event");
                return;
            }

            CountDownLatch editLatch = new CountDownLatch(1);
            final boolean[] editSuccess = {false};

            Event updatedEvent = new Event(
                    "Updated Title",
                    "2026-12-31",
                    "Original Location",
                    "Original Category",
                    150,
                    200
            );
            updatedEvent.setId(eventId);

            eventService.editEvent(updatedEvent, new EventService.EventCallback() {
                @Override
                public void onSuccess(String message) {
                    editSuccess[0] = true;
                    editLatch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    editLatch.countDown();
                }
            });

            assertTrue(editLatch.await(10, TimeUnit.SECONDS));
            assertTrue(editSuccess[0], "Event should be updated successfully");

            CountDownLatch verifyLatch = new CountDownLatch(1);
            final String[] title = {""};
            final Long[] totalSeats = {0L};

            firestore.collection("events").document(eventId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        title[0] = doc.getString("title");
                        totalSeats[0] = doc.getLong("totalSeats");
                        verifyLatch.countDown();
                    });

            assertTrue(verifyLatch.await(5, TimeUnit.SECONDS));
            assertEquals("Updated Title", title[0]);
            assertEquals(200, totalSeats[0].intValue());
        }

        @Test
        @DisplayName("Should update available seats")
        void testEditEvent_UpdateAvailableSeats() throws InterruptedException {
            String eventId = createTestEvent("Seat Test Event", 100);

            Map<String, Object> updates = new HashMap<>();
            updates.put("availableSeats", 75);

            CountDownLatch editLatch = new CountDownLatch(1);

            eventService.editEvent(eventId, updates, new EventService.EventCallback() {
                @Override
                public void onSuccess(String message) {
                    editLatch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    editLatch.countDown();
                }
            });

            assertTrue(editLatch.await(10, TimeUnit.SECONDS));

            CountDownLatch verifyLatch = new CountDownLatch(1);
            final Long[] availableSeats = {0L};

            firestore.collection("events").document(eventId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        availableSeats[0] = doc.getLong("availableSeats");
                        verifyLatch.countDown();
                    });

            assertTrue(verifyLatch.await(5, TimeUnit.SECONDS));
            assertEquals(75, availableSeats[0].intValue());
        }

        @Test
        @DisplayName("Should fail when editing non-existent event")
        void testEditEvent_NonExistentEvent() throws InterruptedException {
            String nonExistentId = "non-existent-id-12345";

            Map<String, Object> updates = new HashMap<>();
            updates.put("title", "New Title");

            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] failureCalled = {false};

            eventService.editEvent(nonExistentId, updates, new EventService.EventCallback() {
                @Override
                public void onSuccess(String message) {
                    latch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    failureCalled[0] = true;
                    latch.countDown();
                }
            });

            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertTrue(failureCalled[0], "Should fail when editing non-existent event");
        }
    }

    // Get all events tests

    @Nested
    @DisplayName("Get All Events Tests")
    class GetAllEventsTests {

        @Test
        @DisplayName("Should return all events")
        void testGetAllEvents_ReturnsEvents() throws InterruptedException {
            // Create test events
            createTestEvent("Get All Test 1", 100);
            createTestEvent("Get All Test 2", 200);
            createTestEvent("Get All Test 3", 300);

            Thread.sleep(2000);

            CountDownLatch latch = new CountDownLatch(1);
            final List<Event>[] retrievedEvents = new List[1];

            eventService.getAllEvents(new EventService.EventsCallback() {
                @Override
                public void onSuccess(List<Event> events) {
                    retrievedEvents[0] = events;
                    latch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertNotNull(retrievedEvents[0]);
            assertTrue(retrievedEvents[0].size() >= 3, "Should have at least 3 events");

            boolean found1 = false, found2 = false, found3 = false;
            for (Event event : retrievedEvents[0]) {
                if (event.getTitle().equals("Get All Test 1")) found1 = true;
                if (event.getTitle().equals("Get All Test 2")) found2 = true;
                if (event.getTitle().equals("Get All Test 3")) found3 = true;
            }

            assertTrue(found1 && found2 && found3, "All test events should be in the list");
        }

        @Test
        @DisplayName("Should return non-null list even when empty")
        void testGetAllEvents_WhenEmpty() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] callbackCalled = {false};

            eventService.getAllEvents(new EventService.EventsCallback() {
                @Override
                public void onSuccess(List<Event> events) {
                    callbackCalled[0] = true;
                    assertNotNull(events, "Events list should not be null");
                    latch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    callbackCalled[0] = true;
                    latch.countDown();
                }
            });

            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertTrue(callbackCalled[0], "Callback should be called");
        }
    }

    // Cancel and reactivate event tests
    @Nested
    @DisplayName("Cancel/Reactivate Event Tests")
    class CancelReactivateEventTests {

        @Test
        @DisplayName("Should cancel event successfully")
        void testCancelEvent_Success() throws InterruptedException {
            String eventId = createTestEvent("To Be Cancelled", 100);

            Map<String, Object> updates = new HashMap<>();
            updates.put("isCancelled", true);

            CountDownLatch cancelLatch = new CountDownLatch(1);

            eventService.editEvent(eventId, updates, new EventService.EventCallback() {
                @Override
                public void onSuccess(String message) {
                    cancelLatch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    cancelLatch.countDown();
                }
            });

            assertTrue(cancelLatch.await(10, TimeUnit.SECONDS));

            CountDownLatch verifyLatch = new CountDownLatch(1);
            final Boolean[] isCancelled = {false};

            firestore.collection("events").document(eventId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        isCancelled[0] = doc.getBoolean("isCancelled");
                        verifyLatch.countDown();
                    });

            assertTrue(verifyLatch.await(5, TimeUnit.SECONDS));
            assertTrue(isCancelled[0], "Event should be marked as cancelled");
        }

        @Test
        @DisplayName("Should reactivate event successfully")
        void testReactivateEvent_Success() throws InterruptedException {
            String eventId = createCancelledEvent("To Be Reactivated", 100);

            if (eventId == null) {
                fail("Failed to create cancelled event - check Firebase connection");
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("isCancelled", false);

            CountDownLatch reactivateLatch = new CountDownLatch(1);

            eventService.editEvent(eventId, updates, new EventService.EventCallback() {
                @Override
                public void onSuccess(String message) {
                    reactivateLatch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    reactivateLatch.countDown();
                }
            });

            assertTrue(reactivateLatch.await(10, TimeUnit.SECONDS));

            CountDownLatch verifyLatch = new CountDownLatch(1);
            final Boolean[] isCancelled = {true};

            firestore.collection("events").document(eventId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        isCancelled[0] = doc.getBoolean("isCancelled");
                        verifyLatch.countDown();
                    });

            assertTrue(verifyLatch.await(5, TimeUnit.SECONDS));
            assertFalse(isCancelled[0], "Event should no longer be cancelled");
        }
    }

    // Helper methods
    private String createTestEvent(String title, int capacity) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final String[] eventId = {null};

        Event event = new Event(title, "2026-12-31", "Test Location", "Test Category", capacity, capacity);

        eventService.addEvent(event, new EventService.EventCallback() {
            @Override
            public void onSuccess(String message) {
                eventId[0] = event.getId();
                createdEventIds.add(event.getId());
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                errorMessage[0] = error;
                System.err.println("Failed to create event '" + title + "': " + error);
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        return eventId[0];
    }

    private String createCancelledEvent(String title, int capacity) throws InterruptedException {
        String eventId = createTestEvent(title, capacity);

        if (eventId == null) {
            System.err.println("Failed to create event for cancellation");
            return null;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("isCancelled", true);

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};

        eventService.editEvent(eventId, updates, new EventService.EventCallback() {
            @Override
            public void onSuccess(String message) {
                success[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        return success[0] ? eventId : null;
    }
}
