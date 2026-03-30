package com.example.cloudticketreservationwk.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class EventListActivitySearchFilterTest {

    private EventListActivity activity;
    private List<EventAdapter.EventItem> events;
    private List<EventAdapter.EventItem> allEvents;
    private View emptyView;

    @Before
    public void setUp() throws Exception {
        // Create test data
        activity = new EventListActivity();

        events = getPrivateList("events");
        allEvents = getPrivateList("allEvents");

        events.clear();
        allEvents.clear();

        allEvents.addAll(Arrays.asList(
                new EventAdapter.EventItem("1", "Comedy Night", "2026-03-10", "Downtown", "Comedy", ""),
                new EventAdapter.EventItem("2", "Tech Meetup", "2026-03-12", "Campus Hall", "Tech", ""),
                new EventAdapter.EventItem("3", "Food Festival", "2026-03-25", "Old Port", "Food", "")
        ));

        events.addAll(allEvents);

        setPrivateField("adapter", new EventAdapter(events, activity));

        // Fake empty state view
        emptyView = new View(RuntimeEnvironment.getApplication());
        emptyView.setVisibility(View.GONE);
        setPrivateField("tvEmpty", emptyView);
    }

    @Test
    public void filter_allMode_matchesTitle() throws Exception {
        setSearchMode(0);
        invokeFilter("comedy");

        assertDisplayedTitles("Comedy Night");
        assertEquals(View.GONE, emptyView.getVisibility());
    }

    @Test
    public void filter_allMode_matchesDate() throws Exception {
        setSearchMode(0);
        invokeFilter("2026-03-12");

        assertDisplayedTitles("Tech Meetup");
    }

    @Test
    public void filter_allMode_matchesLocation() throws Exception {
        setSearchMode(0);
        invokeFilter("old port");

        assertDisplayedTitles("Food Festival");
    }

    @Test
    public void filter_allMode_matchesCategory() throws Exception {
        setSearchMode(0);
        invokeFilter("food");

        assertDisplayedTitles("Food Festival");
    }

    @Test
    public void filter_allMode_returnsMultipleMatches() throws Exception {
        setSearchMode(0);
        invokeFilter("2026-03");

        assertDisplayedTitles("Comedy Night", "Tech Meetup", "Food Festival");
    }

    @Test
    public void filter_dateMode_matchesDate() throws Exception {
        setSearchMode(1);
        invokeFilter("2026-03-10");

        assertDisplayedTitles("Comedy Night");
    }

    @Test
    public void filter_dateMode_ignoresTitle() throws Exception {
        setSearchMode(1);
        invokeFilter("comedy");

        assertDisplayedTitles();
    }

    @Test
    public void filter_locationMode_matchesLocation() throws Exception {
        setSearchMode(2);
        invokeFilter("campus hall");

        assertDisplayedTitles("Tech Meetup");
    }

    @Test
    public void filter_locationMode_ignoresCategory() throws Exception {
        setSearchMode(2);
        invokeFilter("tech");

        assertDisplayedTitles();
    }

    @Test
    public void filter_categoryMode_matchesCategory() throws Exception {
        setSearchMode(3);
        invokeFilter("food");

        assertDisplayedTitles("Food Festival");
    }

    @Test
    public void filter_categoryMode_ignoresLocation() throws Exception {
        setSearchMode(3);
        invokeFilter("old port");

        assertDisplayedTitles();
    }

    @Test
    public void filter_emptyQuery_restoresAllEvents() throws Exception {
        setSearchMode(0);

        invokeFilter("tech");
        assertDisplayedTitles("Tech Meetup");

        invokeFilter("");
        assertDisplayedTitles("Comedy Night", "Tech Meetup", "Food Festival");
    }

    @Test
    public void filter_isCaseInsensitive_andTrimsSpaces() throws Exception {
        setSearchMode(2);
        invokeFilter("   cAmPuS hAlL   ");

        assertDisplayedTitles("Tech Meetup");
    }

    @Test
    public void filter_noMatch_showsEmptyState() throws Exception {
        setSearchMode(0);
        invokeFilter("zzzzzz");

        assertTrue(events.isEmpty());
        assertEquals(View.VISIBLE, emptyView.getVisibility());
    }

    @Test
    public void filter_afterNoMatch_thenMatch_hidesEmptyState() throws Exception {
        setSearchMode(0);

        invokeFilter("zzzzzz");
        assertEquals(View.VISIBLE, emptyView.getVisibility());

        invokeFilter("comedy");
        assertDisplayedTitles("Comedy Night");
        assertEquals(View.GONE, emptyView.getVisibility());
    }

    @Test
    public void filter_nullFields_doesNotCrash() throws Exception {
        // Null values should not break filter
        allEvents.add(new EventAdapter.EventItem("4", null, "2026-04-01", null, null, ""));
        events.add(allEvents.get(allEvents.size() - 1));

        setSearchMode(0);
        invokeFilter("comedy");

        assertDisplayedTitles("Comedy Night");
    }

    // Helpers
    @SuppressWarnings("unchecked")
    private List<EventAdapter.EventItem> getPrivateList(String fieldName) throws Exception {
        Field field = EventListActivity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (List<EventAdapter.EventItem>) field.get(activity);
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = EventListActivity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(activity, value);
    }

    private void setSearchMode(int mode) throws Exception {
        Field field = EventListActivity.class.getDeclaredField("searchMode");
        field.setAccessible(true);
        field.setInt(activity, mode);
    }

    private void invokeFilter(String query) throws Exception {
        Method method = EventListActivity.class.getDeclaredMethod("filter", String.class);
        method.setAccessible(true);
        method.invoke(activity, query);
    }

    private void assertDisplayedTitles(String... expectedTitles) {
        List<String> actualTitles = new ArrayList<>();
        for (EventAdapter.EventItem item : events) {
            actualTitles.add(item.title);
        }
        assertTrue(actualTitles.equals(Arrays.asList(expectedTitles)));
    }
}