package com.example.stafflink;

import android.os.Bundle;
import android.text.InputFilter;
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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class JoinOrganizationActivity extends AppCompatActivity {

    private EditText joinCodeInput;
    private Button joinOrganizationButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_organization);

        joinCodeInput = findViewById(R.id.joinCodeInput);
        joinOrganizationButton = findViewById(R.id.joinOrganizationButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        InputFilter uppercaseFilter = (source, start, end, dest, dstart, dend) ->
                source.subSequence(start, end).toString()
                        .toUpperCase(Locale.ROOT);

        joinCodeInput.setFilters(new InputFilter[]{
                uppercaseFilter,
                new InputFilter.LengthFilter(8)
        });

        joinOrganizationButton.setOnClickListener(v -> joinOrganization());
    }

    private void joinOrganization() {
        String joinCode = joinCodeInput.getText().toString()
                .trim().toUpperCase(Locale.ROOT);

        if (!joinCode.matches("[A-Z0-9]{8}")) {
            joinCodeInput.setError("Enter a valid 8-character code");
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        joinOrganizationButton.setEnabled(false);

        db.collection("joinCodes").document(joinCode)
                .get()
                .addOnSuccessListener(codeDocument -> {
                    if (!codeDocument.exists()) {
                        showError("Invalid join code");
                        return;
                    }

                    String organizationId =
                            codeDocument.getString("organizationId");

                    if (organizationId == null) {
                        showError("Invalid organization");
                        return;
                    }

                    String userId = currentUser.getUid();

                    DocumentReference memberRef = db
                            .collection("organizations")
                            .document(organizationId)
                            .collection("members")
                            .document(userId);

                    DocumentReference userOrganizationRef = db
                            .collection("users")
                            .document(userId)
                            .collection("organizations")
                            .document(organizationId);

                    memberRef.get()
                            .addOnSuccessListener(memberDocument -> {
                                if (memberDocument.exists()) {
                                    showError("You are already a member");
                                    return;
                                }

                                Map<String, Object> member = new HashMap<>();
                                member.put("role", "employee");
                                member.put("joinCode", joinCode);
                                member.put("joinedAt",
                                        FieldValue.serverTimestamp());

                                Map<String, Object> userOrganization =
                                        new HashMap<>();
                                userOrganization.put("role", "employee");
                                userOrganization.put("joinedAt",
                                        FieldValue.serverTimestamp());

                                WriteBatch batch = db.batch();
                                batch.set(memberRef, member);
                                batch.set(userOrganizationRef, userOrganization);

                                batch.commit()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this,
                                                    "Joined organization successfully!",
                                                    Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e ->
                                                showError("Error: " + e.getMessage()));
                            })
                            .addOnFailureListener(e ->
                                    showError("Error: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        showError("Error: " + e.getMessage()));
    }

    private void showError(String message) {
        joinOrganizationButton.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
