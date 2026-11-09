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


public class OrganizationsActivity extends AppCompatActivity {

    private OrganizationFacade organizationFacade;

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

        organizationFacade = new OrganizationFacade();

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
                    organizationFacade.signOut();

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
        organizationsContainer.removeAllViews();
        noOrganizationsText.setVisibility(View.GONE);
        organizationHelpText.setVisibility(View.GONE);
        organizationsScrollView.setVisibility(View.GONE);

        organizationFacade.loadOrganizations(
                new OrganizationFacade.LoadOrganizationsCallback() {
                    @Override
                    public void onOrganizationLoaded(
                            String organizationId,
                            String organizationName) {

                        addOrganizationToList(
                                organizationId,
                                organizationName
                        );
                    }

                    @Override
                    public void onComplete() {
                        updateOrganizationVisibility();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(
                                OrganizationsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
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
