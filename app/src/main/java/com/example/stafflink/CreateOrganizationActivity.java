package com.example.stafflink;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



public class CreateOrganizationActivity extends AppCompatActivity {

    private EditText organizationNameInput;
    private Button createOrganizationButton;

    private OrganizationFacade organizationFacade;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_organization);

        organizationNameInput = findViewById(R.id.organizationNameInput);
        createOrganizationButton = findViewById(R.id.createOrganizationButton);

        organizationFacade = new OrganizationFacade();

        createOrganizationButton.setOnClickListener(v -> {
            createOrganization();
        });
    }

    private void createOrganization() {
        String organizationName =
                organizationNameInput.getText().toString().trim();

        if (organizationName.isEmpty()) {
            organizationNameInput.setError("Enter an organization name");
            return;
        }

        createOrganizationButton.setEnabled(false);

        organizationFacade.createOrganization(
                organizationName,
                new OrganizationFacade.CreateOrganizationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(
                                CreateOrganizationActivity.this,
                                "Organization created successfully!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        createOrganizationButton.setEnabled(true);

                        Toast.makeText(CreateOrganizationActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
