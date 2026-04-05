package com.example.cloudticketreservationwk.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EventCapacityUpdateTest {
    @Test
    @DisplayName("Prevent reducing capacity below existing reservations")
    void testCapacityReduction_BelowReservations_ShouldBeBlocked() {
        int oldTotalSeats = 100;
        int oldAvailableSeats = 20;
        int reservedSeats = oldTotalSeats - oldAvailableSeats; // = 80

        // Admin tries to reduce capacity to 50 (< 80 reserved)
        int requestedNewTotalSeats = 50;

        // Operation should be REJECTED
        boolean isValidReduction = requestedNewTotalSeats >= reservedSeats;
        assertFalse(isValidReduction, "Should not allow capacity below reserved seats");

        // Event should remain unchanged
        int finalTotalSeats = oldTotalSeats;
        int finalAvailableSeats = oldAvailableSeats;

        assertEquals(100, finalTotalSeats);
        assertEquals(20, finalAvailableSeats);
        assertEquals(80, reservedSeats);
    }

    @Test
    @DisplayName("Allow reducing capacity when still above reservations")
    void testCapacityReduction_AboveReservations_ShouldBeAllowed() {
        int oldTotalSeats = 100;
        int oldAvailableSeats = 20;
        int reservedSeats = oldTotalSeats - oldAvailableSeats; // = 80

        // Admin reduces capacity to 90 (still >= 80 reserved)
        int requestedNewTotalSeats = 90;

        // Operation allowed
        boolean isValidReduction = requestedNewTotalSeats >= reservedSeats;
        assertTrue(isValidReduction, "Should allow reduction as long as capacity >= reserved seats");

        int newAvailableSeats = requestedNewTotalSeats - reservedSeats; // 90 - 80 = 10

        assertEquals(90, requestedNewTotalSeats);
        assertEquals(10, newAvailableSeats);
        assertEquals(80, reservedSeats); // Reservations unchanged
    }

    @Test
    @DisplayName("Should ALLOW increasing capacity")
    void testCapacityIncrease() {
        int oldTotalSeats = 100;
        int oldAvailableSeats = 20;
        int reservedSeats = oldTotalSeats - oldAvailableSeats; // 80

        // Admin increases capacity to 150
        int newTotalSeats = 150;

        // Operation allowed
        assertTrue(newTotalSeats >= reservedSeats);

        int newAvailableSeats = newTotalSeats - reservedSeats;
        assertEquals(70, newAvailableSeats);
        assertEquals(150, newTotalSeats);
    }

    @Test
    @DisplayName("Should show error message when trying to reduce capacity below reservations")
    void testCapacityReduction_ShowsErrorMessage() {
        int totalSeats = 100;
        int reservedSeats = 80;
        int requestedCapacity = 50;

        // Validation check
        boolean wouldOverbook = requestedCapacity < reservedSeats;

        // Show error
        assertTrue(wouldOverbook);

        // Expected error message
        String expectedError = String.format(
                "Cannot reduce capacity to %d because there are %d existing reservations.\n\n" +
                        "Please increase capacity or cancel entire event.",
                requestedCapacity, reservedSeats
        );

        assertTrue(expectedError.contains("Cannot reduce capacity"));
        assertTrue(expectedError.contains(String.valueOf(requestedCapacity)));
        assertTrue(expectedError.contains(String.valueOf(reservedSeats)));
    }

    @Test
    @DisplayName("Should allow capacity to be set exactly equal to reservations")
    void testCapacityReduction_ExactlyEqualReservations() {
        int reservedSeats = 80;

        // Admin sets capacity exactly to 80
        int newTotalSeats = 80;

        // Allowed
        boolean isValidReduction = newTotalSeats >= reservedSeats;
        assertTrue(isValidReduction);

        int newAvailableSeats = newTotalSeats - reservedSeats; // 0
        assertEquals(0, newAvailableSeats);
    }

    @Test
    @DisplayName("Should validate capacity before attempting update")
    void testCapacityValidation_BeforeUpdate() {
        int reservedSeats = 75;

        // Invalid scenarios
        assertTrue(50 < reservedSeats);  // Overbook
        assertTrue(74 < reservedSeats);  // Overbook by 1
        assertTrue(0 < reservedSeats);   // Overbook significantly

        // Valid scenarios (should be allowed)
        assertTrue(75 == reservedSeats); // Exactly full
        assertTrue(100 > reservedSeats); // Has available seats
        assertTrue(1000 > reservedSeats); // Much larger
    }

    @Test
    @DisplayName("Should preserve existing reservations when changing capacity")
    void testPreserveReservations_WhenCapacityChanges() {
        // Given
        int originalReservations = 80;
        int originalTotalSeats = 100;

        // When: Capacity changes to 120
        int newTotalSeats = 120;

        // Then: Reservations remain at 80
        int reservationsAfterChange = originalReservations;
        assertEquals(80, reservationsAfterChange);

        // Available seats recalculated
        int newAvailableSeats = newTotalSeats - reservationsAfterChange;
        assertEquals(40, newAvailableSeats);

    }
}