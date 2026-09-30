package com.ips.gestion_academica.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ips.gestion_academica.dto.chat.ChatRequest;
import com.ips.gestion_academica.service.DeepSeekService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final DeepSeekService deepSeekService;

    public ChatController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody ChatRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String legajo = (String) auth.getPrincipal();

        String respuesta = deepSeekService.responder(request.getMessage(), legajo, request.getHistory());

        return ResponseEntity.ok(Map.of("reply", respuesta));
    }
}
