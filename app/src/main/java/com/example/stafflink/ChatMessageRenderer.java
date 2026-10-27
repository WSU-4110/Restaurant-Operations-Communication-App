package com.example.stafflink;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class ChatMessageRenderer {

    private ChatMessageRenderer() { }

    public static View create(LinearLayout parent, String senderName,
                              String messageText, Date sentAt) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);

        TextView nameView = view.findViewById(R.id.chatMessageSender);
        TextView textView = view.findViewById(R.id.chatMessageBody);
        TextView timeView = view.findViewById(R.id.chatMessageTime);

        nameView.setText(senderName);
        textView.setText(messageText);

        timeView.setText(sentAt == null
                ? "Sending..."
                : new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                        .format(sentAt));
        return view;
    }
}
