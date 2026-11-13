package com.example.stafflink;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Map;

public class TaskDetailActivity extends AppCompatActivity {

    private DocumentReference taskReference;
    private ListenerRegistration taskListener;
    private Task currentTask;
    private Button completeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String organizationId = getIntent().getStringExtra("organizationId");
        String roleId = getIntent().getStringExtra("roleId");
        String taskId = getIntent().getStringExtra("taskId");

        if (organizationId == null || roleId == null || taskId == null) {
            finish();
            return;
        }

        taskReference = FirebaseFirestore.getInstance()
                .collection("organizations")
                .document(organizationId)
                .collection("roles")
                .document(roleId)
                .collection("tasks")
                .document(taskId);

        completeButton = findViewById(R.id.taskCompleteButton);
        completeButton.setOnClickListener(v -> completeTask());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (taskReference == null) {
            return;
        }

        taskListener = taskReference.addSnapshotListener((document, error) -> {
            if (error != null || document == null || !document.exists()) {
                Toast.makeText(this, "This task is no longer available", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            showTask(Task.fromDocument(document));
        });
    }

    @Override
    protected void onStop() {
        if (taskListener != null) {
            taskListener.remove();
            taskListener = null;
        }
        super.onStop();
    }

    private void completeTask() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (currentTask == null || user == null) {
            return;
        }

        completeButton.setEnabled(false);
        ProfileName.load(FirebaseFirestore.getInstance(), user.getUid(), name -> {
            Map<String, Object> updates = currentTask.markCompleted(user.getUid(), name);
            if (updates == null) {
                return;
            }
            taskReference.update(updates)
                    .addOnFailureListener(e -> {
                        completeButton.setEnabled(true);
                        Toast.makeText(this, "Could not complete task: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }

    private void showTask(Task task) {
        currentTask = task;
        TextView title = findViewById(R.id.taskDetailTitle);
        TextView priority = findViewById(R.id.taskDetailPriority);
        TextView status = findViewById(R.id.taskDetailStatus);
        TextView created = findViewById(R.id.taskDetailCreated);
        TextView description = findViewById(R.id.taskDetailDescription);

        title.setText(task.getTitle());
        priority.setText("Priority: " + task.getPriority());

        if (task.isCompleted()) {
            status.setText("Completed by " + TaskListActivity.nameOrMember(task.getCompletedByName())
                    + " on " + TaskListActivity.formatDate(task.getCompletedAt()));
        } else {
            status.setText(task.getState().getStatus());
        }

        completeButton.setVisibility(task.isCompleted() ? View.GONE : View.VISIBLE);
        completeButton.setEnabled(true);

        created.setText("Created by " + TaskListActivity.nameOrMember(task.getCreatedByName())
                + " on " + TaskListActivity.formatDate(task.getCreatedAt()));
        description.setText(task.getDescription());
    }
}