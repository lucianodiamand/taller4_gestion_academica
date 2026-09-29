package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.inscripcionexamen.InscripcionExamenNotaRequest;
import com.ips.gestion_academica.dto.inscripcionexamen.InscripcionExamenRequest;
import com.ips.gestion_academica.dto.inscripcionexamen.InscripcionExamenResponse;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
import com.ips.gestion_academica.model.Curso;
import com.ips.gestion_academica.model.Examen;
import com.ips.gestion_academica.model.InscripcionExamen;
import com.ips.gestion_academica.model.Materia;
import com.ips.gestion_academica.model.Rol;
import com.ips.gestion_academica.model.TipoExamen;
import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.repository.ExamenRepository;
import com.ips.gestion_academica.repository.InscripcionExamenRepository;
import com.ips.gestion_academica.repository.InscripcionRepository;
import com.ips.gestion_academica.repository.UsuarioRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscripcionExamenServiceTest {

    @Mock
    private InscripcionExamenRepository inscripcionExamenRepository;

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ExamenRepository examenRepository;

    @InjectMocks
    private InscripcionExamenService inscripcionExamenService;

    @BeforeEach
    void setUp() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "A001",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ALUMNO"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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

    private Examen crearExamenActivo() {
        Materia materia = new Materia();
        materia.setNombre("Matematica");

        Curso curso = new Curso();
        curso.setId(10L);
        curso.setAnio(2026);
        curso.setCuatrimestre(1);
        curso.setComision("A");
        curso.setMateria(materia);

        Examen examen = new Examen();
        examen.setId(10L);
        examen.setFecha(LocalDate.of(2026, 12, 15));
        examen.setTipo(TipoExamen.FINAL);
        examen.setDescripcion("Examen final");
        examen.setCurso(curso);
        examen.setActivo(true);
        return examen;
    }

    private InscripcionExamenRequest crearRequest() {
        InscripcionExamenRequest request = new InscripcionExamenRequest();
        request.setExamenId(10L);
        return request;
    }

    private InscripcionExamen crearInscripcionActiva() {
        InscripcionExamen inscripcion = new InscripcionExamen();
        inscripcion.setId(100L);
        inscripcion.setAlumno(crearAlumnoActivo());
        inscripcion.setExamen(crearExamenActivo());
        inscripcion.setFechaInscripcion(LocalDate.now());
        inscripcion.setActivo(true);
        return inscripcion;
    }

    @Test
    void crear_deberiaCrearInscripcionCorrectamente() {
        InscripcionExamenRequest request = crearRequest();
        Usuario alumno = crearAlumnoActivo();
        Examen examen = crearExamenActivo();

        when(usuarioRepository.findByLegajo("A001")).thenReturn(Optional.of(alumno));
        when(examenRepository.findById(10L)).thenReturn(Optional.of(examen));
        when(inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivoTrue(1L, 10L)).thenReturn(true);
        when(inscripcionExamenRepository.existsByAlumnoIdAndExamenIdAndActivoTrue(1L, 10L)).thenReturn(false);
        when(inscripcionExamenRepository.save(any(InscripcionExamen.class)))
                .thenAnswer(invocation -> {
                    InscripcionExamen i = invocation.getArgument(0);
                    i.setId(100L);
                    return i;
                });

        InscripcionExamenResponse response = inscripcionExamenService.crear(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertNull(response.getNota());
        assertTrue(response.getActivo());
        assertEquals(1L, response.getAlumno().getId());
        assertEquals(10L, response.getExamen().getId());
        verify(inscripcionExamenRepository).save(any(InscripcionExamen.class));
    }

    @Test
    void crear_deberiaLanzarErrorCuandoYaExisteActiva() {
        InscripcionExamenRequest request = crearRequest();
        Usuario alumno = crearAlumnoActivo();
        Examen examen = crearExamenActivo();

        when(usuarioRepository.findByLegajo("A001")).thenReturn(Optional.of(alumno));
        when(examenRepository.findById(10L)).thenReturn(Optional.of(examen));
        when(inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivoTrue(1L, 10L)).thenReturn(true);
        when(inscripcionExamenRepository.existsByAlumnoIdAndExamenIdAndActivoTrue(1L, 10L)).thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> inscripcionExamenService.crear(request)
        );

        verify(inscripcionExamenRepository, never()).save(any(InscripcionExamen.class));
    }

    @Test
    void crear_deberiaLanzarErrorCuandoElExamenYaPaso() {
        InscripcionExamenRequest request = crearRequest();
        Usuario alumno = crearAlumnoActivo();
        Examen examen = crearExamenActivo();
        examen.setFecha(LocalDate.now().minusDays(1));

        when(usuarioRepository.findByLegajo("A001")).thenReturn(Optional.of(alumno));
        when(examenRepository.findById(10L)).thenReturn(Optional.of(examen));

        assertThrows(
                IllegalArgumentException.class,
                () -> inscripcionExamenService.crear(request)
        );

        verify(inscripcionExamenRepository, never()).save(any(InscripcionExamen.class));
    }

    @Test
    void crear_deberiaLanzarErrorCuandoNoEstaInscriptoAlCurso() {
        InscripcionExamenRequest request = crearRequest();
        Usuario alumno = crearAlumnoActivo();
        Examen examen = crearExamenActivo();

        when(usuarioRepository.findByLegajo("A001")).thenReturn(Optional.of(alumno));
        when(examenRepository.findById(10L)).thenReturn(Optional.of(examen));
        when(inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivoTrue(1L, 10L)).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> inscripcionExamenService.crear(request)
        );

        verify(inscripcionExamenRepository, never()).save(any(InscripcionExamen.class));
    }

    @Test
    void cargarNota_deberiaGuardarLaNota() {
        InscripcionExamen inscripcion = crearInscripcionActiva();
        inscripcion.getExamen().setFecha(LocalDate.now());

        InscripcionExamenNotaRequest request = new InscripcionExamenNotaRequest();
        request.setNota(8);

        when(inscripcionExamenRepository.findById(100L)).thenReturn(Optional.of(inscripcion));
        when(inscripcionExamenRepository.save(any(InscripcionExamen.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InscripcionExamenResponse response = inscripcionExamenService.cargarNota(100L, request);

        assertEquals(8, response.getNota());
        assertEquals(8, inscripcion.getNota());
        verify(inscripcionExamenRepository).save(inscripcion);
    }

    @Test
    void cargarNota_deberiaLanzarErrorAntesDeLaFechaDelExamen() {
        InscripcionExamen inscripcion = crearInscripcionActiva();
        inscripcion.getExamen().setFecha(LocalDate.now().plusDays(1));

        InscripcionExamenNotaRequest request = new InscripcionExamenNotaRequest();
        request.setNota(8);

        when(inscripcionExamenRepository.findById(100L)).thenReturn(Optional.of(inscripcion));

        assertThrows(
                IllegalArgumentException.class,
                () -> inscripcionExamenService.cargarNota(100L, request)
        );

        verify(inscripcionExamenRepository, never()).save(any(InscripcionExamen.class));
    }

    @Test
    void cancelar_deberiaMarcarComoInactiva() {
        InscripcionExamen inscripcion = crearInscripcionActiva();

        when(inscripcionExamenRepository.findById(100L)).thenReturn(Optional.of(inscripcion));
        when(usuarioRepository.findByLegajo("A001")).thenReturn(Optional.of(crearAlumnoActivo()));
        when(inscripcionExamenRepository.save(any(InscripcionExamen.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        inscripcionExamenService.cancelar(100L);

        assertFalse(inscripcion.getActivo());
        verify(inscripcionExamenRepository).save(inscripcion);
        verify(inscripcionExamenRepository, never()).delete(any(InscripcionExamen.class));
    }

    @Test
    void cancelar_deberiaLanzarErrorCuandoYaTieneNota() {
        InscripcionExamen inscripcion = crearInscripcionActiva();
        inscripcion.setNota(8);

        when(inscripcionExamenRepository.findById(100L)).thenReturn(Optional.of(inscripcion));

        assertThrows(
                IllegalArgumentException.class,
                () -> inscripcionExamenService.cancelar(100L)
        );

        verify(inscripcionExamenRepository, never()).save(any(InscripcionExamen.class));
    }

    @Test
    void cancelar_deberiaLanzarErrorCuandoElExamenYaPaso() {
        InscripcionExamen inscripcion = crearInscripcionActiva();
        inscripcion.getExamen().setFecha(LocalDate.now().minusDays(1));

        when(inscripcionExamenRepository.findById(100L)).thenReturn(Optional.of(inscripcion));

        assertThrows(
                IllegalArgumentException.class,
                () -> inscripcionExamenService.cancelar(100L)
        );

        verify(inscripcionExamenRepository, never()).save(any(InscripcionExamen.class));
    }

    @Test
    void cancelar_deberiaLanzarErrorCuandoNoEsPropietario() {
        InscripcionExamen inscripcion = crearInscripcionActiva();

        Usuario otroAlumno = crearAlumnoActivo();
        otroAlumno.setId(2L);
        otroAlumno.setLegajo("A002");
        inscripcion.setAlumno(otroAlumno);

        when(inscripcionExamenRepository.findById(100L)).thenReturn(Optional.of(inscripcion));
        when(usuarioRepository.findByLegajo("A001")).thenReturn(Optional.of(crearAlumnoActivo()));

        assertThrows(
                AccessDeniedException.class,
                () -> inscripcionExamenService.cancelar(100L)
        );

        verify(inscripcionExamenRepository, never()).save(any(InscripcionExamen.class));
    }
}
