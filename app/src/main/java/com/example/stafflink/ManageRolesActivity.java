package com.example.stafflink;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class ManageRolesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String organizationId;
    private String ownerId;
    private String userId;

    private LinearLayout rolesContainer;
    private LinearLayout membersContainer;
    private TextView statusText;
    private Button createRoleButton;

    private final List<RoleItem> roles = new ArrayList<>();
    private final List<MemberItem> members = new ArrayList<>();

    private static class RoleItem {
        final String id;
        final String name;

        RoleItem(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private static class MemberItem {
        final String uid;
        final String membershipType;
        final List<String> roleIds;
        String displayName;

        MemberItem(String uid, String membershipType, List<String> roleIds) {
            this.uid = uid;
            this.membershipType = membershipType;
            this.roleIds = roleIds;
            this.displayName = "Member " + abbreviated(uid);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_roles);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        organizationId = getIntent().getStringExtra("organizationId");

        if (user == null || organizationId == null || organizationId.trim().isEmpty()) {
            Toast.makeText(this, "Missing login or organization", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        userId = user.getUid();
        db = FirebaseFirestore.getInstance();
        rolesContainer = findViewById(R.id.rolesContainer);
        membersContainer = findViewById(R.id.membersContainer);
        statusText = findViewById(R.id.rolesStatusText);
        createRoleButton = findViewById(R.id.createRoleButton);
        createRoleButton.setEnabled(false);
        createRoleButton.setOnClickListener(v -> showCreateRoleDialog());

        // Never trust an intent extra such as "isOwner" to authorize a privileged action.
        verifyOwnerThenLoad();
    }

    private void verifyOwnerThenLoad() {
        statusText.setText("Checking organization permissions...");
        db.collection("organizations").document(organizationId).get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        denyAccess("Organization not found");
                        return;
                    }
                    ownerId = document.getString("ownerId");
                    if (ownerId == null || !ownerId.equals(userId)) {
                        denyAccess("Only the organization owner can manage roles");
                        return;
                    }
                    createRoleButton.setEnabled(true);
                    statusText.setText("Tap a member to assign their roles.");
                    loadRoles();
                    loadMembers();
                })
                .addOnFailureListener(e -> denyAccess("Unable to verify access: " + e.getMessage()));
    }

    private void denyAccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private CollectionReference rolesRef() {
        return db.collection("organizations")
                .document(organizationId).collection("roles");
    }

    private CollectionReference membersRef() {
        return db.collection("organizations")
                .document(organizationId).collection("members");
    }

    private void loadRoles() {
        rolesRef().get()
                .addOnSuccessListener(result -> {
                    roles.clear();
                    for (DocumentSnapshot document : result.getDocuments()) {
                        String name = document.getString("name");
                        if (name != null && !name.trim().isEmpty()) {
                            roles.add(new RoleItem(document.getId(), name));
                        }
                    }
                    Collections.sort(roles, (a, b) ->
                            a.name.compareToIgnoreCase(b.name));
                    renderRoles();
                    renderMembers();
                })
                .addOnFailureListener(e -> showError("Loading roles", e));
    }

    private void loadMembers() {
        membersRef().get()
                .addOnSuccessListener(result -> {
                    members.clear();
                    for (DocumentSnapshot document : result.getDocuments()) {
                        List<String> savedRoles = new ArrayList<>();
                        Object raw = document.get("roleIds");
                        if (raw instanceof List<?>) {
                            for (Object item : (List<?>) raw) {
                                if (item instanceof String) {
                                    savedRoles.add((String) item);
                                }
                            }
                        }
                        String membershipType = document.getString("role");
                        MemberItem member = new MemberItem(document.getId(), membershipType, savedRoles);
                        String savedName = document.getString("displayName");
                        if (savedName != null && !savedName.trim().isEmpty()) {
                            member.displayName = savedName.trim();
                        }
                        members.add(member);
                    }
                    Collections.sort(members, (a, b) ->
                            a.displayName.compareToIgnoreCase(b.displayName));
                    renderMembers();
                })
                .addOnFailureListener(e -> showError("Loading organization members", e));
    }

    private void renderRoles() {
        rolesContainer.removeAllViews();
        if (roles.isEmpty()) {
            addLabel(rolesContainer, "No job roles yet. Create Server, Bartender, Host, etc.");
            return;
        }
        for (RoleItem role : roles) {
            addLabel(rolesContainer, "• " + role.name);
        }
    }

    private void renderMembers() {
        membersContainer.removeAllViews();
        if (members.isEmpty()) {
            addLabel(membersContainer, "No members found.");
            return;
        }
        for (MemberItem member : members) {
            boolean isOwner = member.uid.equals(ownerId);
            Button button = new Button(this);
            button.setAllCaps(false);
            button.setText(member.displayName + (isOwner ? " (Owner)" : "")
                    + "\nAssigned roles: " + roleNames(member.roleIds)
                    + (isOwner ? "" : "\nTap to change"));
            button.setEnabled(!isOwner);
            button.setOnClickListener(v -> showAssignRolesDialog(member));
            membersContainer.addView(button);
        }
    }

    private String roleNames(List<String> ids) {
        List<String> names = new ArrayList<>();
        for (String id : ids) {
            for (RoleItem role : roles) {
                if (role.id.equals(id)) {
                    names.add(role.name);
                    break;
                }
            }
        }
        return names.isEmpty() ? "None" : android.text.TextUtils.join(", ", names);
    }

    private void addLabel(LinearLayout container, String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(16);
        int padding = (int) (10 * getResources().getDisplayMetrics().density);
        label.setPadding(0, padding, 0, padding);
        container.addView(label);
    }

    private void showCreateRoleDialog() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Example: Bartender");
        input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(40)});
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setPadding(pad, 0, pad, 0);
        wrapper.addView(input, new LinearLayout.LayoutParams(-1, -2));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Create restaurant role")
                .setView(wrapper)
                .setPositiveButton("Create", null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String name = input.getText().toString().trim().replaceAll("\\s+", " ");
                    if (name.isEmpty()) {
                        input.setError("Enter a role name");
                        return;
                    }
                    for (RoleItem role : roles) {
                        if (role.name.equalsIgnoreCase(name)) {
                            input.setError("This role already exists");
                            return;
                        }
                    }
                    createRoleButton.setEnabled(false);
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name);
                    data.put("createdBy", userId);
                    data.put("createdAt", FieldValue.serverTimestamp());
                    rolesRef().add(data)
                            .addOnSuccessListener(ref -> {
                                dialog.dismiss();
                                createRoleButton.setEnabled(true);
                                Toast.makeText(this, "Role created", Toast.LENGTH_SHORT).show();
                                loadRoles();
                            })
                            .addOnFailureListener(e -> {
                                createRoleButton.setEnabled(true);
                                showError("Creating role", e);
                            });
                }));
        dialog.show();
    }

    private void showAssignRolesDialog(MemberItem member) {
        if (roles.isEmpty()) {
            Toast.makeText(this, "Create a role first", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] names = new String[roles.size()];
        boolean[] checked = new boolean[roles.size()];
        Set<String> assigned = new HashSet<>(member.roleIds);
        for (int i = 0; i < roles.size(); i++) {
            names[i] = roles.get(i).name;
            checked[i] = assigned.contains(roles.get(i).id);
        }

        new AlertDialog.Builder(this)
                .setTitle("Assign roles: " + member.displayName)
                .setMultiChoiceItems(names, checked, (dialog, index, selected) ->
                        checked[index] = selected)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    List<String> selectedIds = new ArrayList<>();
                    for (int i = 0; i < roles.size(); i++) {
                        if (checked[i]) selectedIds.add(roles.get(i).id);
                    }
                    // update() preserves the existing role:"employee"/"owner" field.
                    membersRef().document(member.uid).update("roleIds", selectedIds)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Roles updated", Toast.LENGTH_SHORT).show();
                                loadMembers();
                            })
                            .addOnFailureListener(e -> showError("Assigning roles", e));
                })
                .show();
    }

    private void showError(String operation, Exception e) {
        Toast.makeText(this, operation + " failed: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
    }

    private static String abbreviated(String id) {
        if (id == null) return "unknown";
        return id.substring(0, Math.min(8, id.length()));
    }
}
