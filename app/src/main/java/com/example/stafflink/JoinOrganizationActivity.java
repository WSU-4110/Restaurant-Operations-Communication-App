package com.example.stafflink;

import android.os.Bundle;
import android.text.InputFilter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class JoinOrganizationActivity extends AppCompatActivity {

    private EditText joinCodeInput;
    private Button joinOrganizationButton;

    private OrganizationFacade organizationFacade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_organization);

        joinCodeInput = findViewById(R.id.joinCodeInput);
        joinOrganizationButton = findViewById(R.id.joinOrganizationButton);

        organizationFacade = new OrganizationFacade();

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

        joinOrganizationButton.setEnabled(false);

        organizationFacade.joinOrganization(
                joinCode,
                new OrganizationFacade.JoinOrganizationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(JoinOrganizationActivity.this,
                                "Joined organization successfully!",
                                Toast.LENGTH_SHORT).show();

                        finish();
                    }

                    @Override
                    public void onFailure(String message) {
                        showError(message);
                    }
                });
    }

    private void showError(String message) {
        joinOrganizationButton.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
