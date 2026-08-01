package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.curso.CursoRequest;
import com.ips.gestion_academica.dto.curso.CursoResponse;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.model.Curso;
import com.ips.gestion_academica.model.Materia;
import com.ips.gestion_academica.model.Rol;
import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.repository.CursoRepository;
import com.ips.gestion_academica.repository.MateriaRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MateriaRepository materiaRepository;

    @InjectMocks
    private CursoService cursoService;

    private CursoRequest crearRequestValido() {
        CursoRequest request = new CursoRequest();
        request.setAnio(2026);
        request.setCuatrimestre(1);
        request.setComision("A");
        request.setProfesorId(10L);
        request.setMateriaId(20L);

        return request;
    }

    private Usuario crearProfesorActivo() {
        Usuario profesor = new Usuario();
        profesor.setId(10L);
        profesor.setNombre("Juan");
        profesor.setApellido("Pérez");
        profesor.setLegajo("P100");
        profesor.setRol(Rol.PROFESOR);
        profesor.setActivo(true);

        return profesor;
    }

    private Materia crearMateriaActiva() {
        Materia materia = new Materia();
        materia.setId(20L);
        materia.setNombre("Programación II");
        materia.setCodigo("PROG2");
        materia.setActivo(true);

        return materia;
    }

    @Test
    void crearCurso_deberiaCrearCursoCuandoProfesorYMateriaSonValidos() {
        CursoRequest request = crearRequestValido();
        Usuario profesor = crearProfesorActivo();
        Materia materia = crearMateriaActiva();

        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(profesor));

        when(materiaRepository.findById(20L))
                .thenReturn(Optional.of(materia));

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

        // Agregalo si CursoResponse contiene MateriaResponse:
        assertNotNull(response.getMateria());
        assertEquals(20L, response.getMateria().getId());

        verify(usuarioRepository).findById(10L);
        verify(materiaRepository).findById(20L);
        verify(cursoRepository).save(any(Curso.class));
    }

    @Test
    void crearCurso_deberiaLanzarErrorCuandoProfesorNoExiste() {
        CursoRequest request = crearRequestValido();

        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> cursoService.crearCurso(request)
        );

        verify(materiaRepository, never()).findById(anyLong());
        verify(cursoRepository, never()).save(any(Curso.class));
    }

    @Test
    void crearCurso_deberiaLanzarErrorCuandoProfesorEstaInactivo() {
        CursoRequest request = crearRequestValido();

        Usuario profesor = crearProfesorActivo();
        profesor.setActivo(false);

        Materia materia = crearMateriaActiva();

        when(usuarioRepository.findById(request.getProfesorId()))
                .thenReturn(Optional.of(profesor));

        when(materiaRepository.findById(request.getMateriaId()))
                .thenReturn(Optional.of(materia));

        assertThrows(
                RecursoInactivoException.class,
                () -> cursoService.crearCurso(request)
        );

        verify(cursoRepository, never()).save(any(Curso.class));
    }

    @Test
    void crearCurso_deberiaLanzarErrorCuandoUsuarioNoEsProfesor() {
        CursoRequest request = crearRequestValido();

        Usuario usuario = crearProfesorActivo();
        usuario.setRol(Rol.ALUMNO);

        Materia materia = crearMateriaActiva();

        when(usuarioRepository.findById(request.getProfesorId()))
                .thenReturn(Optional.of(usuario));

        when(materiaRepository.findById(request.getMateriaId()))
                .thenReturn(Optional.of(materia));

        assertThrows(
                IllegalArgumentException.class,
                () -> cursoService.crearCurso(request)
        );

        verify(cursoRepository, never()).save(any(Curso.class));
    }
    @Test
    void crearCurso_deberiaLanzarErrorCuandoMateriaNoExiste() {
        CursoRequest request = crearRequestValido();
        Usuario profesor = crearProfesorActivo();

        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(profesor));

        when(materiaRepository.findById(20L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> cursoService.crearCurso(request)
        );

        verify(cursoRepository, never()).save(any(Curso.class));
    }

    @Test
    void crearCurso_deberiaLanzarErrorCuandoMateriaEstaInactiva() {
        CursoRequest request = crearRequestValido();

        Usuario profesor = crearProfesorActivo();

        Materia materia = crearMateriaActiva();
        materia.setActivo(false);

        when(usuarioRepository.findById(request.getProfesorId()))
                .thenReturn(Optional.of(profesor));

        when(materiaRepository.findById(request.getMateriaId()))
                .thenReturn(Optional.of(materia));

        assertThrows(
                RecursoInactivoException.class,
                () -> cursoService.crearCurso(request)
        );

        verify(cursoRepository, never()).save(any(Curso.class));
    }

    @Test
    void crearCurso_deberiaGuardarLosDatosCorrectos() {
        CursoRequest request = crearRequestValido();
        request.setCuatrimestre(2);
        request.setComision("B");

        Usuario profesor = crearProfesorActivo();
        Materia materia = crearMateriaActiva();

        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(profesor));

        when(materiaRepository.findById(20L))
                .thenReturn(Optional.of(materia));

        when(cursoRepository.save(any(Curso.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        cursoService.crearCurso(request);

        ArgumentCaptor<Curso> captor =
                ArgumentCaptor.forClass(Curso.class);

        verify(cursoRepository).save(captor.capture());

        Curso cursoGuardado = captor.getValue();

        assertEquals(2026, cursoGuardado.getAnio());
        assertEquals(2, cursoGuardado.getCuatrimestre());
        assertEquals("B", cursoGuardado.getComision());
        assertTrue(cursoGuardado.getActivo());

        assertSame(profesor, cursoGuardado.getProfesor());
        assertSame(materia, cursoGuardado.getMateria());
    }

    
}