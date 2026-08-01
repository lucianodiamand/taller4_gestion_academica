package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.curso.CursoRequest;
import com.ips.gestion_academica.dto.curso.CursoResponse;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.model.Curso;
import com.ips.gestion_academica.model.Rol;
import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.repository.CursoRepository;
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
class CursoServiceTest {

    @Mock
    private CursoRepository cursoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CursoService cursoService;

    @Test
    void crearCurso_deberiaCrearCursoCuandoProfesorEsValido() {
        CursoRequest request = new CursoRequest();
        request.setAnio(2026);
        request.setCuatrimestre(1);
        request.setComision("A");
        request.setProfesorId(10L);

        Usuario profesor = new Usuario();
        profesor.setId(10L);
        profesor.setNombre("Juan");
        profesor.setApellido("Pérez");
        profesor.setLegajo("P100");
        profesor.setRol(Rol.PROFESOR);
        profesor.setActivo(true);

        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(profesor));

        when(cursoRepository.save(any(Curso.class)))
                .thenAnswer(invocation -> {
                    Curso curso = invocation.getArgument(0);
                    curso.setId(1L);
                    return curso;
                });

        CursoResponse response = cursoService.crearCurso(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(2026, response.getAnio());
        assertEquals(1, response.getCuatrimestre());
        assertEquals("A", response.getComision());
        assertTrue(response.getActivo());

        assertNotNull(response.getProfesor());
        assertEquals(10L, response.getProfesor().getId());

        verify(usuarioRepository).findById(10L);
        verify(cursoRepository).save(any(Curso.class));
    }

    @Test
    void crearCurso_deberiaLanzarErrorCuandoProfesorNoExiste() {
        CursoRequest request = new CursoRequest();
        request.setAnio(2026);
        request.setCuatrimestre(1);
        request.setComision("A");
        request.setProfesorId(999L);

        when(usuarioRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> cursoService.crearCurso(request)
        );

        verify(cursoRepository, never()).save(any());
    }

    @Test
    void crearCurso_deberiaLanzarErrorCuandoProfesorEstaInactivo() {
        CursoRequest request = new CursoRequest();
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
                RecursoInactivoException.class,
                () -> cursoService.crearCurso(request)
        );

        verify(cursoRepository, never()).save(any());
    }

    @Test
    void crearCurso_deberiaLanzarErrorCuandoUsuarioNoEsProfesor() {
        CursoRequest request = new CursoRequest();
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
                () -> cursoService.crearCurso(request)
        );

        verify(cursoRepository, never()).save(any());
    }

    @Test
    void crearCurso_deberiaGuardarLosDatosCorrectos() {
        CursoRequest request = new CursoRequest();
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

        when(cursoRepository.save(any(Curso.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        cursoService.crearCurso(request);

        ArgumentCaptor<Curso> captor = ArgumentCaptor.forClass(Curso.class);
        verify(cursoRepository).save(captor.capture());

        Curso cursoGuardado = captor.getValue();

        assertEquals(2026, cursoGuardado.getAnio());
        assertEquals(2, cursoGuardado.getCuatrimestre());
        assertEquals("B", cursoGuardado.getComision());
        assertTrue(cursoGuardado.getActivo());
        assertSame(profesor, cursoGuardado.getProfesor());
    }

}

