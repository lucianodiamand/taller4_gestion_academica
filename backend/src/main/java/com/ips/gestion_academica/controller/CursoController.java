package com.ips.gestion_academica.controller;

import com.ips.gestion_academica.dto.usuario.UsuarioRequest;
import com.ips.gestion_academica.dto.usuario.UsuarioResponse;
import com.ips.gestion_academica.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/{id}")
        public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
            return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }
    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(@RequestBody UsuarioRequest usuario) {
        UsuarioResponse usuarioCreado = usuarioService.crearUsuario(usuario);
        return ResponseEntity.ok(usuarioCreado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> modificarUsuario(
            @PathVariable Long id,
            @RequestBody UsuarioRequest usuario) {

        UsuarioResponse usuarioModificado =
                usuarioService.modificarUsuario(id, usuario);

        return ResponseEntity.ok(usuarioModificado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        usuarioService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}