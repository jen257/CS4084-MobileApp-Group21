package com.example.reloop.models;

import java.io.Serializable;

public class Conversation implements Serializable {
    private String id;
    private String participantA;
    private String participantB;
    private String lastMessageText;
    private Long lastUpdatedLong;
    private String productId;
    private String name_A;
    private String name_B;
    private String avatar_A;
    private String avatar_B;

    public Conversation() {}

    public String getDisplayName(String currentUserId) {
        return currentUserId.equals(participantA) ? name_B : name_A;
    }

    public String getDisplayAvatar(String currentUserId) {
        return currentUserId.equals(participantA) ? avatar_B : avatar_A;
    }

    // Getters and Setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getParticipantA() { return participantA; }
    public void setParticipantA(String participantA) { this.participantA = participantA; }
    public String getParticipantB() { return participantB; }
    public void setParticipantB(String participantB) { this.participantB = participantB; }
    public String getLastMessageText() { return lastMessageText; }
    public void setLastMessageText(String lastMessageText) { this.lastMessageText = lastMessageText; }
    public Long getLastUpdatedLong() { return lastUpdatedLong; }
    public void setLastUpdatedLong(Long lastUpdatedLong) { this.lastUpdatedLong = lastUpdatedLong; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getName_A() { return name_A; }
    public void setName_A(String name_A) { this.name_A = name_A; }
    public String getName_B() { return name_B; }
    public void setName_B(String name_B) { this.name_B = name_B; }
    public String getAvatar_A() { return avatar_A; }
    public void setAvatar_A(String avatar_A) { this.avatar_A = avatar_A; }
    public String getAvatar_B() { return avatar_B; }
    public void setAvatar_B(String avatar_B) { this.avatar_B = avatar_B; }

    public String getOtherParticipant(String currentUserId) {
        if (currentUserId == null || participantA == null) return participantB;
        return currentUserId.equals(participantA) ? participantB : participantA;
    }
}