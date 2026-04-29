package com.example.reloop.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.reloop.database.converters.DateConverter;

import java.util.Date;

@Entity(tableName = "messages")
@TypeConverters(DateConverter.class)
public class MessageEntity {

    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String id = "";

    public String conversationId;
    public String senderId;
    public String receiverId;
    public String productId;
    public String content;
    public String messageType;
    public String imageUrl;
    public Date timestamp;
    public boolean isRead;
    public boolean isDelivered;

    // Room requires this empty constructor
    public MessageEntity() {}

    // @Ignore tells Room to bypass this and use the empty one above
    @Ignore
    public MessageEntity(@NonNull String id, String conversationId, String senderId, String receiverId,
                         String productId, String content, String messageType, Date timestamp) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.productId = productId;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = timestamp;
    }
}