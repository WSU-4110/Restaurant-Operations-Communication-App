package com.example.stafflink;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ManageRolesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String organizationId;
    private String ownerId;
    private String userId;
    private boolean currentUserIsOwner;

    private LinearLayout rolesContainer;
    private LinearLayout membersContainer;
    private TextView statusText;
    private Button createRoleButton;

    private final List<RoleItem> roles = new ArrayList<>();
    private final List<MemberItem> members = new ArrayList<>();

    private static class RoleItem {
        final String id;
        final String name;
        final boolean viewChat;
        final boolean sendChat;

        RoleItem(String id, String name, boolean viewChat, boolean sendChat) {
            this.id = id;
            this.name = name;
            this.viewChat = viewChat;
            this.sendChat = sendChat;
        }
    }

    private static class MemberItem {
        final String uid;
        final String membershipType;
        final List<String> roleIds;
        final String displayName;

        MemberItem(String uid, String membershipType, List<String> roleIds,
                   String displayName) {
            this.uid = uid;
            this.membershipType = membershipType;
            this.roleIds = roleIds;
            this.displayName = displayName;
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
        createRoleButton.setOnClickListener(v -> showRoleDialog(null));

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db != null) verifyAdminThenLoad();
    }

    private void verifyAdminThenLoad() {
        statusText.setText("Checking organization permissions...");
        createRoleButton.setEnabled(false);
        db.collection("organizations").document(organizationId).get()
                .addOnSuccessListener(organization -> {
                    if (!organization.exists()) {
                        denyAccess("Organization not found");
                        return;
                    }
                    ownerId = organization.getString("ownerId");
                    currentUserIsOwner = userId.equals(ownerId);
                    if (currentUserIsOwner) {
                        loadForAdmin();
                        return;
                    }
                    membersRef().document(userId).get()
                            .addOnSuccessListener(myMembership -> {
                                if (myMembership.exists()
                                        && "manager".equals(myMembership.getString("role"))) {
                                    loadForAdmin();
                                } else {
                                    denyAccess("Only owners and managers can manage roles");
                                }
                            })
                            .addOnFailureListener(e -> denyAccess("Permission check failed: " + e.getMessage()));
                })
                .addOnFailureListener(e -> denyAccess("Organization lookup failed: " + e.getMessage()));
    }

    private void loadForAdmin() {
        createRoleButton.setEnabled(true);
        statusText.setText(currentUserIsOwner
                ? "Tap a role to edit it, or a member to assign roles and manager access."
                : "Tap a role to edit it, or a member to assign job roles.");
        loadRoles();
        loadMembers();
    }

    private void denyAccess(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
        finish();
    }

    private CollectionReference rolesRef() {
        return db.collection("organizations").document(organizationId).collection("roles");
    }

    private CollectionReference membersRef() {
        return db.collection("organizations").document(organizationId).collection("members");
    }

    private void loadRoles() {
        rolesRef().get().addOnSuccessListener(snapshot -> {
            roles.clear();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                if (Boolean.TRUE.equals(doc.getBoolean("archived"))) continue;
                String name = doc.getString("name");
                if (name == null || name.trim().isEmpty()) continue;

                // Older role records have no permissions; preserve previous behavior.
                Object rawPermissions = doc.get("permissions");
                Map<?, ?> permissions = rawPermissions instanceof Map
                        ? (Map<?, ?>) rawPermissions : null;
                boolean view = readPermission(permissions, "viewChat", true);
                boolean send = readPermission(permissions, "sendChat", true);
                roles.add(new RoleItem(doc.getId(), name, view, send));
            }
            Collections.sort(roles, (a, b) -> a.name.compareToIgnoreCase(b.name));
            renderRoles();
            renderMembers();
        }).addOnFailureListener(e -> showError("Loading roles", e));
    }

    private boolean readPermission(Map<?, ?> values, String key, boolean fallback) {
        if (values == null) return fallback;
        Object value = values.get(key);
        return value instanceof Boolean ? (Boolean) value : fallback;
    }

    private void loadMembers() {
        membersRef().get().addOnSuccessListener(snapshot -> {
            members.clear();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                List<String> ids = new ArrayList<>();
                Object raw = doc.get("roleIds");
                if (raw instanceof List<?>) {
                    for (Object id : (List<?>) raw) {
                        if (id instanceof String) ids.add((String) id);
                    }
                }
                String name = doc.getString("displayName");
                if (name == null || name.trim().isEmpty()) {
                    String uid = doc.getId();
                    name = "Member " + uid.substring(0, Math.min(uid.length(), 8));
                }
                members.add(new MemberItem(doc.getId(), doc.getString("role"), ids, name));
            }
            Collections.sort(members, (a, b) -> a.displayName.compareToIgnoreCase(b.displayName));
            renderMembers();
        }).addOnFailureListener(e -> showError("Loading members", e));
    }

    private void renderRoles() {
        rolesContainer.removeAllViews();
        if (roles.isEmpty()) {
            addLabel(rolesContainer, "No job roles yet. Create a Server, Bartender, or Host role.");
        }
        for (RoleItem role : roles) {
            Button roleButton = new Button(this);
            roleButton.setAllCaps(false);
            roleButton.setText(role.name + "\n" + (role.viewChat ? "Can view chat" : "Chat disabled")
                    + " · " + (role.sendChat ? "Can send" : "Cannot send") + "\nTap to edit");
            roleButton.setOnClickListener(v -> showRoleDialog(role));
            rolesContainer.addView(roleButton);
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
            boolean isManager = "manager".equals(member.membershipType);
            Button button = new Button(this);
            button.setAllCaps(false);
            button.setText(member.displayName
                    + (isOwner ? " (Owner)" : (isManager ? " (Manager)" : ""))
                    + "\nAssigned roles: " + roleNames(member.roleIds)
                    + (isOwner ? "" : "\nTap to manage"));
            button.setEnabled(!isOwner);
            button.setOnClickListener(v -> showMemberDialog(member));
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

    private void addLabel(LinearLayout container, String value) {
        TextView label = new TextView(this);
        label.setText(value);
        label.setTextSize(16);
        int padding = dp(10);
        label.setPadding(0, padding, 0, padding);
        container.addView(label);
    }

    private int dp(int size) {
        return (int) (size * getResources().getDisplayMetrics().density + 0.5f);
    }

    private ScrollView scrollForm(LinearLayout content) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.addView(content);
        return scroll;
    }

    private LinearLayout formContainer() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(8), dp(20), dp(8));
        return layout;
    }

    private Map<String, Object> permissions(boolean viewChat, boolean sendChat) {
        Map<String, Object> data = new HashMap<>();
        data.put("viewChat", viewChat);
        data.put("sendChat", sendChat);
        return data;
    }

    private boolean duplicateName(String proposedName, String excludingId) {
        if (proposedName.equalsIgnoreCase("owner") || proposedName.equalsIgnoreCase("manager"))
            return true;
        for (RoleItem other : roles) {
            if (!other.id.equals(excludingId) && other.name.equalsIgnoreCase(proposedName))
                return true;
        }
        return false;
    }

    private void showRoleDialog(RoleItem existing) {
        LinearLayout form = formContainer();
        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setHint("Role name (e.g., Server)");
        name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
        if (existing != null) name.setText(existing.name);
        form.addView(name);

        CheckBox viewChat = new CheckBox(this);
        viewChat.setText("Members can view this role's chat");
        viewChat.setChecked(existing == null || existing.viewChat);
        form.addView(viewChat);

        CheckBox sendChat = new CheckBox(this);
        sendChat.setText("Members can send messages in this chat");
        sendChat.setChecked(existing == null || existing.sendChat);
        sendChat.setEnabled(viewChat.isChecked());
        form.addView(sendChat);

        viewChat.setOnCheckedChangeListener((button, checked) -> {
            if (!checked) sendChat.setChecked(false);
            sendChat.setEnabled(checked);
        });

        TextView note = new TextView(this);
        note.setText("Chat restrictions will take effect when the role-specific chat "
                + "screens and Firestore rules are implemented. Managers always have full chat access.");
        note.setPadding(0, dp(12), 0, 0);
        form.addView(note);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Create job role" : "Edit " + existing.name)
                .setView(scrollForm(form))
                .setPositiveButton(existing == null ? "Create" : "Save", null)
                .setNegativeButton("Cancel", null);
        if (existing != null) {
            builder.setNeutralButton("Archive", (dialog, which) -> confirmArchive(existing));
        }

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(unused -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                String newName = name.getText().toString().trim().replaceAll("\\s+", " ");
                if (newName.isEmpty()) {
                    name.setError("Enter a name");
                    return;
                }
                if (duplicateName(newName, existing == null ? "" : existing.id)) {
                    name.setError("Role name already exists or is reserved");
                    return;
                }
                saveButton.setEnabled(false);
                Map<String, Object> newPermissions = permissions(viewChat.isChecked(),
                        viewChat.isChecked() && sendChat.isChecked());
                if (existing == null) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", newName);
                    data.put("createdBy", userId);
                    data.put("createdAt", FieldValue.serverTimestamp());
                    data.put("archived", false);
                    data.put("permissions", newPermissions);
                    rolesRef().add(data)
                            .addOnSuccessListener(reference -> {
                                dialog.dismiss();
                                Toast.makeText(this, "Role created", Toast.LENGTH_SHORT).show();
                                loadRoles();
                            })
                            .addOnFailureListener(e -> {
                                saveButton.setEnabled(true);
                                showError("Creating role", e);
                            });
                } else {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", newName);
                    updates.put("permissions", newPermissions);
                    updates.put("updatedAt", FieldValue.serverTimestamp());
                    rolesRef().document(existing.id).update(updates)
                            .addOnSuccessListener(unused2 -> {
                                dialog.dismiss();
                                Toast.makeText(this, "Role updated", Toast.LENGTH_SHORT).show();
                                loadRoles();
                            })
                            .addOnFailureListener(e -> {
                                saveButton.setEnabled(true);
                                showError("Saving role", e);
                            });
                }
            });
        });
        dialog.show();
    }

    private void confirmArchive(RoleItem role) {
        new AlertDialog.Builder(this)
                .setTitle("Archive " + role.name + "?")
                .setMessage("The role will disappear from active lists and members will lose "
                        + "access to its future chat. Existing role IDs and any chat history "
                        + "will be retained in Firestore.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Archive", (dialog, which) -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("archived", true);
                    updates.put("archivedAt", FieldValue.serverTimestamp());
                    updates.put("updatedAt", FieldValue.serverTimestamp());
                    rolesRef().document(role.id).update(updates)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Role archived", Toast.LENGTH_SHORT).show();
                                loadRoles();
                            })
                            .addOnFailureListener(e -> showError("Archiving role", e));
                }).show();
    }

    private void showMemberDialog(MemberItem member) {
        if (member.uid.equals(ownerId)) return;
        LinearLayout form = formContainer();
        TextView hint = new TextView(this);
        hint.setText("Assign job roles to " + member.displayName);
        hint.setTextSize(16);
        hint.setPadding(0, 0, 0, dp(8));
        form.addView(hint);

        Set<String> selectedIds = new HashSet<>(member.roleIds);
        List<CheckBox> roleBoxes = new ArrayList<>();
        for (RoleItem role : roles) {
            CheckBox choice = new CheckBox(this);
            choice.setText(role.name);
            choice.setTag(role.id);
            choice.setChecked(selectedIds.contains(role.id));
            form.addView(choice);
            roleBoxes.add(choice);
        }
        if (roles.isEmpty()) addLabel(form, "No active job roles yet.");

        CheckBox managerBox = new CheckBox(this);
        managerBox.setText("Manager: access all chats, join code, and role management");
        managerBox.setChecked("manager".equals(member.membershipType));
        managerBox.setEnabled(currentUserIsOwner);
        managerBox.setPadding(0, dp(10), 0, 0);
        form.addView(managerBox);
        if (!currentUserIsOwner) addLabel(form, "Only the organization owner can grant or remove Manager access.");

        new AlertDialog.Builder(this)
                .setTitle("Manage " + member.displayName)
                .setView(scrollForm(form))
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Preserve inactive role IDs; never silently remove archived assignments.
                    Set<String> activeIds = new HashSet<>();
                    for (RoleItem role : roles) activeIds.add(role.id);
                    List<String> updated = new ArrayList<>();
                    for (String id : member.roleIds) {
                        if (!activeIds.contains(id) && !updated.contains(id)) updated.add(id);
                    }
                    for (CheckBox box : roleBoxes) {
                        if (box.isChecked()) updated.add((String) box.getTag());
                    }
                    if (updated.size() > 20) {
                        Toast.makeText(this, "A member can have at most 20 roles", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("roleIds", updated);
                    if (currentUserIsOwner) {
                        updates.put("role", managerBox.isChecked() ? "manager" : "employee");
                    }
                    membersRef().document(member.uid).update(updates)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Member roles saved", Toast.LENGTH_SHORT).show();
                                loadMembers();
                            })
                            .addOnFailureListener(e -> showError("Saving member roles", e));
                }).show();
    }

    private void showError(String action, Exception e) {
        Toast.makeText(this, action + " failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
