package com.example.cloudticketreservationwk.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.cloudticketreservationwk.model.Event;
import com.example.cloudticketreservationwk.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReservationServiceTest {

    private ReservationService reservationService;
    private User testUser;
    private Event testEvent;

    @BeforeEach
    public void setUp() {
        reservationService = new ReservationService(null, null);
        testUser = new User("user123", "test@example.com");
        
        testEvent = new Event("Test Event", "2026-03-10", "Test Location", "Test Category", 10, 10);
        testEvent.setId("event123");
    }

    @Test
    public void testCreateReservation_Success() {
        int initialSeats = testEvent.getAvailableSeats();
        boolean result = reservationService.createReservation(testUser, testEvent);
        
        assertTrue(result, "Reservation should be successful");
        assertEquals(initialSeats - 1, testEvent.getAvailableSeats(), "Available seats should decrement by 1");
    }

    @Test
    public void testCreateReservation_NoSeats() {
        testEvent.setAvailableSeats(0);
        boolean result = reservationService.createReservation(testUser, testEvent);
        
        assertFalse(result, "Reservation should fail when no seats available");
        assertEquals(0, testEvent.getAvailableSeats(), "Available seats should remain 0");
    }

    @Test
    public void testCreateReservation_EventCancelled() {
        testEvent.setCancelled(true);
        boolean result = reservationService.createReservation(testUser, testEvent);
        
        assertFalse(result, "Reservation should fail when event is cancelled");
        assertEquals(10, testEvent.getAvailableSeats(), "Available seats should remain unchanged");
    }

    @Test
    public void testCreateReservation_MultipleTickets() {
        reservationService.createReservation(testUser, testEvent);
        reservationService.createReservation(testUser, testEvent);
        
        assertEquals(8, testEvent.getAvailableSeats(), "Available seats should decrement by 2 after two reservations");
    }
}
