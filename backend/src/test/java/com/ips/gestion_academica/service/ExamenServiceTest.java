package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.examen.ExamenRequest;
import com.ips.gestion_academica.dto.examen.ExamenResponse;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.model.Curso;
import com.ips.gestion_academica.model.Examen;
import com.ips.gestion_academica.model.TipoExamen;
import com.ips.gestion_academica.repository.CursoRepository;
import com.ips.gestion_academica.repository.ExamenRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamenServiceTest {

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private CursoRepository cursoRepository;

    @InjectMocks
    private ExamenService examenService;

    private ExamenRequest crearRequestValido() {
        ExamenRequest request = new ExamenRequest();
        request.setFecha(LocalDate.of(2026, 8, 20));
        request.setTipo(TipoExamen.PARCIAL);
        request.setDescripcion("Primer parcial");
        request.setCursoId(10L);

        return request;
    }

    private Curso crearCursoActivo() {
        Curso curso = new Curso();
        curso.setId(10L);
        curso.setAnio(2026);
        curso.setCuatrimestre(2);
        curso.setComision("A");
        curso.setActivo(true);

        return curso;
    }

    private Examen crearExamenActivo() {
        Examen examen = new Examen();
        examen.setId(100L);
        examen.setFecha(LocalDate.of(2026, 8, 20));
        examen.setTipo(TipoExamen.PARCIAL);
        examen.setDescripcion("Primer parcial");
        examen.setCurso(crearCursoActivo());
        examen.setActivo(true);

        return examen;
    }

    @Test
    void crearExamen_deberiaCrearCorrectamente() {
        ExamenRequest request = crearRequestValido();
        Curso curso = crearCursoActivo();

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.of(curso));

        when(examenRepository.existsByCursoIdAndFechaAndTipo(
                10L,
                request.getFecha(),
                request.getTipo()
        )).thenReturn(false);

        when(examenRepository.save(any(Examen.class)))
                .thenAnswer(invocation -> {
                    Examen examen = invocation.getArgument(0);
                    examen.setId(100L);
                    return examen;
                });

        ExamenResponse response = examenService.crearExamen(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(LocalDate.of(2026, 8, 20), response.getFecha());
        assertEquals(TipoExamen.PARCIAL, response.getTipo());
        assertEquals("Primer parcial", response.getDescripcion());
        assertTrue(response.getActivo());

        assertNotNull(response.getCurso());
        assertEquals(10L, response.getCurso().getId());

        ArgumentCaptor<Examen> captor =
                ArgumentCaptor.forClass(Examen.class);

        verify(examenRepository).save(captor.capture());

        Examen examenGuardado = captor.getValue();

        assertSame(curso, examenGuardado.getCurso());
        assertTrue(examenGuardado.getActivo());
    }

    @Test
    void crearExamen_deberiaLanzarErrorCuandoCursoNoExiste() {
        ExamenRequest request = crearRequestValido();

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> examenService.crearExamen(request)
        );

        verify(examenRepository, never()).save(any());
    }

    @Test
    void crearExamen_deberiaLanzarErrorCuandoCursoEstaInactivo() {
        ExamenRequest request = crearRequestValido();

        Curso curso = crearCursoActivo();
        curso.setActivo(false);

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.of(curso));

        assertThrows(
                RecursoInactivoException.class,
                () -> examenService.crearExamen(request)
        );

        verify(examenRepository, never()).save(any());
    }

    @Test
    void crearExamen_deberiaLanzarErrorCuandoYaExiste() {
        ExamenRequest request = crearRequestValido();
        Curso curso = crearCursoActivo();

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.of(curso));

        when(examenRepository.existsByCursoIdAndFechaAndTipo(
                10L,
                request.getFecha(),
                request.getTipo()
        )).thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> examenService.crearExamen(request)
        );

        verify(examenRepository, never()).save(any());
    }

    @Test
    void listarExamenes_deberiaDevolverSoloActivos() {
        Examen examen = crearExamenActivo();

        when(examenRepository.findByActivoTrue())
                .thenReturn(List.of(examen));

        List<ExamenResponse> resultado =
                examenService.listarExamenes();

        assertEquals(1, resultado.size());
        assertEquals(100L, resultado.get(0).getId());
        assertEquals(TipoExamen.PARCIAL, resultado.get(0).getTipo());

        verify(examenRepository).findByActivoTrue();
    }

    @Test
    void buscarPorId_deberiaDevolverExamenActivo() {
        Examen examen = crearExamenActivo();

        when(examenRepository.findById(100L))
                .thenReturn(Optional.of(examen));

        ExamenResponse response =
                examenService.buscarPorId(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(TipoExamen.PARCIAL, response.getTipo());
        assertEquals(10L, response.getCurso().getId());
    }

    @Test
    void buscarPorId_deberiaLanzarErrorCuandoNoExiste() {
        when(examenRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> examenService.buscarPorId(999L)
        );
    }

    @Test
    void buscarPorId_deberiaLanzarErrorCuandoEstaInactivo() {
        Examen examen = crearExamenActivo();
        examen.setActivo(false);

        when(examenRepository.findById(100L))
                .thenReturn(Optional.of(examen));

        assertThrows(
                RecursoInactivoException.class,
                () -> examenService.buscarPorId(100L)
        );
    }

    @Test
    void modificarExamen_deberiaModificarCorrectamente() {
        Examen examen = crearExamenActivo();

        ExamenRequest request = crearRequestValido();
        request.setFecha(LocalDate.of(2026, 8, 25));
        request.setTipo(TipoExamen.RECUPERATORIO);
        request.setDescripcion("Recuperatorio");

        Curso curso = crearCursoActivo();

        when(examenRepository.findById(100L))
                .thenReturn(Optional.of(examen));

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.of(curso));

        when(examenRepository.existsByCursoIdAndFechaAndTipoAndIdNot(
                10L,
                request.getFecha(),
                request.getTipo(),
                100L
        )).thenReturn(false);

        when(examenRepository.save(any(Examen.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ExamenResponse response =
                examenService.modificarExamen(100L, request);

        assertEquals(LocalDate.of(2026, 8, 25), response.getFecha());
        assertEquals(TipoExamen.RECUPERATORIO, response.getTipo());
        assertEquals("Recuperatorio", response.getDescripcion());

        ArgumentCaptor<Examen> captor =
                ArgumentCaptor.forClass(Examen.class);

        verify(examenRepository).save(captor.capture());

        Examen examenModificado = captor.getValue();

        assertSame(curso, examenModificado.getCurso());
        assertTrue(examenModificado.getActivo());
    }

    @Test
    void modificarExamen_deberiaLanzarErrorCuandoNoExiste() {
        ExamenRequest request = crearRequestValido();

        when(examenRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> examenService.modificarExamen(999L, request)
        );

        verify(examenRepository, never()).save(any());
    }

    @Test
    void modificarExamen_deberiaLanzarErrorCuandoEstaInactivo() {
        Examen examen = crearExamenActivo();
        examen.setActivo(false);

        ExamenRequest request = crearRequestValido();

        when(examenRepository.findById(100L))
                .thenReturn(Optional.of(examen));

        assertThrows(
                RecursoInactivoException.class,
                () -> examenService.modificarExamen(100L, request)
        );

        verify(examenRepository, never()).save(any());
    }

    @Test
    void modificarExamen_deberiaLanzarErrorCuandoCursoNoExiste() {
        Examen examen = crearExamenActivo();
        ExamenRequest request = crearRequestValido();

        when(examenRepository.findById(100L))
                .thenReturn(Optional.of(examen));

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> examenService.modificarExamen(100L, request)
        );

        verify(examenRepository, never()).save(any());
    }

    @Test
    void modificarExamen_deberiaLanzarErrorCuandoCursoEstaInactivo() {
        Examen examen = crearExamenActivo();
        ExamenRequest request = crearRequestValido();

        Curso curso = crearCursoActivo();
        curso.setActivo(false);

        when(examenRepository.findById(100L))
                .thenReturn(Optional.of(examen));

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.of(curso));

        assertThrows(
                RecursoInactivoException.class,
                () -> examenService.modificarExamen(100L, request)
        );

        verify(examenRepository, never()).save(any());
    }

    @Test
    void modificarExamen_deberiaLanzarErrorCuandoQuedaDuplicado() {
        Examen examen = crearExamenActivo();
        ExamenRequest request = crearRequestValido();
        Curso curso = crearCursoActivo();

        when(examenRepository.findById(100L))
                .thenReturn(Optional.of(examen));

        when(cursoRepository.findById(10L))
                .thenReturn(Optional.of(curso));

        when(examenRepository.existsByCursoIdAndFechaAndTipoAndIdNot(
                10L,
                request.getFecha(),
                request.getTipo(),
                100L
        )).thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> examenService.modificarExamen(100L, request)
        );

        verify(examenRepository, never()).save(any());
    }

    @Test
    void darDeBaja_deberiaMarcarExamenComoInactivo() {
        Examen examen = crearExamenActivo();

        when(examenRepository.findById(100L))
                .thenReturn(Optional.of(examen));

        when(examenRepository.save(any(Examen.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        examenService.darDeBaja(100L);

        assertFalse(examen.getActivo());

        verify(examenRepository).save(examen);
    }

    @Test
    void darDeBaja_deberiaLanzarErrorCuandoNoExiste() {
        when(examenRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> examenService.darDeBaja(999L)
        );

        verify(examenRepository, never()).save(any());
    }

    @Test
    void darDeBaja_deberiaLanzarErrorCuandoYaEstaInactivo() {
        Examen examen = crearExamenActivo();
        examen.setActivo(false);

        when(examenRepository.findById(100L))
                .thenReturn(Optional.of(examen));

        assertThrows(
                RecursoInactivoException.class,
                () -> examenService.darDeBaja(100L)
        );

        verify(examenRepository, never()).save(any());
    }
}