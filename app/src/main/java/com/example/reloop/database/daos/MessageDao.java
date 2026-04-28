package com.example.reloop.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.reloop.database.entities.MessageEntity;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(MessageEntity message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessages(List<MessageEntity> messages);

    @Update
    void updateMessage(MessageEntity message);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    LiveData<List<MessageEntity>> getMessagesByConversation(String conversationId);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    LiveData<MessageEntity> getLastMessage(String conversationId);

    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId AND senderId != :currentUserId")
    void markConversationAsRead(String conversationId, String currentUserId);

    @Query("UPDATE messages SET isDelivered = 1 WHERE id = :messageId")
    void markMessageAsDelivered(String messageId);

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId AND isRead = 0 AND senderId != :currentUserId")
    LiveData<Integer> getUnreadCount(String conversationId, String currentUserId);

    @Query("SELECT COUNT(*) FROM messages WHERE receiverId = :userId AND isRead = 0")
    LiveData<Integer> getTotalUnreadCount(String userId);

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    void deleteConversationMessages(String conversationId);

    @Query("DELETE FROM messages WHERE timestamp < :timestamp")
    void deleteOldMessages(long timestamp);
}