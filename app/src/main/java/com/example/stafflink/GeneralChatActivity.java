
package com.example.stafflink;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class GeneralChatActivity extends AppCompatActivity {

    private EditText messageInput;
    private Button sendButton;
    private LinearLayout messagesContainer;
    private ScrollView messagesScrollView;
    private CollectionReference messages;
    private ListenerRegistration messageListener;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_chat);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top,
                            bars.right, bars.bottom);
                    return insets;
                });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String organizationId =
                getIntent().getStringExtra("organizationId");
        String organizationName =
                getIntent().getStringExtra("organizationName");

        if (currentUser == null ||
                organizationId == null ||
                organizationId.isEmpty()) {
            Toast.makeText(this,
                    "Unable to open chat",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messagesContainer = findViewById(R.id.messagesContainer);
        messagesScrollView = findViewById(R.id.messagesScrollView);

        TextView chatTitle = findViewById(R.id.chatTitle);
        chatTitle.setText(
                organizationName == null
                        ? "General Chat"
                        : organizationName + " - General Chat"
        );

        messages = FirebaseFirestore.getInstance()
                .collection("organizations")
                .document(organizationId)
                .collection("generalMessages");

        sendButton.setOnClickListener(v -> sendMessage());

        listenForMessages();
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();

        if (text.isEmpty()) {
            return;
        }

        String senderName = currentUser.getDisplayName();

        if (senderName == null || senderName.isEmpty()) {
            senderName = currentUser.getEmail();
        }

        if (senderName == null) {
            senderName = "Member";
        }

        Map<String, Object> message = new HashMap<>();
        message.put("text", text);
        message.put("senderId", currentUser.getUid());
        message.put("senderName", senderName);
        message.put("timestamp", FieldValue.serverTimestamp());

        sendButton.setEnabled(false);

        messages.add(message)
                .addOnSuccessListener(reference -> {
                    messageInput.setText("");
                    sendButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    sendButton.setEnabled(true);
                    Toast.makeText(this,
                            "Error sending: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void listenForMessages() {
        messageListener = messages
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null) {
                        Toast.makeText(this,
                                "Error loading chat: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snapshot == null) {
                        return;
                    }

                    messagesContainer.removeAllViews();

                    for (QueryDocumentSnapshot document : snapshot) {
                        String text = document.getString("text");
                        String sender = document.getString("senderName");

                        if (text == null) {
                            continue;
                        }

                        if (sender == null) {
                            sender = "Member";
                        }

                        TextView messageView = new TextView(this);
                        messageView.setText(sender + "\n" + text);
                        messageView.setTextSize(16);
                        messageView.setTextColor(Color.BLACK);
                        messageView.setBackgroundColor(Color.WHITE);

                        int padding = dp(12);
                        messageView.setPadding(
                                padding, padding, padding, padding);

                        LinearLayout.LayoutParams params =
                                new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );

                        params.bottomMargin = dp(8);

                        messagesContainer.addView(messageView, params);
                    }

                    messagesScrollView.post(() ->
                            messagesScrollView.fullScroll(View.FOCUS_DOWN));
                });
    }

    private int dp(int value) {
        return Math.round(value *
                getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        if (messageListener != null) {
            messageListener.remove();
        }
        super.onDestroy();
    }
}
