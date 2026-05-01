package com.example.reloop.ui.message.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.reloop.R;
import com.example.reloop.models.Conversation;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {
    private List<Conversation> conversations;
    private final OnConversationClickListener listener;

    public ConversationAdapter(List<Conversation> conversations, OnConversationClickListener listener) {
        this.conversations = conversations != null ? conversations : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.bind(conversation);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onConversationClick(conversation);
        });
    }

    @Override
    public int getItemCount() { return conversations.size(); }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations != null ? conversations : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final TextView userName, lastMessage, timestamp;
        private final ImageView profileImage;
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            timestamp = itemView.findViewById(R.id.timestamp);
            profileImage = itemView.findViewById(R.id.user_avatar);
        }

        void bind(Conversation conversation) {
            String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Display the OTHER person's name
            userName.setText(conversation.getDisplayName(currentUid));
            lastMessage.setText(conversation.getLastMessageText());

            String avatarUrl = conversation.getDisplayAvatar(currentUid);
            Glide.with(itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.deafult_contact_person_avatar)
                    .circleCrop()
                    .into(profileImage);

            if (conversation.getLastUpdatedLong() != null) {
                long time = conversation.getLastUpdatedLong();
                timestamp.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(time)));
            }
        }

        private boolean isToday(long time) {
            Calendar today = Calendar.getInstance();
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(time);
            return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
        }
    }

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }
}