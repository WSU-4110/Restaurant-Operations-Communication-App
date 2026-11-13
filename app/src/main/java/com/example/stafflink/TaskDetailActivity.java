package com.example.stafflink;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class TaskDetailActivity extends AppCompatActivity {

    private DocumentReference taskReference;
    private ListenerRegistration taskListener;

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

    private void showTask(Task task) {
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
            status.setText("Open");
        }

        created.setText("Created by " + TaskListActivity.nameOrMember(task.getCreatedByName())
                + " on " + TaskListActivity.formatDate(task.getCreatedAt()));
        description.setText(task.getDescription());
    }
}