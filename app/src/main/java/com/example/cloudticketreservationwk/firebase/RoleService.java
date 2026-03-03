package com.example.cloudticketreservationwk.firebase;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class RoleService {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface SimpleCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public void ensureUserProfile(String uid, String email, String phone, String role, SimpleCallback cb) {
        Map<String, Object> data = new HashMap<>();
        if (email != null) data.put("email", email);
        if (phone != null) data.put("phone", phone);

        data.put("role", role);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }
}