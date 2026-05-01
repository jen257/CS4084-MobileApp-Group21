package com.example.reloop.repository;

import androidx.annotation.NonNull;
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
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageRepository {
    private final MessageDao messageDao;
    private final MessageService messageService;
    private final ExecutorService executorService;

    public MessageRepository(MessageDao messageDao) {
        this.messageDao = messageDao;
        this.messageService = new MessageService();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<Conversation>> getUserConversations(String userId) {
        MutableLiveData<List<Conversation>> conversationsLiveData = new MutableLiveData<>();
        if (userId == null) return conversationsLiveData;

        messageService.getUserConversationsQuery().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Conversation> list = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Conversation conv = snapshot.getValue(Conversation.class);
                    // Null-safe participant check to prevent crashes
                    if (conv != null && conv.getParticipantA() != null && conv.getParticipantB() != null) {
                        if (Objects.equals(conv.getParticipantA(), userId) || Objects.equals(conv.getParticipantB(), userId)) {
                            list.add(conv);
                        }
                    }
                }
                list.sort((o1, o2) -> {
                    if (o1.getLastUpdatedLong() == null || o2.getLastUpdatedLong() == null) return 0;
                    return o2.getLastUpdatedLong().compareTo(o1.getLastUpdatedLong());
                });
                conversationsLiveData.postValue(list);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
        return conversationsLiveData;
    }

    public void sendMessage(Message message) {
        messageService.sendMessage(message, new MessageService.MessageCallback() {
            @Override
            public void onSuccess(String messageId) {
                executorService.execute(() -> messageDao.insertMessage(toMessageEntity(message)));
            }
            @Override public void onFailure(Exception e) {}
        });
    }

    public LiveData<List<Message>> getMessages(String conversationId) {
        MutableLiveData<List<Message>> liveData = new MutableLiveData<>();
        messageService.getMessagesQuery(conversationId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Message msg = ds.getValue(Message.class);
                    if (msg != null) messages.add(msg);
                }
                liveData.postValue(messages);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
        return liveData;
    }

    private MessageEntity toMessageEntity(Message message) {
        MessageEntity entity = new MessageEntity();
        entity.id = message.getId() != null ? message.getId() : "";
        entity.conversationId = message.getConversationId();
        entity.senderId = message.getSenderId();
        entity.receiverId = message.getReceiverId();
        entity.content = message.getContent();
        entity.timestamp = message.getTimestamp() != null ? message.getTimestamp() : new java.util.Date();
        return entity;
    }

    public void markMessagesAsRead(String cid, String uid) {
        messageService.markMessagesAsRead(cid, uid);
    }
}