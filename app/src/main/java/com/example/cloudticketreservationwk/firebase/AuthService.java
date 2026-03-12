package com.example.cloudticketreservationwk.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthService {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final RoleService roleService = new RoleService();

    public interface UserCallback {
        void onSuccess(FirebaseUser user);
        void onError(Exception e);
    }

    public void registerWithEmail(String email, String password, UserCallback cb) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        cb.onError(new Exception("User is null after registration"));
                        return;
                    }

                    String normalized = (email == null) ? "" : email.trim().toLowerCase();
                    String role = normalized.contains(".admin")
                            ? Constants.ROLE_ADMIN
                            : Constants.ROLE_CUSTOMER;

                    roleService.ensureUserProfile(
                            user.getUid(),
                            email,
                            null,
                            role,
                            new RoleService.SimpleCallback() {
                                @Override public void onSuccess() { cb.onSuccess(user); }
                                @Override public void onError(Exception e) { cb.onError(e); }
                            }
                    );
                })
                .addOnFailureListener(cb::onError);
    }

    public void loginWithEmail(String email, String password, UserCallback cb) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        cb.onError(new Exception("User is null after login"));
                        return;
                    }
                    cb.onSuccess(user);
                })
                .addOnFailureListener(cb::onError);
    }

    public void logout() {
        auth.signOut();
    }
}