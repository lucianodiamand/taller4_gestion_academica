package com.ips.gestion_academica.service;

import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario crearUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Usuario modificarUsuario(Long id, Usuario usuarioModificado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombre(usuarioModificado.getNombre());
        usuario.setApellido(usuarioModificado.getApellido());
        usuario.setDni(usuarioModificado.getDni());
        usuario.setEmail(usuarioModificado.getEmail());
        usuario.setLegajo(usuarioModificado.getLegajo());
        usuario.setRol(usuarioModificado.getRol());

        return usuarioRepository.save(usuario);
    }

    public void darDeBaja(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }
}