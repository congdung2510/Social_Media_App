package com.is1423.socialmedia.domain;

import java.time.Instant;

public class Message {
    String message, receiver, sender;
    String sendDatetime;
    boolean isSeen;

    public Message() {
    }

    public Message(String message, String receiver, String sender, String sendDatetime, boolean isSeen) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.sendDatetime = sendDatetime;
        this.isSeen = isSeen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSendDatetime() {
        return sendDatetime;
    }

    public void setSendDatetime(String sendDatetime) {
        this.sendDatetime = sendDatetime;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
