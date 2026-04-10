package com.example.cloudticketreservationwk.integration;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.controller.EventDetailsActivity;

import org.junit.jupiter.api.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class EventDetailsIntegrationTest {

    private Intent createIntent() {
        // Create the intent used to open EventDetailsActivity with test data
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", "event-1");
        intent.putExtra("TITLE", "Comedy Night");
        intent.putExtra("DATE", "2026-03-10");
        intent.putExtra("LOCATION", "Downtown");
        intent.putExtra("CATEGORY", "Comedy");
        intent.putExtra("DESCRIPTION", "A fun comedy show.");
        return intent;
    }

    @Test
    void eventDetails_displaysPassedEventData() {
        // Open the event details screen with sample event data
        ActivityScenario.launch(createIntent());

        // Check that all event details are shown correctly
        onView(withId(R.id.tvDetailsTitle)).check(matches(withText("Comedy Night")));
        onView(withId(R.id.tvDetailsDate)).check(matches(withText("2026-03-10")));
        onView(withId(R.id.tvDetailsLocation)).check(matches(withText("Downtown")));
        onView(withId(R.id.tvDetailsCategory)).check(matches(withText("Comedy")));
        onView(withId(R.id.tvDetailsDescription)).check(matches(withText("A fun comedy show.")));
    }

    @Test
    void eventDetails_clickReserveWithEmptyTickets_showsError() {
        // Open the event details screen
        ActivityScenario.launch(createIntent());

        // Click reserve without entering any ticket number
        onView(withId(R.id.btnReserve)).perform(click());

        // Check that the correct error message is shown
        onView(withId(R.id.etTickets)).check(matches(hasErrorText("Enter number of tickets")));
    }

    @Test
    void eventDetails_clickReserveWithInvalidNumber_showsError() {
        // Open the event details screen
        ActivityScenario.launch(createIntent());

        // Enter text instead of a valid number and click reserve
        onView(withId(R.id.etTickets)).perform(replaceText("abc"));
        onView(withId(R.id.btnReserve)).perform(click());

        // Check that the correct error message is shown
        onView(withId(R.id.etTickets)).check(matches(hasErrorText("Enter a valid number")));
    }

    @Test
    void eventDetails_clickReserveWithZeroTickets_showsError() {
        // Open the event details screen
        ActivityScenario.launch(createIntent());

        // Enter 0 tickets and click reserve
        onView(withId(R.id.etTickets)).perform(replaceText("0"));
        onView(withId(R.id.btnReserve)).perform(click());

        // Check that the correct error message is shown
        onView(withId(R.id.etTickets)).check(matches(hasErrorText("Enter at least 1 ticket")));
    }
}