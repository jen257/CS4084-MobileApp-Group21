package com.example.reloop.network;
import android.util.Log;
import androidx.annotation.NonNull;

import com.example.reloop.utils.Constants;
import com.example.reloop.models.Conversation;
import com.example.reloop.models.Message;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

// Service class for handling message-related operations with Firebase Realtime Database
// Provides functionality for sending messages, managing conversations, and real-time messaging
public class MessageService {
    private DatabaseReference messagesRef;
    private DatabaseReference conversationsRef;

    private static final String FCM_SERVER_KEY = "AIzaSyBOIjJ-zZION6Wh3F_NZK_cn3AscTakips";
    private DatabaseReference usersRef;
    public MessageService() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("messages");
        conversationsRef = database.getReference("conversations");
        usersRef = database.getReference(Constants.NODE_USERS);
    }

    // Send a message and store it in Firebase
    public void sendMessage(Message message, MessageCallback callback) {
        String messageId = messagesRef.push().getKey();
        if (messageId != null) {
            message.setId(messageId);

            // Save to messages node
            messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        // Update the last message in the conversation
                        updateConversationLastMessage(message);
                        sendPushNotificationToReceiver(message);
                        if (callback != null) {
                            callback.onSuccess(messageId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    });
        }
    }

    // Update the last message and timestamp in a conversation
    private void updateConversationLastMessage(Message message) {
        Map<String, Object> conversationUpdate = new HashMap<>();
        conversationUpdate.put("lastMessage", message);
        conversationUpdate.put("lastUpdated", message.getTimestamp());

        conversationsRef.child(message.getConversationId()).updateChildren(conversationUpdate);
    }

    // Get existing conversation or create a new one if it doesn't exist
    public void getOrCreateConversation(String participantA, String participantB,
                                        String productId, ConversationCallback callback) {
        String conversationId = Conversation.generateConversationId(participantA, participantB, productId);

        conversationsRef.child(conversationId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Conversation already exists
                    if (callback != null) {
                        callback.onConversationExists(conversationId);
                    }
                } else {
                    // Create new conversation
                    Map<String, Object> conversationData = new HashMap<>();
                    conversationData.put("participantA", participantA);
                    conversationData.put("participantB", participantB);
                    conversationData.put("productId", productId);
                    conversationData.put("lastUpdated", System.currentTimeMillis());

                    conversationsRef.child(conversationId).setValue(conversationData)
                            .addOnSuccessListener(aVoid -> {
                                if (callback != null) {
                                    callback.onConversationCreated(conversationId);
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) {
                                    callback.onFailure(e);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) {
                    callback.onFailure(new Exception(error.getMessage()));
                }
            }
        });
    }

    // Get query for retrieving messages in a specific conversation
    public Query getMessagesQuery(String conversationId) {
        return messagesRef.orderByChild("conversationId").equalTo(conversationId);
    }

    // Get query for retrieving user's conversation list
    public Query getUserConversationsQuery(String userId) {
        // Note: This query needs to be filtered client-side for the specific user
        return conversationsRef.orderByChild("lastUpdated");
    }

    // Mark all messages in a conversation as read for the current user
    public void markMessagesAsRead(String conversationId, String currentUserId) {
        Query query = messagesRef.orderByChild("conversationId").equalTo(conversationId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null && !message.getSenderId().equals(currentUserId) && !message.isRead()) {
                        messageSnapshot.getRef().child("isRead").setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error silently or log it
            }
        });
    }

    // Callback interfaces

    // Callback interface for message sending operations
    public interface MessageCallback {
        // Called when message is successfully sent
        void onSuccess(String messageId);

        // Called when message sending fails
        void onFailure(Exception e);
    }

    // Callback interface for conversation operations
    public interface ConversationCallback {
        // Called when a new conversation is created
        void onConversationCreated(String conversationId);

        // Called when an existing conversation is found
        void onConversationExists(String conversationId);

        // Called when conversation operation fails
        void onFailure(Exception e);
    }

    private void sendPushNotificationToReceiver(Message message) {
        String receiverId = message.getReceiverId();

        usersRef.child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("fcmToken")) {
                    String token = snapshot.child("fcmToken").getValue(String.class);
                    if (token != null && !token.isEmpty()) {
                        buildAndSendFCMRequest(token, "New Message", message.getContent());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void buildAndSendFCMRequest(String targetToken, String title, String body) {
        if (FCM_SERVER_KEY.equals("AIzaSyBOIjJ-zZION6Wh3F_NZK_cn3AscTakips")) return;

        String jsonPayload = "{"
                + "\"to\": \"" + targetToken + "\","
                + "\"notification\": {"
                + "  \"title\": \"" + title + "\","
                + "  \"body\": \"" + body + "\""
                + "}"
                + "}";

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(jsonPayload, JSON);

        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .addHeader("Authorization", "key=" + FCM_SERVER_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            }
        });
    }
}