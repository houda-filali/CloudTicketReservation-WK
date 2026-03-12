package com.example.cloudticketreservationwk;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.cloudticketreservationwk.firebase.AuthService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class AuthLoginLogoutInstrumentedTest {

    private final AuthService authService = new AuthService();

    @After
    public void cleanup() {
        FirebaseAuth.getInstance().signOut();
    }

    @Test
    public void login_then_logout_works() throws Exception {
        // 1) Create a brand new account (so login test is deterministic)
        String email = "test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        String pass = "pass1234";

        FirebaseUser registeredUser = registerBlocking(email, pass);
        assertNotNull("Registered user should not be null", registeredUser);

        // 2) Logout
        authService.logout();
        assertNull("After logout, currentUser should be null", FirebaseAuth.getInstance().getCurrentUser());

        // 3) Login again
        FirebaseUser loggedInUser = loginBlocking(email, pass);
        assertNotNull("Logged in user should not be null", loggedInUser);
        assertEquals("Login should restore same user email", email, loggedInUser.getEmail());

        // 4) Logout again
        authService.logout();
        assertNull("After logout, currentUser should be null", FirebaseAuth.getInstance().getCurrentUser());
    }

    private FirebaseUser registerBlocking(String email, String pass) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final FirebaseUser[] out = new FirebaseUser[1];
        final Exception[] err = new Exception[1];

        authService.registerWithEmail(email, pass, new AuthService.UserCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                out[0] = user;
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                err[0] = e;
                latch.countDown();
            }
        });

        assertTrue("Register timed out", latch.await(30, TimeUnit.SECONDS));
        if (err[0] != null) throw err[0];
        return out[0];
    }

    private FirebaseUser loginBlocking(String email, String pass) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final FirebaseUser[] out = new FirebaseUser[1];
        final Exception[] err = new Exception[1];

        authService.loginWithEmail(email, pass, new AuthService.UserCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                out[0] = user;
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                err[0] = e;
                latch.countDown();
            }
        });

        assertTrue("Login timed out", latch.await(30, TimeUnit.SECONDS));
        if (err[0] != null) throw err[0];
        return out[0];
    }
}