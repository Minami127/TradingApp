package com.example.carrotapp.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.carrotapp.config.Config;

import java.util.Date;

public class ChatMessage {
    private String message;
    private int senderId;
    private String senderType;
    private long timestamp;

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}