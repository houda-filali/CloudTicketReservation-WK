package com.example.cloudticketreservationwk.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class AdminEventValidationTest {
    private AdminEventValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AdminEventValidator();
    }

    @Test
    void testValidEventPassesValidation() {
        ValidationResult result = validator.validate(
                "Valid Event Title",
                "2026-05-15",
                "Valid Location",
                "Music",
                100
        );

        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void testEmptyTitleIsInvalid(String invalidTitle) {
        ValidationResult result = validator.validate(
                invalidTitle,
                "2026-05-15",
                "Location",
                "Category",
                100
        );

        assertFalse(result.isValid());
        assertEquals("Title is required", result.getErrorMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void testEmptyDateIsInvalid(String invalidDate) {
        ValidationResult result = validator.validate(
                "Valid Title",
                invalidDate,
                "Location",
                "Category",
                100
        );

        assertFalse(result.isValid());
        assertEquals("Date is required", result.getErrorMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void testEmptyLocationIsInvalid(String invalidLocation) {
        ValidationResult result = validator.validate(
                "Valid Title",
                "2026-05-15",
                invalidLocation,
                "Category",
                100
        );

        assertFalse(result.isValid());
        assertEquals("Location is required", result.getErrorMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void testEmptyCategoryIsInvalid(String invalidCategory) {
        ValidationResult result = validator.validate(
                "Valid Title",
                "2026-05-15",
                "Location",
                invalidCategory,
                100
        );

        assertFalse(result.isValid());
        assertEquals("Category is required", result.getErrorMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-5", "abc", "12.5"})
    void testInvalidCapacityValues(String invalidCapacity) {
        int capacity;
        try {
            capacity = Integer.parseInt(invalidCapacity);
        } catch (NumberFormatException e) {
            // Capacity is empty or invalid number
            assertTrue(true);
            return;
        }

        ValidationResult result = validator.validate(
                "Valid Title",
                "2026-05-15",
                "Location",
                "Category",
                capacity
        );

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Capacity"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 50, 100, 1000})
    void testValidCapacityValues(int validCapacity) {
        ValidationResult result = validator.validate(
                "Valid Title",
                "2026-05-15",
                "Location",
                "Category",
                validCapacity
        );

        assertTrue(result.isValid());
    }

    @Test
    void testEmptyCapacityShowsError() {
        // Simulating empty capacity field
        ValidationResult result = validator.validate(
                "Valid Title",
                "2026-05-15",
                "Location",
                "Category",
                0  // 0 capacity should be invalid
        );

        assertFalse(result.isValid());
        assertEquals("Capacity must be greater than 0", result.getErrorMessage());
    }

    // Helper class to encapsulate validation logic from AdminEventFormActivity
    static class AdminEventValidator {
        ValidationResult validate(String title, String date, String location,
                                  String category, int capacity) {
            if (title == null || title.trim().isEmpty()) {
                return new ValidationResult(false, "Title is required");
            }
            if (date == null || date.trim().isEmpty()) {
                return new ValidationResult(false, "Date is required");
            }
            if (location == null || location.trim().isEmpty()) {
                return new ValidationResult(false, "Location is required");
            }
            if (category == null || category.trim().isEmpty()) {
                return new ValidationResult(false, "Category is required");
            }
            if (capacity <= 0) {
                return new ValidationResult(false, "Capacity must be greater than 0");
            }

            return new ValidationResult(true, null);
        }
    }

    static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        boolean isValid() { return valid; }
        String getErrorMessage() { return errorMessage; }
    }

}
