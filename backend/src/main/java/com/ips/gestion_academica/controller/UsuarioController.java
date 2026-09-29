package com.ips.gestion_academica.controller;

import com.ips.gestion_academica.dto.usuario.ActualizarPerfilRequest;
import com.ips.gestion_academica.dto.usuario.CambiarPasswordRequest;
import com.ips.gestion_academica.dto.usuario.UsuarioRequest;
import com.ips.gestion_academica.dto.usuario.UsuarioResponse;
import com.ips.gestion_academica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    /**
     * Cambia la contrasena del usuario logueado. El legajo se toma del
     * token (no de la URL ni del body), asi que solo se puede cambiar
     * la propia contrasena, nunca la de otro usuario.
     */
    @PutMapping("/me/password")
    public ResponseEntity<Void> cambiarMiPassword(
            @Valid @RequestBody CambiarPasswordRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String legajoLogueado = (String) auth.getPrincipal();

        usuarioService.cambiarPassword(legajoLogueado, request);

        return ResponseEntity.noContent().build();
    }

    /** Datos del propio perfil (el usuario logueado se identifica por el token). */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> obtenerMiPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String legajoLogueado = (String) auth.getPrincipal();

        return ResponseEntity.ok(usuarioService.obtenerPerfil(legajoLogueado));
    }

    /**
     * Actualiza nombre/apellido/email del propio perfil. A proposito NO
     * permite tocar legajo, dni ni rol: eso sigue siendo exclusivo de
     * ADMIN via PUT /{id}.
     */
    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> actualizarMiPerfil(
            @Valid @RequestBody ActualizarPerfilRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String legajoLogueado = (String) auth.getPrincipal();

        return ResponseEntity.ok(usuarioService.actualizarPerfil(legajoLogueado, request));
    }
}