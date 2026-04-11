package com.example.cloudticketreservationwk.integration;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.controller.MainActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InvalidLoginIntegrationTest {

    private FirebaseUser createdUser;

    @BeforeEach
    void setUp() {
        // Make sure Firebase is initialized
        if (FirebaseApp.getApps(InstrumentationRegistry.getInstrumentation().getTargetContext()).isEmpty()) {
            FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().getTargetContext());
        }

        // Start each test signed out
        FirebaseAuth.getInstance().signOut();
    }

    @AfterEach
    void tearDown() {
        // Delete the test user created during the test
        try {
            if (createdUser != null) {
                createdUser.delete();
            }
        } catch (Exception ignored) {}

        // Sign out after the test
        FirebaseAuth.getInstance().signOut();
    }

    @Test
    void login_withWrongPassword_showsErrorMessage() throws Exception {
        // Create a real user for the test
        String email = "login" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        String correctPassword = "test1234";
        String wrongPassword = "wrong1234";

        createdUser = registerBlocking(email, correctPassword);
        assertNotNull(createdUser);

        // Sign out before trying to log in with the wrong password
        FirebaseAuth.getInstance().signOut();

        // Open the login screen
        ActivityScenario.launch(MainActivity.class);

        // Enter a valid email but a wrong password
        onView(withId(R.id.etEmail)).perform(replaceText(email), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(replaceText(wrongPassword), closeSoftKeyboard());
        onView(withId(R.id.btnSubmit)).perform(click());

        // Wait a bit for async Firebase callback to update the UI
        onView(isRoot()).perform(waitFor(3000));

        // Check that the correct error message is shown
        onView(withId(R.id.tvError))
                .check(matches(withText("Incorrect password")));
    }

    private FirebaseUser registerBlocking(String email, String password) throws Exception {
        // Wait for async Firebase registration to finish
        CountDownLatch latch = new CountDownLatch(1);
        final FirebaseUser[] out = new FirebaseUser[1];

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    out[0] = result.getUser();
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        return out[0];
    }

    private static ViewAction waitFor(long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}