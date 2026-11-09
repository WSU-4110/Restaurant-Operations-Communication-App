package com.example.stafflink;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RoleRepository {

    public interface DuplicateCheckCallback {
        void onResult(boolean exists);
        void onFailure(Exception error);
    }

    public interface SaveCallback {
        void onSuccess();
        void onFailure(Exception error);
    }

    private final FirebaseFirestore db;

    public RoleRepository(FirebaseFirestore db) {
        this.db = db;
    }

    private CollectionReference rolesRef(String organizationId) {
        return db.collection("organizations").document(organizationId)
                .collection("roles");
    }

    public void activeRoleNameExists(String organizationId, String name,
                                     DuplicateCheckCallback callback) {
        rolesRef(organizationId).get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot role : snapshot.getDocuments()) {
                        if (Boolean.TRUE.equals(role.getBoolean("archived"))) {
                            continue;
                        }
                        String saved = role.getString("name");
                        if (saved != null && saved.trim().replaceAll("\s+", " ")
                                .equalsIgnoreCase(name)) {
                            callback.onResult(true);
                            return;
                        }
                    }
                    callback.onResult(false);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void createRole(String organizationId, String userId, String name,
                           boolean viewChat, boolean sendChat,
                           SaveCallback callback) {
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("viewChat", viewChat);
        permissions.put("sendChat", sendChat);

        Map<String, Object> role = new HashMap<>();
        role.put("name", name);
        role.put("createdBy", userId);
        role.put("createdAt", FieldValue.serverTimestamp());
        role.put("archived", false);
        role.put("permissions", permissions);

        rolesRef(organizationId).add(role)
                .addOnSuccessListener(reference -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}
