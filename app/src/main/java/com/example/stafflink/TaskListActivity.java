package com.example.stafflink;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskListActivity extends AppCompatActivity {

    private static final long COMPLETED_VISIBLE_MILLIS = 24L * 60 * 60 * 1000;

    private FirebaseFirestore db;
    private String organizationId;
    private String roleId;
    private ListenerRegistration taskListener;

    private LinearLayout openTasksContainer;
    private LinearLayout completedTasksContainer;
    private TextView completedTasksHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        organizationId = getIntent().getStringExtra("organizationId");
        roleId = getIntent().getStringExtra("roleId");
        String roleName = getIntent().getStringExtra("roleName");

        if (organizationId == null || roleId == null) {
            finish();
            return;
        }

        TextView title = findViewById(R.id.taskListTitle);
        title.setText(roleName == null ? "Tasks" : roleName + " Tasks");

        openTasksContainer = findViewById(R.id.openTasksContainer);
        completedTasksContainer = findViewById(R.id.completedTasksContainer);
        completedTasksHeader = findViewById(R.id.completedTasksHeader);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (roleId == null) {
            return;
        }

        taskListener = db.collection("organizations")
                .document(organizationId)
                .collection("roles")
                .document(roleId)
                .collection("tasks")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Could not load tasks: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshot != null) {
                        renderTasks(snapshot);
                    }
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

    private void renderTasks(QuerySnapshot snapshot) {
        List<Task> openTasks = new ArrayList<>();
        List<Task> completedTasks = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            Task task = Task.fromDocument(document);
            if (task.isCompleted()) {
                Date completedAt = task.getCompletedAt();
                if (completedAt != null && now - completedAt.getTime() < COMPLETED_VISIBLE_MILLIS) {
                    completedTasks.add(task);
                }
            } else {
                openTasks.add(task);
            }
        }

        openTasks.sort(Comparator.comparingInt(Task::getPriorityRank)
                .thenComparing(Task::getCreatedAt,
                        Comparator.nullsFirst(Comparator.reverseOrder())));
        completedTasks.sort(Comparator.comparing(Task::getCompletedAt,
                Comparator.nullsFirst(Comparator.reverseOrder())));

        openTasksContainer.removeAllViews();
        if (openTasks.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No open tasks.");
            openTasksContainer.addView(empty);
        }
        for (Task task : openTasks) {
            openTasksContainer.addView(createTaskView(task, openTasksContainer));
        }

        completedTasksContainer.removeAllViews();
        completedTasksHeader.setVisibility(completedTasks.isEmpty() ? View.GONE : View.VISIBLE);
        for (Task task : completedTasks) {
            completedTasksContainer.addView(createTaskView(task, completedTasksContainer));
        }
    }

    private View createTaskView(Task task, LinearLayout parent) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_task, parent, false);

        TextView titleView = view.findViewById(R.id.taskItemTitle);
        TextView priorityView = view.findViewById(R.id.taskItemPriority);
        TextView infoView = view.findViewById(R.id.taskItemInfo);

        titleView.setText(task.getTitle());
        priorityView.setText("Priority: " + task.getPriority());

        if (task.isCompleted()) {
            infoView.setText("Completed by " + nameOrMember(task.getCompletedByName())
                    + " on " + formatDate(task.getCompletedAt()));
        } else {
            infoView.setText("Created by " + nameOrMember(task.getCreatedByName())
                    + " on " + formatDate(task.getCreatedAt()));
        }

        view.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskDetailActivity.class);
            intent.putExtra("organizationId", organizationId);
            intent.putExtra("roleId", roleId);
            intent.putExtra("taskId", task.getId());
            startActivity(intent);
        });

        return view;
    }

    static String nameOrMember(String name) {
        return name == null || name.trim().isEmpty() ? "Member" : name;
    }

    static String formatDate(Date date) {
        return date == null
                ? "..."
                : new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(date);
    }
}