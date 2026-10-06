package com.example.stafflink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    EditText firstNameInput;
    EditText lastNameInput;
    EditText emailInput;
    EditText passwordInput;
    EditText confirmPasswordInput;
    Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.signupEmailInput);
        passwordInput = findViewById(R.id.signupPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        createAccountButton = findViewById(R.id.createAccountButton);

        createAccountButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String firstName = firstNameInput.getText().toString().trim();
            String lastName = lastNameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();

            if (firstName.isEmpty() || lastName.isEmpty() ||
                    email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(SignUpActivity.this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this,
                        "Passwords do not match",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this,
                                    "Account created successfully",
                                    Toast.LENGTH_SHORT).show();

                            String userId = mAuth.getCurrentUser().getUid();

                            Map<String, Object> user = new HashMap<>();
                            user.put("firstName", firstName);
                            user.put("lastName", lastName);
                            user.put("email", email);

                            db.collection("users")
                                    .document(userId)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SignUpActivity.this,
                                                "Account created successfully",
                                                Toast.LENGTH_SHORT).show();

                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SignUpActivity.this,
                                                "Account created, but profile could not be saved",
                                                Toast.LENGTH_LONG).show();
                                    });
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this,
                                    "Account creation failed: " +
                                            task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
            });


        //takes to login page
        TextView loginText = findViewById(R.id.loginText);
        loginText.setOnClickListener(v -> {
            finish();
        });
    }
}