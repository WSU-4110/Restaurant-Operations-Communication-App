package com.example.stafflink;

import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TaskCompletionActivity extends AppCompatActivity {

    private LinearLayout taskContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_completion);

        taskContainer = findViewById(R.id.taskContainer);

        addTask(new Task("Clean dining tables"));
        addTask(new Task("Restock supplies"));
        addTask(new Task("Check kitchen equipment"));
    }

    private void addTask(Task task) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(16, 16, 16, 24);

        TextView title = new TextView(this);
        title.setText(task.getTitle());
        title.setTextSize(18);
        title.setTextColor(Color.BLACK);

        TextView status = new TextView(this);
        status.setText(task.getStatus());

        Button completeButton = new Button(this);
        completeButton.setText("Complete");

        completeButton.setOnClickListener(v -> {
            task.markCompleted();
            status.setText(task.getStatus());
            completeButton.setEnabled(false);
            completeButton.setText("Completed");
        });

        row.addView(title);
        row.addView(status);
        row.addView(completeButton);

        taskContainer.addView(row);
    }
}
