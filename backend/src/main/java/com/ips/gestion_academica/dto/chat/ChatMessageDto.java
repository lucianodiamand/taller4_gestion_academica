package com.ips.gestion_academica.dto.chat;

public class ChatMessageDto {

    private String role;
    private String content;

    public ChatMessageDto() {
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
