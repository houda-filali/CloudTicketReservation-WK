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

                    roleService.ensureUserProfile(
                            user.getUid(),
                            email,
                            null,
                            Constants.ROLE_CUSTOMER,
                            new RoleService.SimpleCallback() {
                                @Override
                                public void onSuccess() {
                                    cb.onSuccess(user);
                                }

                                @Override
                                public void onError(Exception e) {
                                    cb.onError(e);
                                }
                            }
                    );
                })
                .addOnFailureListener(cb::onError);
    }
}