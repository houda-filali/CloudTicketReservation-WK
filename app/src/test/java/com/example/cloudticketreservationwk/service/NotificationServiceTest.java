package com.example.cloudticketreservationwk.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import android.content.Context;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;

public class NotificationServiceTest {

    private Context mockContext;
    private FirebaseFirestore mockFirestore;
    private CollectionReference mockReservationsCollection;
    private CollectionReference mockNotificationsCollection;
    private Query mockQueryAfterEventId;
    private Query mockQueryAfterStatus;
    private Task<QuerySnapshot> mockTask;
    private QuerySnapshot mockQuerySnapshot;
    private DocumentSnapshot mockDoc1;
    private DocumentSnapshot mockDoc2;
    private DocumentReference mockDocRef1;
    private DocumentReference mockDocRef2;

    private DocumentReference mockNotifDoc1;
    private DocumentReference mockNotifDoc2;
    private Task<Void> mockSetTask;

    private WriteBatch mockBatch;
    private Task<Void> mockCommitTask;

    @BeforeEach
    public void setUp() {
        mockContext = mock(Context.class);
        mockFirestore = mock(FirebaseFirestore.class);
        mockReservationsCollection = mock(CollectionReference.class);
        mockNotificationsCollection = mock(CollectionReference.class);
        mockQueryAfterEventId = mock(Query.class);
        mockQueryAfterStatus = mock(Query.class);
        mockTask = mock(Task.class);
        mockQuerySnapshot = mock(QuerySnapshot.class);
        mockDoc1 = mock(DocumentSnapshot.class);
        mockDoc2 = mock(DocumentSnapshot.class);
        mockDocRef1 = mock(DocumentReference.class);
        mockDocRef2 = mock(DocumentReference.class);

        mockNotifDoc1 = mock(DocumentReference.class);
        mockNotifDoc2 = mock(DocumentReference.class);
        mockSetTask = mock(Task.class);

        mockBatch = mock(WriteBatch.class);
        mockCommitTask = mock(Task.class);
    }

