package com.example.stafflink;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizationPermissionService {

    public interface PermissionCallback {
        void onResult(boolean canManage);
        void onFailure(Exception error);
    }

    private final FirebaseFirestore db;

    public OrganizationPermissionService(FirebaseFirestore db) {
        this.db = db;
    }

    public void canManageRoles(String organizationId, String userId,
                               PermissionCallback callback) {
        FirebaseUser signedInUser = FirebaseAuth.getInstance().getCurrentUser();
        if (signedInUser == null || !signedInUser.getUid().equals(userId)) {
            callback.onResult(false);
            return;
        }

        db.collection("organizations").document(organizationId).get()
                .addOnSuccessListener(organization -> {
                    if (!organization.exists()) {
                        callback.onResult(false);
                        return;
                    }
                    if (userId.equals(organization.getString("ownerId"))) {
                        callback.onResult(true);
                        return;
                    }
                    db.collection("organizations").document(organizationId)
                            .collection("members").document(userId).get()
                            .addOnSuccessListener(member ->
                                    callback.onResult(member.exists()
                                            && "manager".equals(member.getString("role"))))
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
