package com.example.cloudticketreservationwk.integration;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.controller.EventListActivity;
import com.example.cloudticketreservationwk.controller.MyReservationsActivity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class CustomerFlowIntegrationTest {

    @BeforeEach
    void setUp() {
        Intents.init();
    }

    @AfterEach
    void tearDown() {
        // Release Espresso Intents after each test
        Intents.release();
    }

    @Test
    void eventList_launchesAndDisplaysMainCustomerViews() {
        // Open the customer event list screen
        ActivityScenario.launch(EventListActivity.class);

        // Check that the main customer screen elements are visible
        onView(withId(R.id.rvEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.etSearch)).check(matches(isDisplayed()));
        onView(withId(R.id.btnMyReservations)).check(matches(isDisplayed()));
    }

    @Test
    void eventList_clickMyReservations_opensMyReservationsActivity() {
        // Open the customer event list screen
        ActivityScenario.launch(EventListActivity.class);

        // Click the "My Reservations" button
        onView(withId(R.id.btnMyReservations)).perform(click());

        // Check that MyReservationsActivity was opened
        intended(hasComponent(MyReservationsActivity.class.getName()));
    }

}