    @Test
    public void testNotifyEventCancellation_Success() {
        try (MockedStatic<FirebaseFirestore> firestoreStatic = mockStatic(FirebaseFirestore.class)) {
            firestoreStatic.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);

            when(mockFirestore.collection("reservations")).thenReturn(mockReservationsCollection);
            when(mockFirestore.collection("notifications")).thenReturn(mockNotificationsCollection);
            when(mockFirestore.batch()).thenReturn(mockBatch);

            when(mockReservationsCollection.whereEqualTo("eventId", "event123"))
                    .thenReturn(mockQueryAfterEventId);
            when(mockQueryAfterEventId.whereEqualTo("status", "Active"))
                    .thenReturn(mockQueryAfterStatus);
            when(mockQueryAfterStatus.get()).thenReturn(mockTask);

            when(mockDoc1.getString("userId")).thenReturn("user1");
            when(mockDoc1.getId()).thenReturn("res1");
            when(mockDoc1.getReference()).thenReturn(mockDocRef1);

            when(mockDoc2.getString("userId")).thenReturn("user2");
            when(mockDoc2.getId()).thenReturn("res2");
            when(mockDoc2.getReference()).thenReturn(mockDocRef2);

            when(mockQuerySnapshot.getDocuments()).thenReturn(Arrays.asList(mockDoc1, mockDoc2));

            when(mockNotificationsCollection.document())
                    .thenReturn(mockNotifDoc1)
                    .thenReturn(mockNotifDoc2);

            when(mockNotifDoc1.set(any())).thenReturn(mockSetTask);
            when(mockNotifDoc2.set(any())).thenReturn(mockSetTask);

            when(mockBatch.update(any(DocumentReference.class), any(String.class), any()))
                    .thenReturn(mockBatch);
            when(mockBatch.commit()).thenReturn(mockCommitTask);

            when(mockTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
                OnSuccessListener<QuerySnapshot> listener = invocation.getArgument(0);
                listener.onSuccess(mockQuerySnapshot);
                return mockTask;
            });
            when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);

            when(mockCommitTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
                OnSuccessListener<Void> listener = invocation.getArgument(0);
                listener.onSuccess(null);
                return mockCommitTask;
            });
            when(mockCommitTask.addOnFailureListener(any())).thenReturn(mockCommitTask);

            NotificationService service = new NotificationService(mockContext);
            TestCallback callback = new TestCallback();

            service.notifyEventCancellation("event123", "Test Event", callback);

            verify(mockFirestore).collection("reservations");
            verify(mockReservationsCollection).whereEqualTo("eventId", "event123");
            verify(mockQueryAfterEventId).whereEqualTo("status", "Active");
            verify(mockQueryAfterStatus).get();

            verify(mockNotificationsCollection, times(2)).document();

            verify(mockBatch).update(mockDocRef1, "status", "Cancelled");
            verify(mockBatch).update(mockDocRef2, "status", "Cancelled");
            verify(mockBatch).commit();

            assertTrue(callback.successCalled);
            assertFalse(callback.failureCalled);
            assertEquals("Cancelled 2 reservations and notified users", callback.successMessage);
        }
    }

    @Test
    public void testNotifyEventCancellation_NoReservations() {
        try (MockedStatic<FirebaseFirestore> firestoreStatic = mockStatic(FirebaseFirestore.class)) {
            firestoreStatic.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);

            when(mockFirestore.collection("reservations")).thenReturn(mockReservationsCollection);
            when(mockFirestore.collection("notifications")).thenReturn(mockNotificationsCollection);
            when(mockFirestore.batch()).thenReturn(mockBatch);

            when(mockReservationsCollection.whereEqualTo("eventId", "event123"))
                    .thenReturn(mockQueryAfterEventId);
            when(mockQueryAfterEventId.whereEqualTo("status", "Active"))
                    .thenReturn(mockQueryAfterStatus);
            when(mockQueryAfterStatus.get()).thenReturn(mockTask);

            when(mockQuerySnapshot.getDocuments()).thenReturn(Collections.emptyList());

            when(mockBatch.commit()).thenReturn(mockCommitTask);

            when(mockTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
                OnSuccessListener<QuerySnapshot> listener = invocation.getArgument(0);
                listener.onSuccess(mockQuerySnapshot);
                return mockTask;
            });
            when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);

            when(mockCommitTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
                OnSuccessListener<Void> listener = invocation.getArgument(0);
                listener.onSuccess(null);
                return mockCommitTask;
            });
            when(mockCommitTask.addOnFailureListener(any())).thenReturn(mockCommitTask);

            NotificationService service = new NotificationService(mockContext);
            TestCallback callback = new TestCallback();

            service.notifyEventCancellation("event123", "Test Event", callback);

            verify(mockNotificationsCollection, never()).document();
            verify(mockBatch, never()).update(any(DocumentReference.class), any(String.class), any());
            verify(mockBatch).commit();

            assertTrue(callback.successCalled);
            assertFalse(callback.failureCalled);
            assertEquals("Cancelled 0 reservations and notified users", callback.successMessage);
        }
    }

    @Test
    public void testNotifyEventCancellation_Failure() {
        try (MockedStatic<FirebaseFirestore> firestoreStatic = mockStatic(FirebaseFirestore.class)) {
            firestoreStatic.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);

            when(mockFirestore.collection("reservations")).thenReturn(mockReservationsCollection);

            when(mockReservationsCollection.whereEqualTo("eventId", "event123"))
                    .thenReturn(mockQueryAfterEventId);
            when(mockQueryAfterEventId.whereEqualTo("status", "Active"))
                    .thenReturn(mockQueryAfterStatus);
            when(mockQueryAfterStatus.get()).thenReturn(mockTask);

            RuntimeException exception = new RuntimeException("Permission denied");

            when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);
            when(mockTask.addOnFailureListener(any())).thenAnswer(invocation -> {
                OnFailureListener listener = invocation.getArgument(0);
                listener.onFailure(exception);
                return mockTask;
            });

            NotificationService service = new NotificationService(mockContext);
            TestCallback callback = new TestCallback();

            service.notifyEventCancellation("event123", "Test Event", callback);

            assertFalse(callback.successCalled);
            assertTrue(callback.failureCalled);
            assertEquals("Failed to fetch reservations: Permission denied", callback.failureMessage);

            verify(mockFirestore, never()).collection("notifications");
        }
    }

    private static class TestCallback implements NotificationService.NotificationCallback {
        boolean successCalled = false;
        boolean failureCalled = false;
        String successMessage;
        String failureMessage;

        @Override
        public void onSuccess(String message) {
            successCalled = true;
            successMessage = message;
        }

        @Override
        public void onFailure(String error) {
            failureCalled = true;
            failureMessage = error;
        }
    }
}