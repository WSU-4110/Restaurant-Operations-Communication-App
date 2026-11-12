package com.example.stafflink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RoleActivity extends AppCompatActivity {

    private String organizationId;
    private String organizationName;
    private String roleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_role);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        organizationId = getIntent().getStringExtra("organizationId");
        organizationName = getIntent().getStringExtra("organizationName");
        roleId = getIntent().getStringExtra("roleId");
        String roleName = getIntent().getStringExtra("roleName");

        if (organizationId == null || roleId == null) {
            finish();
            return;
        }

        TextView roleTitle = findViewById(R.id.roleTitle);
        roleTitle.setText(roleName);

        Button chatButton = findViewById(R.id.roleChatButton);
        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GeneralChatActivity.class);
            intent.putExtra("organizationId", organizationId);
            intent.putExtra("organizationName", organizationName);
            intent.putExtra("roleId", roleId);
            startActivity(intent);
        });

        Button createTaskButton = findViewById(R.id.roleCreateTaskButton);
        createTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateTaskActivity.class);
            intent.putExtra("organizationId", organizationId);
            intent.putExtra("roleId", roleId);
            startActivity(intent);
        });
    }
}
