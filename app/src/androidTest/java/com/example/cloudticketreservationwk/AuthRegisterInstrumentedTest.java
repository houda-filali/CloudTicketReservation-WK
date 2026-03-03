package com.example.cloudticketreservationwk;

import static org.junit.Assert.*;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.cloudticketreservationwk.firebase.AuthService;
import com.example.cloudticketreservationwk.firebase.Constants;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class AuthRegisterInstrumentedTest {

    private static final String TAG = "AUTH_TEST";

    private FirebaseUser createdUser;

    @Before
    public void setUp() throws Exception {
        // Ensure Firebase is initialized in the instrumented test environment
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx);
        }
        assertFalse("FirebaseApp did not initialize", FirebaseApp.getApps(ctx).isEmpty());

        // Optional: sign out before each test to avoid state issues
        FirebaseAuth.getInstance().signOut();
    }

    @After
    public void cleanup() throws Exception {
        // Clean up Firestore + Auth user created during the test.
        // Best-effort cleanup; don't fail the test if cleanup fails.
        if (createdUser != null) {
            try {
                FirebaseFirestore.getInstance()
                        .collection(Constants.USERS_COLLECTION)
                        .document(createdUser.getUid())
                        .delete();
            } catch (Exception ignored) {}

            try {
                createdUser.delete();
            } catch (Exception ignored) {}

            try {
                FirebaseAuth.getInstance().signOut();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Sanity test: Firestore is reachable & writeable (or at least not hanging).
     * If this fails, your Auth test may "hang" waiting for Firestore role doc creation.
     */
    @Test
    public void firestore_ping_write_works() throws Exception {
        Log.d(TAG, "Starting Firestore ping test...");

        CountDownLatch latch = new CountDownLatch(1);
        final Exception[] err = new Exception[1];

        FirebaseFirestore.getInstance()
                .collection("debug")
                .document("ping")
                .set(Collections.singletonMap("ok", true))
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Firestore ping write SUCCESS");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore ping write FAILED", e);
                    err[0] = e;
                    latch.countDown();
                });

        boolean done = latch.await(30, TimeUnit.SECONDS);
        assertTrue("Firestore ping timed out (no callback). Check internet/rules.", done);

        if (err[0] != null) {
            fail("Firestore ping failed: " + err[0].getMessage());
        }
    }

    /**
     * US #2.1: Register with email/password.
     * Verifies:
     * 1) Firebase Auth user is created (uid not null)
     * 2) Firestore users/{uid} doc exists and has role CUSTOMER
     */
    @Test
    public void registerWithEmail_createsAuthUser_andFirestoreRoleDoc() throws Exception {
        Log.d(TAG, "Starting registerWithEmail test...");

        firestore_ping_write_works();

        AuthService authService = new AuthService();

        String email = "test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        String password = "password123"; // >= 6 chars

        CountDownLatch registerLatch = new CountDownLatch(1);
        final Exception[] registerErr = new Exception[1];

        Log.d(TAG, "Calling registerWithEmail(" + email + ")");

        authService.registerWithEmail(email, password, new AuthService.UserCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Log.d(TAG, "registerWithEmail SUCCESS uid=" + user.getUid());
                createdUser = user;
                registerLatch.countDown();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "registerWithEmail ERROR", e);
                registerErr[0] = e;
                registerLatch.countDown();
            }
        });

        boolean registerDone = registerLatch.await(60, TimeUnit.SECONDS);
        assertTrue("Timed out waiting for Firebase Auth register() callback.", registerDone);

        if (registerErr[0] != null) {
            fail("Registration failed: " + registerErr[0].getMessage());
        }

        assertNotNull("User should not be null", createdUser);
        assertNotNull("UID should not be null", createdUser.getUid());

        CountDownLatch roleLatch = new CountDownLatch(1);
        final Exception[] roleErr = new Exception[1];
        final String[] role = new String[1];

        Log.d(TAG, "Reading Firestore role doc users/" + createdUser.getUid());

        FirebaseFirestore.getInstance()
                .collection(Constants.USERS_COLLECTION)
                .document(createdUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        roleErr[0] = new Exception("User doc does not exist in Firestore.");
                    } else {
                        role[0] = doc.getString("role");
                    }
                    roleLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore role read FAILED", e);
                    roleErr[0] = e;
                    roleLatch.countDown();
                });

        boolean roleDone = roleLatch.await(60, TimeUnit.SECONDS);
        assertTrue("Timed out waiting for Firestore role read callback.", roleDone);

        if (roleErr[0] != null) {
            fail("Role read failed: " + roleErr[0].getMessage());
        }

        assertEquals("Expected role CUSTOMER in Firestore", Constants.ROLE_CUSTOMER, role[0]);
        Log.d(TAG, "Role verified as CUSTOMER");
    }
}