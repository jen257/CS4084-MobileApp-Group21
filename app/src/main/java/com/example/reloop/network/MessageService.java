package com.example.reloop.network;

import androidx.annotation.NonNull;
import com.example.reloop.models.Message;
import com.example.reloop.utils.Constants;
import com.google.firebase.database.*;
import java.util.HashMap;
import java.util.Map;

public class MessageService {
    private final DatabaseReference messagesRef;
    private final DatabaseReference conversationsRef;
    private final DatabaseReference usersRef;

    public MessageService() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        this.messagesRef = db.getReference("messages");
        this.conversationsRef = db.getReference("conversations");
        this.usersRef = db.getReference(Constants.NODE_USERS);
    }

    public void markMessagesAsRead(String conversationId, String currentUserId) {
        messagesRef.orderByChild("conversationId").equalTo(conversationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Message msg = ds.getValue(Message.class);
                            if (msg != null && msg.getReceiverId().equals(currentUserId) && !msg.isRead()) {
                                ds.getRef().child("read").setValue(true);
                            }
                        }
                    }
                    @Override public void onCancelled(DatabaseError error) {}
                });
    }

    public void sendMessage(Message message, MessageCallback callback) {
        String messageId = messagesRef.push().getKey();
        if (messageId == null) return;
        message.setId(messageId);

        messagesRef.child(messageId).setValue(message).addOnSuccessListener(aVoid -> {
            updateConversationMetadata(message);
            if (callback != null) callback.onSuccess(messageId);
        });
    }

    private void updateConversationMetadata(Message message) {
        // Update info for BOTH participants so both see the correct person
        updateParticipantInfo(message.getSenderId(), "A", message);
        updateParticipantInfo(message.getReceiverId(), "B", message);
    }

    private void updateParticipantInfo(String userId, String suffix, Message message) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("username").getValue(String.class);
                String avatar = snapshot.hasChild("profileImage") ?
                        snapshot.child("profileImage").getValue(String.class) : "";

                Map<String, Object> updates = new HashMap<>();
                updates.put("id", message.getConversationId());
                updates.put("participantA", message.getSenderId());
                updates.put("participantB", message.getReceiverId());
                updates.put("lastMessageText", message.getContent());
                updates.put("lastUpdatedLong", ServerValue.TIMESTAMP);
                updates.put("productId", message.getProductId());

                updates.put("name_" + suffix, name != null ? name : "User");
                updates.put("avatar_" + suffix, avatar);

                conversationsRef.child(message.getConversationId()).updateChildren(updates);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public Query getUserConversationsQuery() {
        return conversationsRef.orderByChild("lastUpdatedLong");
    }

    public Query getMessagesQuery(String conversationId) {
        return messagesRef.orderByChild("conversationId").equalTo(conversationId);
    }

    public interface MessageCallback {
        void onSuccess(String id);
        void onFailure(Exception e);
    }
}