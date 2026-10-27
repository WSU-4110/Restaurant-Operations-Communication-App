package com.example.stafflink;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
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
    private FirebaseFirestore db;
    private String organizationId;
    private String resolvedSenderName;
    private QuerySnapshot lastMessagesSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_chat);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        organizationId = getIntent().getStringExtra("organizationId");
        String organizationName = getIntent().getStringExtra("organizationName");

        if (currentUser == null || organizationId == null || organizationId.isEmpty()) {
            Toast.makeText(this, "Unable to open chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messagesContainer = findViewById(R.id.messagesContainer);
        messagesScrollView = findViewById(R.id.messagesScrollView);

        TextView chatTitle = findViewById(R.id.chatTitle);
        chatTitle.setText(organizationName == null
                ? "General Chat"
                : organizationName + " - General Chat");

        messages = db.collection("organizations")
                .document(organizationId)
                .collection("generalMessages");

        sendButton.setEnabled(false);
        sendButton.setOnClickListener(v -> sendMessage());
        resolveSenderName();
        listenForMessages();
    }

    private void resolveSenderName() {
        String uid = currentUser.getUid();

        db.collection("organizations")
                .document(organizationId)
                .collection("members")
                .document(uid)
                .get()
                .addOnSuccessListener(member -> {
                    String name = member.getString("displayName");
                    if (hasName(name)) {
                        setSenderName(name);
                    } else {
                        resolveFromOwnProfile();
                    }
                })
                .addOnFailureListener(error -> resolveFromOwnProfile());
    }

    private void resolveFromOwnProfile() {
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(profile -> {
                    String first = profile.getString("firstName");
                    String last = profile.getString("lastName");
                    String fullName = (
                            (first == null ? "" : first.trim()) + " " +
                            (last == null ? "" : last.trim())
                    ).trim();
                    if (hasName(fullName)) {
                        setSenderName(fullName);
                    } else {
                        resolveFromAuthProfile();
                    }
                })
                .addOnFailureListener(error -> resolveFromAuthProfile());
    }

    private void resolveFromAuthProfile() {
        String authName = currentUser.getDisplayName();
        setSenderName(hasName(authName) ? authName : "Member");
    }

    private boolean hasName(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void setSenderName(String name) {
        resolvedSenderName = name.trim().replaceAll("\\s+", " ");
        sendButton.setEnabled(true);
        if (lastMessagesSnapshot != null) {
            renderMessages(lastMessagesSnapshot);
        }
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty() || resolvedSenderName == null) {
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("text", text);
        message.put("senderId", currentUser.getUid());
        message.put("senderName", resolvedSenderName);
        message.put("timestamp", FieldValue.serverTimestamp());

        sendButton.setEnabled(false);
        messages.add(message)
                .addOnSuccessListener(reference -> {
                    messageInput.setText("");
                    sendButton.setEnabled(true);
                })
                .addOnFailureListener(error -> {
                    sendButton.setEnabled(true);
                    Toast.makeText(this,
                            "Error sending: " + error.getMessage(),
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
                    if (snapshot == null) return;

                    lastMessagesSnapshot = snapshot;
                    renderMessages(snapshot);
                });
    }

    private void renderMessages(QuerySnapshot snapshot) {
        messagesContainer.removeAllViews();
        for (QueryDocumentSnapshot document : snapshot) {
            String text = document.getString("text");
            if (text == null) continue;

            String sender = document.getString("senderName");
            String senderId = document.getString("senderId");

            if (resolvedSenderName != null &&
                    currentUser.getUid().equals(senderId)) {
                sender = resolvedSenderName;
            }
            if (!hasName(sender)) sender = "Member";

            Date sentAt = document.getDate(
                    "timestamp", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);

            View messageView = ChatMessageRenderer.create(
                    messagesContainer, sender, text, sentAt);
            messagesContainer.addView(messageView);
        }
        messagesScrollView.post(() ->
                messagesScrollView.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    protected void onDestroy() {
        if (messageListener != null) messageListener.remove();
        super.onDestroy();
    }
}
