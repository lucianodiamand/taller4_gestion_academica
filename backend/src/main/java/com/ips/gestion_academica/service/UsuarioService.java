package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.usuario.UsuarioRequest;
import com.ips.gestion_academica.dto.usuario.UsuarioResponse;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.exception.UsuarioInactivoException;
import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;



@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    private UsuarioResponse convertirAResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getLegajo(),
                usuario.getEmail(),
                usuario.getActivo(),
                usuario.getRol()
        );
    }

    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findByActivoTrue()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException(id));

        if (!usuario.getActivo()) {
            throw new UsuarioInactivoException(id);
        }

        return convertirAResponse(usuario);
    }


    public UsuarioResponse crearUsuario(UsuarioRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setDni(request.getDni());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword());
        usuario.setLegajo(request.getLegajo());
        usuario.setRol(request.getRol());

        usuario.setActivo(true);

        if(usuarioRepository.existsByEmail(usuario.getEmail())){
            throw new RecursoDuplicadoException(
                "Ya existe un usuario con ese email " + usuario.getEmail()
            );
        }

        if(usuarioRepository.existsByDni(usuario.getDni())){
            throw new RecursoDuplicadoException(
                "Ya existe un usuario con ese dni " + usuario.getDni()
            );
        }

        if(usuarioRepository.existsByLegajo(usuario.getLegajo())){
            throw new RecursoDuplicadoException(
                "Ya existe un usuario con ese legajo " + usuario.getLegajo()
            );
        }
        Usuario usuario_creado = usuarioRepository.save(usuario);
        return convertirAResponse(usuario_creado);
    }

    public UsuarioResponse modificarUsuario(Long id, UsuarioRequest usuarioModificado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(id));

        if(usuarioRepository.existsByEmailAndIdNot(usuarioModificado.getEmail(),id)){
            throw new RecursoDuplicadoException(
                "Ya existe un usuario con ese email " + usuarioModificado.getEmail()
            );
        }
        if(usuarioRepository.existsByDniAndIdNot(usuarioModificado.getDni(),id)){
            throw new RecursoDuplicadoException(
                "Ya existe un usuario con ese DNI " + usuarioModificado.getDni()
            );
        }

        if(usuarioRepository.existsByLegajoAndIdNot(usuarioModificado.getLegajo(),id)){
            throw new RecursoDuplicadoException(
                "Ya existe un usuario con ese Legajo " + usuarioModificado.getLegajo()
            );
        }

        if (!usuario.getActivo()) {
            throw new UsuarioInactivoException(id);
        }

        usuario.setNombre(usuarioModificado.getNombre());
        usuario.setApellido(usuarioModificado.getApellido());
        usuario.setDni(usuarioModificado.getDni());
        usuario.setEmail(usuarioModificado.getEmail());
        usuario.setLegajo(usuarioModificado.getLegajo());
        usuario.setRol(usuarioModificado.getRol());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return convertirAResponse(usuarioGuardado);
    }

    public void darDeBaja(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(id));


        if (!usuario.getActivo()) {
            throw new UsuarioInactivoException(id);
        }
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }
}