package com.example.stafflink;

import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizationsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private LinearLayout organizationsContainer;
    private ScrollView organizationsScrollView;
    private TextView noOrganizationsText;
    private TextView organizationHelpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizations);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        organizationsContainer = findViewById(R.id.organizationsContainer);
        organizationsScrollView = findViewById(R.id.organizationsScrollView);
        noOrganizationsText = findViewById(R.id.noOrganizationsText);
        organizationHelpText = findViewById(R.id.organizationHelpText);

        Button logOutButton = findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(v -> confirmLogOut());

        Button addOrganizationButton = findViewById(R.id.addOrganizationButton);

        addOrganizationButton.setOnClickListener(v -> {
            String[] options = {"Create Organization", "Join Organization"};

            new AlertDialog.Builder(OrganizationsActivity.this)
                    .setTitle("Add Organization")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(
                                    OrganizationsActivity.this,
                                    CreateOrganizationActivity.class
                            );
                            startActivity(intent);
                        }
                        else if (which == 1) {
                            Intent intent = new Intent(
                                    OrganizationsActivity.this,
                                    JoinOrganizationActivity.class
                            );
                            startActivity(intent);
                        }
                    })
                    .show();
        });
    }

    private void confirmLogOut() {
        new AlertDialog.Builder(this)
                .setTitle("Log out of StaffLink?")
                .setMessage("You will need to log in again to access your organizations.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log Out", (dialog, which) -> {
                    auth.signOut();

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrganizations();
    }


    private void loadOrganizations() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        organizationsContainer.removeAllViews();
        noOrganizationsText.setVisibility(View.GONE);
        organizationHelpText.setVisibility(View.GONE);
        organizationsScrollView.setVisibility(View.GONE);

        String userId = currentUser.getUid();

        db.collection("organizations")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        addOrganizationToList(
                                document.getId(),
                                document.getString("name")
                        );
                    }

                    db.collection("users")
                            .document(userId)
                            .collection("organizations")
                            .get()
                            .addOnSuccessListener(joinedSnapshot -> {

                                if (joinedSnapshot.isEmpty()) {
                                    updateOrganizationVisibility();
                                    return;
                                }

                                final int[] remaining = {
                                        joinedSnapshot.size()
                                };

                                for (DocumentSnapshot membership :
                                        joinedSnapshot.getDocuments()) {

                                    String organizationId = membership.getId();

                                    db.collection("organizations")
                                            .document(organizationId)
                                            .get()
                                            .addOnSuccessListener(document -> {
                                                if (document.exists()) {
                                                    addOrganizationToList(
                                                            organizationId,
                                                            document.getString("name")
                                                    );
                                                }

                                                remaining[0]--;
                                                if (remaining[0] == 0) {
                                                    updateOrganizationVisibility();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                remaining[0]--;
                                                if (remaining[0] == 0) {
                                                    updateOrganizationVisibility();
                                                }
                                                Toast.makeText(this,
                                                        "Error loading joined organization",
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                updateOrganizationVisibility();
                                Toast.makeText(this,
                                        "Error loading memberships: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    updateOrganizationVisibility();
                    Toast.makeText(this,
                            "Error loading organizations: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void addOrganizationToList(String organizationId, String organizationName) {
        if (organizationName == null) {
            return;
        }

        Button organizationButton = new Button(OrganizationsActivity.this);
        organizationButton.setText(organizationName);
        organizationButton.setAllCaps(false);
        organizationButton.setTextSize(20);
        organizationButton.setBackgroundResource(
                R.drawable.organization_button_background
        );
        organizationButton.setBackgroundTintList(null);
        organizationButton.setTextColor(getColor(android.R.color.black));
        organizationButton.setMinHeight((int) (70 *
                getResources().getDisplayMetrics().density));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.bottomMargin = (int) (18 *
                getResources().getDisplayMetrics().density);

        organizationButton.setLayoutParams(params);

        organizationButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    OrganizationsActivity.this,
                    OrganizationHomeActivity.class
            );

            intent.putExtra("organizationId", organizationId);
            intent.putExtra("organizationName", organizationName);

            startActivity(intent);
        });

        organizationsContainer.addView(organizationButton);
    }

    private void updateOrganizationVisibility() {
        boolean empty = organizationsContainer.getChildCount() == 0;

        noOrganizationsText.setVisibility(empty ? View.VISIBLE : View.GONE);
        organizationHelpText.setVisibility(empty ? View.VISIBLE : View.GONE);
        organizationsScrollView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
