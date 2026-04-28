package com.example.reloop.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.reloop.database.converters.DateConverter;

import java.util.Date;

@Entity(tableName = "messages")
@TypeConverters(DateConverter.class)
public class MessageEntity {
    @PrimaryKey(autoGenerate = false)
    public String id;

    public String conversationId;
    public String senderId;
    public String receiverId;
    public String productId;
    public String content;
    public String messageType; // TEXT, IMAGE, LOCATION
    public String imageUrl;
    public Date timestamp;
    public boolean isRead;
    public boolean isDelivered;

    public MessageEntity() {}

    public MessageEntity(String id, String conversationId, String senderId, String receiverId,
                         String productId, String content, String messageType, Date timestamp) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.productId = productId;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.isRead = false;
        this.isDelivered = false;
    }
}