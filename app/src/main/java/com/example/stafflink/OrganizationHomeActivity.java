
package com.example.stafflink;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;

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

        // Only the organization owner will see this button after loadJoinCode().
        Button manageRolesButton = findViewById(R.id.manageRolesButton);
        manageRolesButton.setVisibility(View.GONE);
        manageRolesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageRolesActivity.class);
            intent.putExtra("organizationId", organizationId);
            startActivity(intent);
        });

        loadJoinCode();

        Button generalButton = findViewById(R.id.generalButton);

        generalButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    OrganizationHomeActivity.this,
                    GeneralChatActivity.class
            );

            intent.putExtra("organizationId", organizationId);
            intent.putExtra("organizationName", organizationName);

            startActivity(intent);
        });

    }

    private void loadJoinCode() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        db.collection("organizations")
                .document(organizationId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        return;
                    }

                    String ownerId = document.getString("ownerId");
                    String joinCode = document.getString("joinCode");
                    boolean isOwner = currentUser.getUid().equals(ownerId);
                    findViewById(R.id.manageRolesButton)
                            .setVisibility(isOwner ? View.VISIBLE : View.GONE);

                    if (isOwner && joinCode != null) {

                        LinearLayout joinCodeSection =
                                findViewById(R.id.joinCodeSection);

                        TextView joinCodeText =
                                findViewById(R.id.joinCodeText);

                        Button copyJoinCodeButton =
                                findViewById(R.id.copyJoinCodeButton);

                        joinCodeText.setText(joinCode);
                        joinCodeSection.setVisibility(View.VISIBLE);

                        copyJoinCodeButton.setOnClickListener(v -> {

                            ClipboardManager clipboard =
                                    (ClipboardManager) getSystemService(
                                            Context.CLIPBOARD_SERVICE);

                            ClipData clip = ClipData.newPlainText(
                                    "StaffLink Join Code", joinCode);

                            clipboard.setPrimaryClip(clip);

                            Toast.makeText(this,
                                    "Join code copied!",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Error loading organization: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
