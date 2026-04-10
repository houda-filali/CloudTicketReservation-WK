package com.example.cloudticketreservationwk.integration;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.controller.EventListActivity;
import com.example.cloudticketreservationwk.controller.MyReservationsActivity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class CustomerFlowIntegrationTest {

    @BeforeEach
    void setUp() {
        Intents.init();
    }

    @AfterEach
    void tearDown() {
        Intents.release();
    }

    @Test
    void eventList_clickMyReservations_opensMyReservationsActivity() {
        ActivityScenario.launch(EventListActivity.class);

        onView(withId(R.id.btnMyReservations)).perform(click());

        intended(hasComponent(MyReservationsActivity.class.getName()));
    }
}