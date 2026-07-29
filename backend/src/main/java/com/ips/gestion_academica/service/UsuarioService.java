package com.ips.gestion_academica.service;

import com.ips.gestion_academica.exception.UsuarioDuplicadoException;
import com.ips.gestion_academica.exception.UsuarioNoEncontradoException;
import com.ips.gestion_academica.exception.UsuarioInactivoException;
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
        return usuarioRepository.findByActivoTrue();
    }

    public Usuario buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        if (!usuario.getActivo()) {
            throw new UsuarioInactivoException(id);
        }

        return usuario;
    }

    public Usuario crearUsuario(Usuario usuario) {
        if(usuarioRepository.existsByEmail(usuario.getEmail())){
            throw new UsuarioDuplicadoException(
                "Ya existe un usuario con ese email " + usuario.getEmail()
            );
        }

        if(usuarioRepository.existsByDni(usuario.getDni())){
            throw new UsuarioDuplicadoException(
                "Ya existe un usuario con ese dni " + usuario.getDni()
            );
        }

        if(usuarioRepository.existsByLegajo(usuario.getLegajo())){
            throw new UsuarioDuplicadoException(
                "Ya existe un usuario con ese legajo " + usuario.getLegajo()
            );
        }

        return usuarioRepository.save(usuario);
    }

    public Usuario modificarUsuario(Long id, Usuario usuarioModificado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        if(usuarioRepository.existsByEmailAndIdNot(usuarioModificado.getEmail(),id)){
            throw new UsuarioDuplicadoException(
                "Ya existe un usuario con ese email " + usuarioModificado.getEmail()
            );
        }
        if(usuarioRepository.existsByDniAndIdNot(usuarioModificado.getDni(),id)){
            throw new UsuarioDuplicadoException(
                "Ya existe un usuario con ese DNI " + usuarioModificado.getDni()
            );
        }

        if(usuarioRepository.existsByLegajoAndIdNot(usuarioModificado.getLegajo(),id)){
            throw new UsuarioDuplicadoException(
                "Ya existe un usuario con ese Legajo " + usuarioModificado.getLegajo()
            );
        }

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
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));


        if (!usuario.getActivo()) {
            throw new UsuarioInactivoException(id);
        }
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }
}