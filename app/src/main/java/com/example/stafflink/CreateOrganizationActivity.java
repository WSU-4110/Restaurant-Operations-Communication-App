package com.example.stafflink;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class CreateOrganizationActivity extends AppCompatActivity {

    private EditText organizationNameInput;
    private Button createOrganizationButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_organization);

        organizationNameInput = findViewById(R.id.organizationNameInput);
        createOrganizationButton = findViewById(R.id.createOrganizationButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        createOrganizationButton.setOnClickListener(v -> {
            createOrganization();
        });
    }

    private String generateJoinCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }

    private void createOrganization() {
        String organizationName =
                organizationNameInput.getText().toString().trim();

        if (organizationName.isEmpty()) {
            organizationNameInput.setError("Enter an organization name");
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerId = currentUser.getUid();

        DocumentReference organizationRef =
                db.collection("organizations").document();

        DocumentReference memberRef =
                organizationRef.collection("members").document(ownerId);

        String joinCode = generateJoinCode();

        DocumentReference joinCodeRef =
                db.collection("joinCodes").document(joinCode);

        Map<String, Object> organization = new HashMap<>();
        organization.put("name", organizationName);
        organization.put("ownerId", ownerId);
        organization.put("createdAt", FieldValue.serverTimestamp());
        organization.put("joinCode", joinCode);

        Map<String, Object> member = new HashMap<>();
        member.put("role", "owner");
        member.put("joinedAt", FieldValue.serverTimestamp());

        Map<String, Object> joinCodeData = new HashMap<>();
        joinCodeData.put("organizationId", organizationRef.getId());
        joinCodeData.put("createdAt", FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();
        batch.set(organizationRef, organization);
        batch.set(memberRef, member);
        batch.set(joinCodeRef, joinCodeData);

        createOrganizationButton.setEnabled(false);

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Organization created successfully!",
                            Toast.LENGTH_SHORT).show();

                    finish();
                })
                .addOnFailureListener(e -> {
                    createOrganizationButton.setEnabled(true);

                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
