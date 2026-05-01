package com.example.reloop.ui.message.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reloop.R;
import com.example.reloop.models.Message;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<Message> messages;
    private String currentUserId;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.getSenderId().equals(currentUserId) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view, timeFormat);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view, timeFormat);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText, timeText;
        private final SimpleDateFormat timeFormat;

        SentMessageViewHolder(@NonNull View itemView, SimpleDateFormat format) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.time_text);
            this.timeFormat = format;
        }

        void bind(Message message) {
            messageText.setText(message.getContent());
            // Safe binding to prevent null timestamp crash
            if (message.getTimestamp() != null) {
                timeText.setText(timeFormat.format(message.getTimestamp()));
            } else {
                timeText.setText("--:--");
            }
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText, timeText, senderName;
        private final SimpleDateFormat timeFormat;

        ReceivedMessageViewHolder(@NonNull View itemView, SimpleDateFormat format) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.time_text);
            senderName = itemView.findViewById(R.id.sender_name);
            this.timeFormat = format;
        }

        void bind(Message message) {
            messageText.setText(message.getContent());
            senderName.setText(message.getSenderName() != null ? message.getSenderName() : "User");
            // Safe binding to prevent null timestamp crash
            if (message.getTimestamp() != null) {
                timeText.setText(timeFormat.format(message.getTimestamp()));
            } else {
                timeText.setText("--:--");
            }
        }
    }
}