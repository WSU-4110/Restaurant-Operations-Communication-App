package com.example.stafflink;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.DocumentSnapshot;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
public class OrganizationFacade {
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public OrganizationFacade() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public interface CreateOrganizationCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    private String generateJoinCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }

    public void createOrganization(
            String organizationName,
            CreateOrganizationCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            callback.onFailure(new Exception("Please log in first"));
            return;
        }

        String ownerId = currentUser.getUid();

        DocumentReference organizationRef =
                db.collection("organizations").document();

        DocumentReference memberRef =
                organizationRef.collection("members").document(ownerId);

        String joinCode = generateJoinCode();

        DocumentReference joinCodeRef =
                db.collection("joinCodes").document(joinCode);

        Map<String, Object> organization = new HashMap<>();
        organization.put("name", organizationName);
        organization.put("ownerId", ownerId);
        organization.put("createdAt", FieldValue.serverTimestamp());
        organization.put("joinCode", joinCode);

        Map<String, Object> member = new HashMap<>();
        member.put("role", "owner");
        member.put("joinedAt", FieldValue.serverTimestamp());

        Map<String, Object> joinCodeData = new HashMap<>();
        joinCodeData.put("organizationId", organizationRef.getId());
        joinCodeData.put("createdAt", FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();
        batch.set(organizationRef, organization);
        batch.set(memberRef, member);
        batch.set(joinCodeRef, joinCodeData);

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }


    public interface JoinOrganizationCallback {
        void onSuccess();
        void onFailure(String message);
    }

    public void joinOrganization(
            String joinCode,
            JoinOrganizationCallback callback) {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            callback.onFailure("Please log in first");
            return;
        }

        db.collection("joinCodes").document(joinCode)
                .get()
                .addOnSuccessListener(codeDocument -> {
                    if (!codeDocument.exists()) {
                        callback.onFailure("Invalid join code");
                        return;
                    }

                    String organizationId =
                            codeDocument.getString("organizationId");

                    if (organizationId == null) {
                        callback.onFailure("Invalid organization");
                        return;
                    }

                    String userId = currentUser.getUid();

                    DocumentReference memberRef = db
                            .collection("organizations")
                            .document(organizationId)
                            .collection("members")
                            .document(userId);

                    DocumentReference userOrganizationRef = db
                            .collection("users")
                            .document(userId)
                            .collection("organizations")
                            .document(organizationId);

                    memberRef.get()
                            .addOnSuccessListener(memberDocument -> {
                                if (memberDocument.exists()) {
                                    callback.onFailure("You are already a member");
                                    return;
                                }

                                Map<String, Object> member = new HashMap<>();
                                member.put("role", "employee");
                                member.put("joinCode", joinCode);
                                member.put("joinedAt",
                                        FieldValue.serverTimestamp());

                                Map<String, Object> userOrganization =
                                        new HashMap<>();
                                userOrganization.put("role", "employee");
                                userOrganization.put("joinedAt",
                                        FieldValue.serverTimestamp());

                                WriteBatch batch = db.batch();
                                batch.set(memberRef, member);
                                batch.set(userOrganizationRef, userOrganization);

                                batch.commit()
                                        .addOnSuccessListener(aVoid ->
                                                callback.onSuccess())
                                        .addOnFailureListener(e ->
                                                callback.onFailure(
                                                        "Error: " + e.getMessage()));
                            })
                            .addOnFailureListener(e ->
                                    callback.onFailure("Error: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Error: " + e.getMessage()));
    }


    public interface LoadOrganizationsCallback {
        void onOrganizationLoaded(String organizationId, String organizationName);
        void onComplete();
        void onError(String message);
    }

    public void loadOrganizations(LoadOrganizationsCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            callback.onComplete();
            return;
        }

        String userId = currentUser.getUid();

        db.collection("organizations")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        callback.onOrganizationLoaded(
                                document.getId(),
                                document.getString("name")
                        );
                    }

                    db.collection("users")
                            .document(userId)
                            .collection("organizations")
                            .get()
                            .addOnSuccessListener(joinedSnapshot -> {

                                if (joinedSnapshot.isEmpty()) {
                                    callback.onComplete();
                                    return;
                                }

                                final int[] remaining = {
                                        joinedSnapshot.size()
                                };

                                for (DocumentSnapshot membership :
                                        joinedSnapshot.getDocuments()) {

                                    String organizationId = membership.getId();

                                    db.collection("organizations")
                                            .document(organizationId)
                                            .get()
                                            .addOnSuccessListener(document -> {
                                                if (document.exists()) {
                                                    callback.onOrganizationLoaded(
                                                            organizationId,
                                                            document.getString("name")
                                                    );
                                                }

                                                remaining[0]--;
                                                if (remaining[0] == 0) {
                                                    callback.onComplete();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                remaining[0]--;

                                                if (remaining[0] == 0) {
                                                    callback.onComplete();
                                                }

                                                callback.onError(
                                                        "Error loading joined organization"
                                                );
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                callback.onError(
                                        "Error loading memberships: " + e.getMessage()
                                );
                                callback.onComplete();
                            });
                })
                .addOnFailureListener(e -> {
                    callback.onError(
                            "Error loading organizations: " + e.getMessage()
                    );
                    callback.onComplete();
                });
    }

    public void signOut() {
        auth.signOut();
    }


}
