package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.inscripcion.InscripcionEstadoRequest;
import com.ips.gestion_academica.dto.inscripcion.InscripcionRequest;
import com.ips.gestion_academica.dto.inscripcion.InscripcionResponse;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.model.Curso;
import com.ips.gestion_academica.model.EstadoInscripcion;
import com.ips.gestion_academica.model.Inscripcion;
import com.ips.gestion_academica.model.Rol;
import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.repository.CursoRepository;
import com.ips.gestion_academica.repository.InscripcionRepository;
import com.ips.gestion_academica.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CursoRepository cursoRepository;

    @InjectMocks
    private InscripcionService inscripcionService;

    private InscripcionRequest crearRequestValido() {
        InscripcionRequest request = new InscripcionRequest();
        request.setAlumnoId(1L);
        request.setCursoId(10L);
        return request;
    }

    private Usuario crearAlumnoActivo() {
        Usuario alumno = new Usuario();
        alumno.setId(1L);
        alumno.setNombre("Sofia");
        alumno.setApellido("Danieli");
        alumno.setLegajo("A001");
        alumno.setEmail("sofia@mail.com");
        alumno.setRol(Rol.ALUMNO);
        alumno.setActivo(true);
        return alumno;
    }

    private Curso crearCursoActivo() {
        Curso curso = new Curso();
        curso.setId(10L);
        curso.setAnio(2026);
        curso.setCuatrimestre(1);
        curso.setComision("A");
        curso.setActivo(true);
        return curso;
    }

    private Inscripcion crearInscripcionActiva() {
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setId(100L);
        inscripcion.setAlumno(crearAlumnoActivo());
        inscripcion.setCurso(crearCursoActivo());
        inscripcion.setFechaInscripcion(LocalDate.of(2026, 8, 4));
        inscripcion.setEstado(EstadoInscripcion.INSCRIPTO);
        inscripcion.setActivo(true);
        return inscripcion;
    }

    @Test
    void crearInscripcion_deberiaCrearCorrectamente() {
        InscripcionRequest request = crearRequestValido();
        Usuario alumno = crearAlumnoActivo();
        Curso curso = crearCursoActivo();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(alumno));

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.of(curso));

        when(inscripcionRepository.existsByAlumnoIdAndCursoId(1L, 10L))
                .thenReturn(false);

        when(inscripcionRepository.save(any(Inscripcion.class)))
                .thenAnswer(invocation -> {
                    Inscripcion inscripcion = invocation.getArgument(0);
                    inscripcion.setId(100L);
                    return inscripcion;
                });

        InscripcionResponse response =
                inscripcionService.crearInscripcion(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(EstadoInscripcion.INSCRIPTO, response.getEstado());
        assertTrue(response.getActivo());
        assertNotNull(response.getFechaInscripcion());

        assertEquals(1L, response.getAlumno().getId());
        assertEquals(10L, response.getCurso().getId());

        ArgumentCaptor<Inscripcion> captor =
                ArgumentCaptor.forClass(Inscripcion.class);

        verify(inscripcionRepository).save(captor.capture());

        Inscripcion guardada = captor.getValue();

        assertSame(alumno, guardada.getAlumno());
        assertSame(curso, guardada.getCurso());
        assertEquals(EstadoInscripcion.INSCRIPTO, guardada.getEstado());
        assertTrue(guardada.getActivo());
        assertNotNull(guardada.getFechaInscripcion());
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorCuandoAlumnoNoExiste() {
        InscripcionRequest request = crearRequestValido();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> inscripcionService.crearInscripcion(request)
        );

        verify(cursoRepository, never()).findById(anyLong());
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorCuandoCursoNoExiste() {
        InscripcionRequest request = crearRequestValido();
        Usuario alumno = crearAlumnoActivo();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(alumno));

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> inscripcionService.crearInscripcion(request)
        );

        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorCuandoAlumnoEstaInactivo() {
        InscripcionRequest request = crearRequestValido();

        Usuario alumno = crearAlumnoActivo();
        alumno.setActivo(false);

        Curso curso = crearCursoActivo();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(alumno));

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.of(curso));

        assertThrows(
                RecursoInactivoException.class,
                () -> inscripcionService.crearInscripcion(request)
        );

        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorCuandoUsuarioNoEsAlumno() {
        InscripcionRequest request = crearRequestValido();

        Usuario usuario = crearAlumnoActivo();
        usuario.setRol(Rol.PROFESOR);

        Curso curso = crearCursoActivo();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.of(curso));

        assertThrows(
                IllegalArgumentException.class,
                () -> inscripcionService.crearInscripcion(request)
        );

        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorCuandoCursoEstaInactivo() {
        InscripcionRequest request = crearRequestValido();

        Usuario alumno = crearAlumnoActivo();

        Curso curso = crearCursoActivo();
        curso.setActivo(false);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(alumno));

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.of(curso));

        assertThrows(
                RecursoInactivoException.class,
                () -> inscripcionService.crearInscripcion(request)
        );

        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorCuandoYaExiste() {
        InscripcionRequest request = crearRequestValido();
        Usuario alumno = crearAlumnoActivo();
        Curso curso = crearCursoActivo();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(alumno));

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.of(curso));

        when(inscripcionRepository.existsByAlumnoIdAndCursoId(1L, 10L))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> inscripcionService.crearInscripcion(request)
        );

        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void buscarPorId_deberiaDevolverInscripcionActiva() {
        Inscripcion inscripcion = crearInscripcionActiva();

        when(inscripcionRepository.findById(100L))
                .thenReturn(Optional.of(inscripcion));

        InscripcionResponse response =
                inscripcionService.buscarPorId(100L);

        assertEquals(100L, response.getId());
        assertEquals(EstadoInscripcion.INSCRIPTO, response.getEstado());
        assertEquals(1L, response.getAlumno().getId());
        assertEquals(10L, response.getCurso().getId());
    }

    @Test
    void buscarPorId_deberiaLanzarErrorCuandoNoExiste() {
        when(inscripcionRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> inscripcionService.buscarPorId(999L)
        );
    }

    @Test
    void buscarPorId_deberiaLanzarErrorCuandoEstaInactiva() {
        Inscripcion inscripcion = crearInscripcionActiva();
        inscripcion.setActivo(false);

        when(inscripcionRepository.findById(100L))
                .thenReturn(Optional.of(inscripcion));

        assertThrows(
                RecursoInactivoException.class,
                () -> inscripcionService.buscarPorId(100L)
        );
    }

    @Test
    void modificarEstado_deberiaModificarEstadoCorrectamente() {
        Inscripcion inscripcion = crearInscripcionActiva();

        InscripcionEstadoRequest request =
                new InscripcionEstadoRequest();

        request.setEstado(EstadoInscripcion.REGULAR);

        when(inscripcionRepository.findById(100L))
                .thenReturn(Optional.of(inscripcion));

        when(inscripcionRepository.save(any(Inscripcion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InscripcionResponse response =
                inscripcionService.modificarEstado(100L, request);

        assertEquals(EstadoInscripcion.REGULAR, response.getEstado());
        assertEquals(EstadoInscripcion.REGULAR, inscripcion.getEstado());

        verify(inscripcionRepository).save(inscripcion);
    }

    @Test
    void modificarEstado_deberiaLanzarErrorCuandoNoExiste() {
        InscripcionEstadoRequest request =
                new InscripcionEstadoRequest();

        request.setEstado(EstadoInscripcion.REGULAR);

        when(inscripcionRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> inscripcionService.modificarEstado(999L, request)
        );

        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void modificarEstado_deberiaLanzarErrorCuandoEstaInactiva() {
        Inscripcion inscripcion = crearInscripcionActiva();
        inscripcion.setActivo(false);

        InscripcionEstadoRequest request =
                new InscripcionEstadoRequest();

        request.setEstado(EstadoInscripcion.REGULAR);

        when(inscripcionRepository.findById(100L))
                .thenReturn(Optional.of(inscripcion));

        assertThrows(
                RecursoInactivoException.class,
                () -> inscripcionService.modificarEstado(100L, request)
        );

        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void darDeBaja_deberiaCancelarYDesactivarInscripcion() {
        Inscripcion inscripcion = crearInscripcionActiva();

        when(inscripcionRepository.findById(100L))
                .thenReturn(Optional.of(inscripcion));

        when(inscripcionRepository.save(any(Inscripcion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        inscripcionService.darDeBaja(100L);

        assertFalse(inscripcion.getActivo());
        assertEquals(
                EstadoInscripcion.CANCELADO,
                inscripcion.getEstado()
        );

        verify(inscripcionRepository).save(inscripcion);
    }

    @Test
    void darDeBaja_deberiaLanzarErrorCuandoNoExiste() {
        when(inscripcionRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> inscripcionService.darDeBaja(999L)
        );

        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void darDeBaja_deberiaLanzarErrorCuandoYaEstaInactiva() {
        Inscripcion inscripcion = crearInscripcionActiva();
        inscripcion.setActivo(false);

        when(inscripcionRepository.findById(100L))
                .thenReturn(Optional.of(inscripcion));

        assertThrows(
                RecursoInactivoException.class,
                () -> inscripcionService.darDeBaja(100L)
        );

        verify(inscripcionRepository, never()).save(any());
    }
}