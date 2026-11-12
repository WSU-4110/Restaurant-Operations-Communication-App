package com.example.stafflink;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class OrganizationHomeActivity extends AppCompatActivity {

    private String organizationId;
    private String organizationName;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organization_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        organizationId = getIntent().getStringExtra("organizationId");
        organizationName = getIntent().getStringExtra("organizationName");

        if (organizationId == null || organizationName == null) {
            finish();
            return;
        }

        TextView organizationNameTitle = findViewById(R.id.organizationNameTitle);
        organizationNameTitle.setText(organizationName);

        Button manageRolesButton = findViewById(R.id.manageRolesButton);
        manageRolesButton.setVisibility(View.GONE);
        manageRolesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageRolesActivity.class);
            intent.putExtra("organizationId", organizationId);
            startActivity(intent);
        });

        syncMemberDisplayName();

        Button generalButton = findViewById(R.id.generalButton);
        generalButton.setOnClickListener(v -> openChat(null));

        Button createTaskButton = findViewById(R.id.createTaskNavigationButton);

        createTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateTaskActivity.class);
            intent.putExtra("organizationId", organizationId);
            startActivity(intent);
        });
    }

    private void syncMemberDisplayName() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String uid = currentUser.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(profile -> {
                    if (!profile.exists()) {
                        return;
                    }

                    String firstName = profile.getString("firstName");
                    String lastName = profile.getString("lastName");
                    String displayName = (
                            (firstName == null ? "" : firstName.trim()) + " " +
                            (lastName == null ? "" : lastName.trim())
                    ).trim();
                    displayName = displayName.replaceAll("\\s+", " ");

                    if (displayName.isEmpty() || displayName.length() > 100) {
                        Log.w("StaffLink", "Profile name is missing or too long");
                        return;
                    }

                    final String nameToSave = displayName;
                    db.collection("organizations")
                            .document(organizationId)
                            .collection("members")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(membership -> {
                                if (!membership.exists()) {
                                    return;
                                }
                                if (nameToSave.equals(membership.getString("displayName"))) {
                                    return;
                                }
                                membership.getReference().update("displayName", nameToSave)
                                        .addOnFailureListener(error -> Log.w(
                                                "StaffLink", "Unable to sync member display name", error));
                            })
                            .addOnFailureListener(error -> Log.w(
                                    "StaffLink", "Unable to read membership for display name", error));
                })
                .addOnFailureListener(error -> Log.w(
                        "StaffLink", "Unable to read own user profile", error));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db != null && organizationId != null) {
            loadJoinCode();
            loadRoleChats();
        }
    }

    private void loadRoleChats() {
        LinearLayout container = findViewById(R.id.roleChatsContainer);
        container.removeAllViews();
        TextView status = new TextView(this);
        status.setText("Loading role chats...");
        container.addView(status);

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        DocumentReference organization = db.collection("organizations").document(organizationId);
        organization.get()
                .addOnSuccessListener(organizationSnapshot -> {
                    organization.collection("members").document(user.getUid()).get()
                            .addOnSuccessListener(member -> {
                                RoleChatAccess access = new RoleChatAccess(user.getUid(),
                                        organizationSnapshot.getString("ownerId"), member.getData());
                                organization.collection("roles").get()
                                        .addOnSuccessListener(roles -> renderRoleChats(
                                                container, status, roles, access))
                                        .addOnFailureListener(error -> status.setText(
                                                "Unable to load role chats. Reopen this organization to retry."));
                            })
                            .addOnFailureListener(error -> status.setText(
                                    "Unable to verify chat access."));
                })
                .addOnFailureListener(error -> status.setText("Unable to load organization."));
    }

    private void renderRoleChats(LinearLayout container, TextView status,
                                 QuerySnapshot roles, RoleChatAccess access) {
        container.removeAllViews();
        for (DocumentSnapshot role : roles) {
            if (!access.canView(role.getId(), role.getData())) {
                continue;
            }

            Button button = new Button(this);
            button.setAllCaps(false);
            button.setText(role.getString("name") + " Chat");
            button.setOnClickListener(v -> openChat(role.getId()));
            container.addView(button);
        }

        if (container.getChildCount() == 0) {
            status.setText("No role chats available. Ask a manager to assign a role and enable chat access.");
            container.addView(status);
        }
    }

    private void openChat(String roleId) {
        Intent intent = new Intent(this, GeneralChatActivity.class);
        intent.putExtra("organizationId", organizationId);
        intent.putExtra("organizationName", organizationName);
        intent.putExtra("roleId", roleId);
        startActivity(intent);
    }

    private void loadJoinCode() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        findViewById(R.id.manageRolesButton).setVisibility(View.GONE);
        findViewById(R.id.joinCodeSection).setVisibility(View.GONE);

        db.collection("organizations").document(organizationId).get()
                .addOnSuccessListener(organization -> {
                    if (!organization.exists()) {
                        return;
                    }
                    String ownerId = organization.getString("ownerId");
                    String joinCode = organization.getString("joinCode");
                    if (user.getUid().equals(ownerId)) {
                        showAdminControls(joinCode);
                    } else {
                        db.collection("organizations").document(organizationId)
                                .collection("members").document(user.getUid()).get()
                                .addOnSuccessListener(member -> {
                                    if (member.exists() && "manager".equals(member.getString("role"))) {
                                        showAdminControls(joinCode);
                                    }
                                })
                                .addOnFailureListener(error -> Log.w("StaffLink", "Could not verify manager", error));
                    }
                })
                .addOnFailureListener(error -> Toast.makeText(this,
                        "Error loading organization: " + error.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

    private void showAdminControls(String joinCode) {
        findViewById(R.id.manageRolesButton).setVisibility(View.VISIBLE);
        if (joinCode == null || joinCode.trim().isEmpty()) {
            return;
        }

        LinearLayout joinCodeSection = findViewById(R.id.joinCodeSection);
        TextView joinCodeText = findViewById(R.id.joinCodeText);
        Button copyJoinCodeButton = findViewById(R.id.copyJoinCodeButton);
        joinCodeText.setText(joinCode);
        joinCodeSection.setVisibility(View.VISIBLE);
        copyJoinCodeButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("StaffLink Join Code", joinCode));
                Toast.makeText(this, "Join code copied!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
