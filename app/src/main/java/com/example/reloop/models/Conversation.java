package com.example.reloop.models;

import java.util.Date;
import java.util.Objects;

public class Conversation {
    private String id;
    private String participantA;
    private String participantB;
    private String productId;
    private Message lastMessage;
    private Date lastUpdated;
    private int unreadCount;
    private String productTitle;
    private String productImage;
    private String otherParticipantName;
    private String otherParticipantId;

    public Conversation() {
        // Firebase需要的空构造方法
    }

    public Conversation(String participantA, String participantB, String productId) {
        this.participantA = participantA;
        this.participantB = participantB;
        this.productId = productId;
        this.lastUpdated = new Date();
        this.unreadCount = 0;
        this.id = generateConversationId(participantA, participantB, productId);
    }

    public static String generateConversationId(String user1, String user2, String productId) {
        String userA = user1.compareTo(user2) < 0 ? user1 : user2;
        String userB = user1.compareTo(user2) < 0 ? user2 : user1;
        return userA + "_" + userB + "_" + productId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getParticipantA() { return participantA; }
    public void setParticipantA(String participantA) { this.participantA = participantA; }

    public String getParticipantB() { return participantB; }
    public void setParticipantB(String participantB) { this.participantB = participantB; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Message getLastMessage() { return lastMessage; }
    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
        if (lastMessage != null) {
            this.lastUpdated = lastMessage.getTimestamp();
        }
    }

    public Date getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public String getOtherParticipantName() { return otherParticipantName; }
    public void setOtherParticipantName(String otherParticipantName) { this.otherParticipantName = otherParticipantName; }

    public String getOtherParticipantId() { return otherParticipantId; }
    public void setOtherParticipantId(String otherParticipantId) { this.otherParticipantId = otherParticipantId; }

    public String getOtherParticipant(String currentUserId) {
        return participantA.equals(currentUserId) ? participantB : participantA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversation that = (Conversation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}