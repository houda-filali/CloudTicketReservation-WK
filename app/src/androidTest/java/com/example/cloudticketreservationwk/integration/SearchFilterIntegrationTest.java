package com.example.cloudticketreservationwk.integration;

import androidx.test.core.app.ActivityScenario;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.controller.EventAdapter;
import com.example.cloudticketreservationwk.controller.EventListActivity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class SearchFilterIntegrationTest {

    @SuppressWarnings("unchecked")
    @Test
    void search_byTitle_filtersDisplayedEvents() {
        // Launch the event list screen
        ActivityScenario<EventListActivity> scenario = ActivityScenario.launch(EventListActivity.class);

        // Inject fake events into the activity
        scenario.onActivity(activity -> {
            try {
                Field allEventsField = EventListActivity.class.getDeclaredField("allEvents");
                Field eventsField = EventListActivity.class.getDeclaredField("events");
                Field adapterField = EventListActivity.class.getDeclaredField("adapter");
                Field searchModeField = EventListActivity.class.getDeclaredField("searchMode");

                allEventsField.setAccessible(true);
                eventsField.setAccessible(true);
                adapterField.setAccessible(true);
                searchModeField.setAccessible(true);

                List<EventAdapter.EventItem> allEvents =
                        (List<EventAdapter.EventItem>) allEventsField.get(activity);
                List<EventAdapter.EventItem> events =
                        (List<EventAdapter.EventItem>) eventsField.get(activity);
                EventAdapter adapter = (EventAdapter) adapterField.get(activity);

                // Clear existing data
                allEvents.clear();
                events.clear();

                // Add test events
                allEvents.add(new EventAdapter.EventItem(
                        "event-1", "Comedy Night", "2026-03-10", "Downtown", "Comedy", "Funny show"
                ));
                allEvents.add(new EventAdapter.EventItem(
                        "event-2", "Tech Meetup", "2026-03-12", "Campus Hall", "Tech", "Tech talks"
                ));
                allEvents.add(new EventAdapter.EventItem(
                        "event-3", "Food Festival", "2026-03-20", "Old Port", "Food", "Food event"
                ));

                // Copy data into displayed list
                events.addAll(allEvents);

                // Set search mode (title search)
                searchModeField.setInt(activity, 0);

                // Refresh UI
                adapter.notifyDataSetChanged();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Type "Comedy" in search bar
        onView(withId(R.id.etSearch)).perform(clearText(), replaceText("Comedy"));

        // Check that only the matching event is shown
        onView(withText("Comedy Night")).check(matches(isDisplayed()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void search_noMatch_showsEmptyState() {
        // Launch the event list screen
        ActivityScenario<EventListActivity> scenario = ActivityScenario.launch(EventListActivity.class);

        // Load sample test data into the activity for search testing
        scenario.onActivity(activity -> {
            try {
                Field allEventsField = EventListActivity.class.getDeclaredField("allEvents");
                Field eventsField = EventListActivity.class.getDeclaredField("events");
                Field adapterField = EventListActivity.class.getDeclaredField("adapter");
                Field searchModeField = EventListActivity.class.getDeclaredField("searchMode");

                allEventsField.setAccessible(true);
                eventsField.setAccessible(true);
                adapterField.setAccessible(true);
                searchModeField.setAccessible(true);

                List<EventAdapter.EventItem> allEvents =
                        (List<EventAdapter.EventItem>) allEventsField.get(activity);
                List<EventAdapter.EventItem> events =
                        (List<EventAdapter.EventItem>) eventsField.get(activity);
                EventAdapter adapter = (EventAdapter) adapterField.get(activity);

                // Clear existing data
                allEvents.clear();
                events.clear();

                // Add test events
                allEvents.add(new EventAdapter.EventItem(
                        "event-1", "Comedy Night", "2026-03-10", "Downtown", "Comedy", "Funny show"
                ));
                allEvents.add(new EventAdapter.EventItem(
                        "event-2", "Tech Meetup", "2026-03-12", "Campus Hall", "Tech", "Tech talks"
                ));

                // Copy data into displayed list
                events.addAll(allEvents);

                // Set search mode
                searchModeField.setInt(activity, 0);

                // Refresh UI
                adapter.notifyDataSetChanged();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Type something that matches no events
        onView(withId(R.id.etSearch)).perform(clearText(), replaceText("zzzzzz"));

        // Check that empty state message is shown
        onView(withId(R.id.tvEmptyEvents)).check(matches(isDisplayed()));
    }
}