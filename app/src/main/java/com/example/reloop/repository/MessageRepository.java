package com.example.reloop.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.reloop.database.daos.MessageDao;
import com.example.reloop.database.entities.MessageEntity;
import com.example.reloop.models.Conversation;
import com.example.reloop.models.Message;
import com.example.reloop.network.MessageService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Repository class for handling message-related data operations
 * Mediates between local database (Room) and remote database (Firebase)
 */
public class MessageRepository {
    private MessageDao messageDao;
    private MessageService messageService;
    private ExecutorService executorService;
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * Constructor initializes dependencies
     * @param messageDao DAO for local message database operations
     */
    public MessageRepository(MessageDao messageDao) {
        this.messageDao = messageDao;
        this.messageService = new MessageService();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    /**
     * Send a message and store it in both Firebase and local database
     * @param message The message to be sent
     */
    public void sendMessage(Message message) {
        messageService.sendMessage(message, new MessageService.MessageCallback() {
            @Override
            public void onSuccess(String messageId) {
                // On successful send, save to local database
                executorService.execute(() -> {
                    MessageEntity entity = toMessageEntity(message);
                    messageDao.insertMessage(entity);
                });
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Failed to send message: " + e.getMessage());
            }
        });
    }

    /**
     * Get messages for a conversation with real-time updates
     * @param conversationId ID of the conversation
     * @return LiveData containing list of messages
     */
    public LiveData<List<Message>> getMessages(String conversationId) {
        MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();

        // First get from local database
        LiveData<List<MessageEntity>> localMessages = messageDao.getMessagesByConversation(conversationId);

        // Then get real-time updates from Firebase
        messageService.getMessagesQuery(conversationId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Message> messages = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Message message = snapshot.getValue(Message.class);
                            if (message != null) {
                                messages.add(message);

                                // Save to local database
                                executorService.execute(() -> {
                                    MessageEntity entity = toMessageEntity(message);
                                    messageDao.insertMessage(entity);
                                });
                            }
                        }
                        messagesLiveData.postValue(messages);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        errorMessage.postValue("Failed to load messages: " + databaseError.getMessage());
                    }
                });

        return messagesLiveData;
    }

    /**
     * Get existing conversation or create new one if it doesn't exist
     * @param participantA First participant's user ID
     * @param participantB Second participant's user ID
     * @param productId ID of the product being discussed
     * @param callback Callback to handle results
     */
    public void getOrCreateConversation(String participantA, String participantB,
                                        String productId, MessageService.ConversationCallback callback) {
        messageService.getOrCreateConversation(participantA, participantB, productId, callback);
    }

    /**
     * Get list of conversations for a user
     * @param userId ID of the user
     * @return LiveData containing list of conversations
     */
    public LiveData<List<Conversation>> getUserConversations(String userId) {
        MutableLiveData<List<Conversation>> conversationsLiveData = new MutableLiveData<>();

        messageService.getUserConversationsQuery(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Conversation> conversations = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Conversation conversation = snapshot.getValue(Conversation.class);
                            if (conversation != null &&
                                    (conversation.getParticipantA().equals(userId) ||
                                            conversation.getParticipantB().equals(userId))) {
                                conversations.add(conversation);
                            }
                        }
                        conversationsLiveData.postValue(conversations);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        errorMessage.postValue("Failed to load conversations: " + databaseError.getMessage());
                    }
                });

        return conversationsLiveData;
    }

    /**
     * Mark all messages in a conversation as read
     * @param conversationId ID of the conversation
     * @param currentUserId ID of the current user
     */
    public void markMessagesAsRead(String conversationId, String currentUserId) {
        messageService.markMessagesAsRead(conversationId, currentUserId);

        // Update local database
        executorService.execute(() -> {
            messageDao.markConversationAsRead(conversationId, currentUserId);
        });
    }

    /**
     * Get total count of unread messages for a user
     * @param userId ID of the user
     * @return LiveData containing unread message count
     */
    public LiveData<Integer> getTotalUnreadCount(String userId) {
        return messageDao.getTotalUnreadCount(userId);
    }

    /**
     * Convert Message to MessageEntity for local storage
     * @param message Message object
     * @return MessageEntity object
     */
    private MessageEntity toMessageEntity(Message message) {
        MessageEntity entity = new MessageEntity();
        entity.id = message.getId();
        entity.conversationId = message.getConversationId();
        entity.senderId = message.getSenderId();
        entity.receiverId = message.getReceiverId();
        entity.productId = message.getProductId();
        entity.content = message.getContent();
        entity.messageType = message.getMessageType();
        entity.imageUrl = message.getImageUrl();
        entity.timestamp = message.getTimestamp();
        entity.isRead = message.isRead();
        entity.isDelivered = message.isDelivered();
        return entity;
    }

    /**
     * Convert MessageEntity to Message
     * @param entity MessageEntity object
     * @return Message object
     */
    private Message toMessage(MessageEntity entity) {
        Message message = new Message();
        message.setId(entity.id);
        message.setConversationId(entity.conversationId);
        message.setSenderId(entity.senderId);
        message.setReceiverId(entity.receiverId);
        message.setProductId(entity.productId);
        message.setContent(entity.content);
        message.setMessageType(entity.messageType);
        message.setImageUrl(entity.imageUrl);
        message.setTimestamp(entity.timestamp);
        message.setRead(entity.isRead);
        message.setDelivered(entity.isDelivered);
        return message;
    }

    /**
     * Get error messages
     * @return LiveData containing error messages
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        executorService.shutdown();
    }
}