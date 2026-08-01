package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.usuario.UsuarioRequest;
import com.ips.gestion_academica.dto.usuario.UsuarioResponse;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.model.Rol;
import com.ips.gestion_academica.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void crearUsuario_deberiaCrearUsuario() {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Juan");
        request.setApellido("Perez");
        request.setLegajo("L-1234");
        request.setDni("1234");
        request.setEmail("email@gmail.com");
        request.setPassword(("MyPassword"));
        request.setRol(Rol.PROFESOR);


        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario Usuario = invocation.getArgument(0);
                    Usuario.setId(1L);
                    return Usuario;
                });

        UsuarioResponse response = usuarioService.crearUsuario(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Juan", response.getNombre());
        assertEquals("Perez", response.getApellido());
        assertEquals("L-1234", response.getLegajo());
        assertTrue(response.getActivo());

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void crearUsuario_deberiaLanzarErrorCuandoEmailExiste() {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Juan");
        request.setApellido("Perez");
        request.setLegajo("L-1234");
        request.setDni("1234");
        request.setEmail("email1@gmail.com");
        request.setPassword(("MyPassword"));
        request.setRol(Rol.PROFESOR);


        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.empty());

        UsuarioRequest request_2 = new UsuarioRequest();
        request_2.setNombre("Juan");
        request_2.setApellido("Perez");
        request_2.setLegajo("L-12345");
        request_2.setDni("12345");
        request_2.setEmail("email1@gmail.com");
        request_2.setPassword(("MyPassword"));
        request_2.setRol(Rol.PROFESOR);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> usuarioService.crearUsuario(request)
        );

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearUsuario_deberiaLanzarErrorCuandoProfesorEstaInactivo() {
        UsuarioRequest request = new UsuarioRequest();
        request.setAnio(2026);
        request.setCuatrimestre(1);
        request.setComision("A");
        request.setProfesorId(10L);

        Usuario profesor = new Usuario();
        profesor.setId(10L);
        profesor.setRol(Rol.PROFESOR);
        profesor.setActivo(false);

        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(profesor));

        assertThrows(
                ReUsuarioInactivoException.class,
                () -> UsuarioService.crearUsuario(request)
        );

        verify(UsuarioRepository, never()).save(any());
    }

    @Test
    void crearUsuario_deberiaLanzarErrorCuandoUsuarioNoEsProfesor() {
        UsuarioRequest request = new UsuarioRequest();
        request.setAnio(2026);
        request.setCuatrimestre(1);
        request.setComision("A");
        request.setProfesorId(10L);

        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setRol(Rol.ALUMNO);
        usuario.setActivo(true);

        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(usuario));

        assertThrows(
                IllegalArgumentException.class,
                () -> UsuarioService.crearUsuario(request)
        );

        verify(UsuarioRepository, never()).save(any());
    }

    @Test
    void crearUsuario_deberiaGuardarLosDatosCorrectos() {
        UsuarioRequest request = new UsuarioRequest();
        request.setAnio(2026);
        request.setCuatrimestre(2);
        request.setComision("B");
        request.setProfesorId(5L);

        Usuario profesor = new Usuario();
        profesor.setId(5L);
        profesor.setNombre("Ana");
        profesor.setApellido("López");
        profesor.setLegajo("P005");
        profesor.setRol(Rol.PROFESOR);
        profesor.setActivo(true);

        when(usuarioRepository.findById(5L))
                .thenReturn(Optional.of(profesor));

        when(UsuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioService.crearUsuario(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(UsuarioRepository).save(captor.capture());

        Usuario UsuarioGuardado = captor.getValue();

        assertEquals(2026, UsuarioGuardado.getAnio());
        assertEquals(2, UsuarioGuardado.getCuatrimestre());
        assertEquals("B", UsuarioGuardado.getComision());
        assertTrue(UsuarioGuardado.getActivo());
        assertSame(profesor, UsuarioGuardado.getProfesor());
    }

}

