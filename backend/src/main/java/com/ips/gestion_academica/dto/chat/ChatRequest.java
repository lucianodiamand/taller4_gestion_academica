package com.ips.gestion_academica.dto.chat;

import java.util.ArrayList;
import java.util.List;

public class ChatRequest {

    private String message;
    private List<ChatMessageDto> history = new ArrayList<>();

    public ChatRequest() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ChatMessageDto> getHistory() {
        return history;
    }

    public void setHistory(List<ChatMessageDto> history) {
        this.history = history;
    }
}
