package com.example.reloop.ui.message.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reloop.R;
import com.example.reloop.models.Conversation;
import com.example.reloop.models.Message;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {
    private List<Conversation> conversations;
    private OnConversationClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ConversationAdapter(List<Conversation> conversations, OnConversationClickListener listener) {
        this.conversations = conversations != null ? conversations : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.bind(conversation);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public void updateConversations(List<Conversation> newConversations) {
        this.conversations = newConversations != null ? newConversations : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addConversation(Conversation conversation) {
        if (conversation != null) {
            this.conversations.add(0, conversation);
            notifyItemInserted(0);
        }
    }

    public void updateConversation(Conversation updatedConversation) {
        for (int i = 0; i < conversations.size(); i++) {
            if (conversations.get(i).getId().equals(updatedConversation.getId())) {
                conversations.set(i, updatedConversation);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations != null ? conversations : new ArrayList<>();
        notifyDataSetChanged();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private TextView userName;
        private TextView lastMessage;
        private TextView timestamp;
        private TextView unreadCount;

        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            timestamp = itemView.findViewById(R.id.timestamp);
            unreadCount = itemView.findViewById(R.id.unread_count);
        }

        void bind(Conversation conversation) {
            // Set participant name
            userName.setText(conversation.getOtherParticipantName() != null ?
                    conversation.getOtherParticipantName() : "Unknown User");

            // Set last message
            Message lastMsg = conversation.getLastMessage();
            if (lastMsg != null) {
                lastMessage.setText(lastMsg.getContent());

                if (lastMsg.getTimestamp() != null) {
                    if (isToday(lastMsg.getTimestamp().getTime())) {
                        timestamp.setText(timeFormat.format(lastMsg.getTimestamp()));
                    } else {
                        timestamp.setText(dateFormat.format(lastMsg.getTimestamp()));
                    }
                } else {
                    timestamp.setText("");
                }
            } else {
                lastMessage.setText("No messages yet");
                timestamp.setText("");
            }

            // Set unread count
            if (conversation.getUnreadCount() > 0) {
                unreadCount.setVisibility(View.VISIBLE);
                unreadCount.setText(String.valueOf(conversation.getUnreadCount()));
            } else {
                unreadCount.setVisibility(View.GONE);
            }
        }

        private boolean isToday(long timestamp) {
            Calendar today = Calendar.getInstance();
            Calendar messageDate = Calendar.getInstance();
            messageDate.setTimeInMillis(timestamp);

            return today.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR);
        }
    }

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }
}