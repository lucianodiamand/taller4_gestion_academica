package com.ips.gestion_academica.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ips.gestion_academica.dto.auth.LoginRequest;
import com.ips.gestion_academica.exception.CredencialesInvalidasException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.repository.UsuarioRepository;
import com.ips.gestion_academica.security.JwtUtil;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, JwtUtil jwtUtil,
                          BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByLegajo(request.getLegajo())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", null));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new CredencialesInvalidasException();
        }

        String token = jwtUtil.generarToken(usuario.getLegajo(), usuario.getRol());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
