package com.example.stafflink;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

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
}
