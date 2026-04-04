package com.example.cloudticketreservationwk.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;

import android.view.View;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class EventListActivitySearchFilterTest {

    private EventListActivity activity;
    private List<EventAdapter.EventItem> events;
    private List<EventAdapter.EventItem> allEvents;
    private EventAdapter adapter;
    private View emptyView;

    @BeforeEach
    void setUp() throws Exception {
        activity = mock(EventListActivity.class, CALLS_REAL_METHODS);

        events = new ArrayList<>();
        allEvents = new ArrayList<>();

        allEvents.addAll(Arrays.asList(
                new EventAdapter.EventItem("1", "Comedy Night", "2026-03-10", "Downtown", "Comedy", ""),
                new EventAdapter.EventItem("2", "Tech Meetup", "2026-03-12", "Campus Hall", "Tech", ""),
                new EventAdapter.EventItem("3", "Food Festival", "2026-03-25", "Old Port", "Food", "")
        ));

        events.addAll(allEvents);

        adapter = mock(EventAdapter.class);
        emptyView = mock(View.class);

        setField("events", events);
        setField("allEvents", allEvents);
        setField("adapter", adapter);
        setField("tvEmpty", emptyView);
        setSearchMode(0);
    }

    @Test
    void filter_allMode_matchesTitle() throws Exception {
        invokeFilter("comedy");

        assertDisplayedTitles("Comedy Night");
        verify(emptyView).setVisibility(View.GONE);
        verify(adapter).notifyDataSetChanged();
    }

    @Test
    void filter_allMode_matchesDate() throws Exception {
        invokeFilter("2026-03-12");

        assertDisplayedTitles("Tech Meetup");
    }

    @Test
    void filter_allMode_matchesLocation() throws Exception {
        invokeFilter("old port");

        assertDisplayedTitles("Food Festival");
    }

    @Test
    void filter_allMode_matchesCategory() throws Exception {
        invokeFilter("food");

        assertDisplayedTitles("Food Festival");
    }

    @Test
    void filter_allMode_returnsMultipleMatches() throws Exception {
        invokeFilter("2026-03");

        assertDisplayedTitles("Comedy Night", "Tech Meetup", "Food Festival");
    }

    @Test
    void filter_dateMode_matchesDate() throws Exception {
        setSearchMode(1);
        invokeFilter("2026-03-10");

        assertDisplayedTitles("Comedy Night");
    }

    @Test
    void filter_dateMode_ignoresTitle() throws Exception {
        setSearchMode(1);
        invokeFilter("comedy");

        assertDisplayedTitles();
    }

    @Test
    void filter_locationMode_matchesLocation() throws Exception {
        setSearchMode(2);
        invokeFilter("campus hall");

        assertDisplayedTitles("Tech Meetup");
    }

    @Test
    void filter_locationMode_ignoresCategory() throws Exception {
        setSearchMode(2);
        invokeFilter("tech");

        assertDisplayedTitles();
    }

    @Test
    void filter_categoryMode_matchesCategory() throws Exception {
        setSearchMode(3);
        invokeFilter("food");

        assertDisplayedTitles("Food Festival");
    }

    @Test
    void filter_categoryMode_ignoresLocation() throws Exception {
        setSearchMode(3);
        invokeFilter("old port");

        assertDisplayedTitles();
    }

    @Test
    void filter_emptyQuery_restoresAllEvents() throws Exception {
        invokeFilter("tech");
        assertDisplayedTitles("Tech Meetup");

        invokeFilter("");
        assertDisplayedTitles("Comedy Night", "Tech Meetup", "Food Festival");
    }

    @Test
    void filter_isCaseInsensitive_andTrimsSpaces() throws Exception {
        setSearchMode(2);
        invokeFilter("   cAmPuS hAlL   ");

        assertDisplayedTitles("Tech Meetup");
    }

    @Test
    void filter_noMatch_showsEmptyState() throws Exception {
        invokeFilter("zzzzzz");

        assertTrue(events.isEmpty());
        verify(emptyView).setVisibility(View.VISIBLE);
    }

    @Test
    void filter_afterNoMatch_thenMatch_hidesEmptyState() throws Exception {
        invokeFilter("zzzzzz");
        assertTrue(events.isEmpty());
        verify(emptyView, times(1)).setVisibility(View.VISIBLE);

        clearInvocations(emptyView);

        invokeFilter("comedy");
        assertDisplayedTitles("Comedy Night");
        verify(emptyView, times(1)).setVisibility(View.GONE);
    }

    @Test
    void filter_nullFields_doesNotCrash() throws Exception {
        allEvents.add(new EventAdapter.EventItem("4", null, "2026-04-01", null, null, ""));

        invokeFilter("comedy");

        assertDisplayedTitles("Comedy Night");
    }

    private void setField(String fieldName, Object value) throws Exception {
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
        assertEquals(Arrays.asList(expectedTitles), actualTitles);
    }
}