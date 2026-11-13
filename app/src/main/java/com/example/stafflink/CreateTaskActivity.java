package com.example.stafflink;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;
import java.util.Map;

public class CreateTaskActivity extends AppCompatActivity {
    private Spinner taskPrioritySpinner;
    private Spinner taskRoleSpinner;
    private FirebaseFirestore db;
    private String organizationId;
    private String presetRoleId;
    private final List<String> roleNames = new ArrayList<>();
    private final List<String> roleIds = new ArrayList<>();
    private EditText taskTitleInput;
    private EditText taskDescriptionInput;
    private Button createTaskButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        taskPrioritySpinner = findViewById(R.id.taskPrioritySpinner);

        String[] priorities = {"Low", "Medium", "High"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                priorities
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        taskPrioritySpinner.setAdapter(adapter);
        taskRoleSpinner = findViewById(R.id.taskRoleSpinner);

        db = FirebaseFirestore.getInstance();
        organizationId = getIntent().getStringExtra("organizationId");

        if (organizationId == null || organizationId.trim().isEmpty()) {
            Toast.makeText(this, "Organization not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        presetRoleId = getIntent().getStringExtra("roleId");
        if (presetRoleId != null) {
            findViewById(R.id.taskRoleLabel).setVisibility(View.GONE);
            taskRoleSpinner.setVisibility(View.GONE);
        } else {
            loadRoles();
        }

        taskTitleInput = findViewById(R.id.taskTitleInput);
        taskDescriptionInput = findViewById(R.id.taskDescriptionInput);
        createTaskButton = findViewById(R.id.createTaskButton);

        createTaskButton.setOnClickListener(v -> {

            String title = taskTitleInput.getText().toString().trim();
            String description = taskDescriptionInput.getText().toString().trim();

            if (title.isEmpty()) {
                taskTitleInput.setError("Enter a task title");
                return;
            }

            if (description.isEmpty()) {
                taskDescriptionInput.setError("Enter a task description");
                return;
            }

            String selectedRoleId;
            if (presetRoleId != null) {
                selectedRoleId = presetRoleId;
            } else {
                if (roleIds.isEmpty()) {
                    Toast.makeText(this, "Select a job role", Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedRoleId = roleIds.get(taskRoleSpinner.getSelectedItemPosition());
            }
            String priority = taskPrioritySpinner.getSelectedItem().toString();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser == null) {
                Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
                return;
            }

            String creatorId = currentUser.getUid();

            checkTaskPermission(creatorId, selectedRoleId,
                    title, description, priority);
        });
    }

    private void loadRoles() {
        db.collection("organizations")
                .document(organizationId)
                .collection("roles")
                .get()
                .addOnSuccessListener(snapshot -> {

                    roleNames.clear();
                    roleIds.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {

                        if (Boolean.TRUE.equals(doc.getBoolean("archived"))) {
                            continue;
                        }

                        String name = doc.getString("name");

                        if (name != null && !name.trim().isEmpty()) {
                            roleNames.add(name);
                            roleIds.add(doc.getId());
                        }
                    }

                    ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            roleNames
                    );

                    roleAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                    );

                    taskRoleSpinner.setAdapter(roleAdapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Could not load roles: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void checkTaskPermission(String creatorId, String selectedRoleId,
                                     String title, String description,
                                     String priority) {

        db.collection("organizations")
                .document(organizationId)
                .get()
                .addOnSuccessListener(organization -> {

                    if (!organization.exists()) {
                        Toast.makeText(this, "Organization not found",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (creatorId.equals(organization.getString("ownerId"))) {
                        saveTask(title, description, priority, selectedRoleId, creatorId);
                        return;
                    }

                    db.collection("organizations")
                            .document(organizationId)
                            .collection("members")
                            .document(creatorId)
                            .get()
                            .addOnSuccessListener(member -> {

                                if (!member.exists()) {
                                    Toast.makeText(this, "You are not a member of this organization",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                String role = member.getString("role");

                                if ("manager".equals(role)) {
                                    saveTask(title, description, priority, selectedRoleId, creatorId);
                                    return;
                                }

                                Object rawRoleIds = member.get("roleIds");

                                if (rawRoleIds instanceof List
                                        && ((List<?>) rawRoleIds).contains(selectedRoleId)) {
                                    saveTask(title, description, priority, selectedRoleId, creatorId);
                                } else {
                                    Toast.makeText(this, "You cannot assign tasks to this role",
                                            Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Could not check membership",
                                            Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Could not check organization permissions",
                                Toast.LENGTH_SHORT).show()
                );
    }
    private void saveTask(String title, String description,
                          String priority, String selectedRoleId,
                          String creatorId) {

        db.collection("users")
                .document(creatorId)
                .get()
                .addOnSuccessListener(profile -> {
                    String first = profile.getString("firstName");
                    String last = profile.getString("lastName");
                    String name = ((first == null ? "" : first.trim()) + " "
                            + (last == null ? "" : last.trim())).trim();
                    if (name.isEmpty()) {
                        name = "Member";
                    }
                    if (name.length() > 100) {
                        name = name.substring(0, 100);
                    }
                    writeTask(title, description, priority, selectedRoleId, creatorId, name);
                })
                .addOnFailureListener(e ->
                        writeTask(title, description, priority, selectedRoleId, creatorId, "Member"));
    }

    private void writeTask(String title, String description,
                           String priority, String selectedRoleId,
                           String creatorId, String creatorName) {

        Task task = new Task.Builder()
                .setTitle(title)
                .setDescription(description)
                .setPriority(priority)
                .setAssignedRoleId(selectedRoleId)
                .setCreatedBy(creatorId)
                .setCreatedByName(creatorName)
                .build();

        db.collection("organizations")
                .document(organizationId)
                .collection("roles")
                .document(selectedRoleId)
                .collection("tasks")
                .add(task.toMap())
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Task created successfully!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Could not create task: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
