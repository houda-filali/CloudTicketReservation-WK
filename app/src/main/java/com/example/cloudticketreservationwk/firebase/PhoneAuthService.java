package com.example.cloudticketreservationwk.firebase;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneAuthService {

    private static final String TAG = "PHONE_AUTH";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final RoleService roleService = new RoleService();

    public interface StartCallback {
        void onCodeSent(String verificationId);
        void onAutoVerified(FirebaseUser user);
        void onError(Exception e);
    }

    public interface VerifyCallback {
        void onSuccess(FirebaseUser user);
        void onError(Exception e);
    }

    public void startPhoneVerification(String phoneNumberE164, Activity activity, StartCallback cb) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumberE164)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential credential) {
                                Log.d(TAG, "onVerificationCompleted (auto). Signing in...");
                                auth.signInWithCredential(credential)
                                        .addOnSuccessListener(result -> {
                                            FirebaseUser user = result.getUser();
                                            if (user == null) {
                                                cb.onError(new Exception("User is null after auto verification"));
                                                return;
                                            }

                                            roleService.ensureUserProfile(
                                                    user.getUid(),
                                                    null,
                                                    user.getPhoneNumber(),
                                                    Constants.ROLE_CUSTOMER,
                                                    new RoleService.SimpleCallback() {
                                                        @Override public void onSuccess() { cb.onAutoVerified(user); }
                                                        @Override public void onError(Exception e) { cb.onError(e); }
                                                    }
                                            );
                                        })
                                        .addOnFailureListener(cb::onError);
                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e) {
                                Log.e(TAG, "onVerificationFailed", e);
                                cb.onError(e);
                            }

                            @Override
                            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                                Log.d(TAG, "onCodeSent verificationId=" + verificationId);
                                cb.onCodeSent(verificationId);
                            }
                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void verifyOtp(String verificationId, String otpCode, VerifyCallback cb) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpCode);

        auth.signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        cb.onError(new Exception("User is null after OTP verification"));
                        return;
                    }

                    roleService.ensureUserProfile(
                            user.getUid(),
                            null,
                            user.getPhoneNumber(),
                            Constants.ROLE_CUSTOMER,
                            new RoleService.SimpleCallback() {
                                @Override public void onSuccess() { cb.onSuccess(user); }
                                @Override public void onError(Exception e) { cb.onError(e); }
                            }
                    );
                })
                .addOnFailureListener(cb::onError);
    }
}