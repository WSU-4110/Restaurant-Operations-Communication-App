package com.example.stafflink;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class TaskDetailActivity extends AppCompatActivity {

    private DocumentReference taskReference;
    private ListenerRegistration taskListener;
    private ListenerRegistration commentsListener;
    private Task currentTask;
    private Button completeButton;
    private Button postCommentButton;
    private EditText commentInput;
    private LinearLayout commentsContainer;

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

        postCommentButton = findViewById(R.id.taskPostCommentButton);
        commentInput = findViewById(R.id.taskCommentInput);
        commentsContainer = findViewById(R.id.taskCommentsContainer);
        postCommentButton.setOnClickListener(v -> postComment());
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

        loadComments();
    }

    @Override
    protected void onStop() {
        if (taskListener != null) {
            taskListener.remove();
            taskListener = null;
        }
        if (commentsListener != null) {
            commentsListener.remove();
            commentsListener = null;
        }
        super.onStop();
    }

    private void loadComments() {
        commentsListener = taskReference.collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    commentsContainer.removeAllViews();
                    if (snapshots == null) {
                        return;
                    }

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String name = doc.getString("authorName");
                        String message = doc.getString("text");
                        if (name == null) {
                            name = "Unknown user";
                        }
                        if (message == null) {
                            message = "";
                        }

                        TextView commentView = new TextView(this);
                        commentView.setText(name + "\n" + message);
                        commentView.setTextSize(16);
                        commentView.setPadding(12, 12, 12, 20);
                        commentsContainer.addView(commentView);
                    }
                });
    }

    private void postComment() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = commentInput.getText().toString().trim();
        if (message.isEmpty()) {
            commentInput.setError("Enter a comment");
            return;
        }

        postCommentButton.setEnabled(false);
        ProfileName.load(FirebaseFirestore.getInstance(), user.getUid(), name -> {
            Map<String, Object> comment = new HashMap<>();
            comment.put("userId", user.getUid());
            comment.put("authorName", name == null ? "User" : name);
            comment.put("text", message);
            comment.put("createdAt", FieldValue.serverTimestamp());

            taskReference.collection("comments")
                    .add(comment)
                    .addOnSuccessListener(ref -> {
                        commentInput.setText("");
                        postCommentButton.setEnabled(true);
                        Toast.makeText(this, "Comment posted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        postCommentButton.setEnabled(true);
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
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
