package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.materia.MateriaRequest;
import com.ips.gestion_academica.dto.materia.MateriaResponse;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.model.Materia;
import com.ips.gestion_academica.repository.MateriaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MateriaServiceTest {
    @Mock
    private MateriaRepository materiaRepository;

    @InjectMocks
    private MateriaService materiaService;

    private MateriaRequest crearRequestValido() {
        MateriaRequest request = new MateriaRequest();
        request.setCodigo("MAT-001");
        request.setNombre("Matematica I");
        request.setDescripcion("Algebra y geometria");
        request.setAnioCursada(1);
        return request;
    }

    private Materia crearMateriaActiva() {
        Materia materia = new Materia();
        materia.setId(1L);
        materia.setCodigo("MAT-001");
        materia.setNombre("Matematica I");
        materia.setDescripcion("Algebra y geometria");
        materia.setAnioCursada(1);
        materia.setActivo(true);
        return materia;
    }

    @Test
    void crearMateria_deberiaCrearMateriaCorrectamente() {
        MateriaRequest request = crearRequestValido();

        when(materiaRepository.existsByCodigo(request.getCodigo()))
                .thenReturn(false);

        when(materiaRepository.save(any(Materia.class)))
                .thenAnswer(invocation -> {
                    Materia materia = invocation.getArgument(0);
                    materia.setId(1L);
                    return materia;
                });

        MateriaResponse response = materiaService.crearMateria(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("MAT-001", response.getCodigo());
        assertEquals("Matematica I", response.getNombre());
        assertEquals("Algebra y geometria", response.getDescripcion());
        assertEquals(1, response.getAnioCursada());
        assertTrue(response.getActivo());

        ArgumentCaptor<Materia> captor = ArgumentCaptor.forClass(Materia.class);
        verify(materiaRepository).save(captor.capture());

        Materia materiaGuardada = captor.getValue();
        assertEquals("MAT-001", materiaGuardada.getCodigo());
        assertEquals("Matematica I", materiaGuardada.getNombre());
        assertEquals(1, materiaGuardada.getAnioCursada());
        assertTrue(materiaGuardada.getActivo());
    }

@Test
    void crearMateria_deberiaLanzarErrorCuandoCodigoEstaDuplicado() {
        MateriaRequest request = crearRequestValido();

        when(materiaRepository.existsByCodigo(request.getCodigo()))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> materiaService.crearMateria(request)
        );

        verify(materiaRepository, never()).save(any(Materia.class));
    }

    @Test
    void crearMateria_deberiaGuardarLosDatosCorrectos() {
        MateriaRequest request = crearRequestValido();
        request.setCodigo("FISI1");
        request.setNombre("Fisica I");
        request.setDescripcion("MRU :P");
        request.setAnioCursada(2);

        when(materiaRepository.existsByCodigo(request.getCodigo()))
                .thenReturn(false);

        when(materiaRepository.save(any(Materia.class)))
                .thenAnswer(invocation -> {
                    Materia materia = invocation.getArgument(0);
                    materia.setId(2L);
                    return materia;
                });

        materiaService.crearMateria(request);

        ArgumentCaptor<Materia> captor = ArgumentCaptor.forClass(Materia.class);
        verify(materiaRepository).save(captor.capture());

        Materia materiaGuardada = captor.getValue();
        assertEquals("FISI1", materiaGuardada.getCodigo());
        assertEquals("Fisica I", materiaGuardada.getNombre());
        assertEquals("MRU :P", materiaGuardada.getDescripcion());
        assertEquals(2, materiaGuardada.getAnioCursada());
        assertTrue(materiaGuardada.getActivo());
    }

    @Test
    void listarMaterias_deberiaDevolverSoloMateriasActivas() {
        Materia materia = crearMateriaActiva();

        when(materiaRepository.findByActivoTrue())
                .thenReturn(List.of(materia));

        List<MateriaResponse> resultado = materiaService.obtenerMateriasActivas();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("MAT-001", resultado.get(0).getCodigo());
        assertEquals("Matematica I", resultado.get(0).getNombre());

        verify(materiaRepository).findByActivoTrue();
    }

    @Test
    void buscarPorId_deberiaDevolverMateriaActiva() {
        Materia materia = crearMateriaActiva();

        when(materiaRepository.findById(1L))
                .thenReturn(Optional.of(materia));

        MateriaResponse response = materiaService.obtenerMateriaPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("MAT-001", response.getCodigo());
        assertEquals("Matematica I", response.getNombre());
    }

    @Test
    void buscarPorId_deberiaLanzarErrorCuandoMateriaNoExiste() {
        when(materiaRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> materiaService.obtenerMateriaPorId(999L)
        );
    }

    @Test
    void modificarMateria_deberiaModificarMateriaCorrectamente() {
        Materia materiaExistente = crearMateriaActiva();

        MateriaRequest request = crearRequestValido();
        request.setCodigo("MATE2");
        request.setNombre("Matematica II");
        request.setDescripcion("Calculo avanzado");
        request.setAnioCursada(2);

        when(materiaRepository.findById(1L))
                .thenReturn(Optional.of(materiaExistente));

        when(materiaRepository.existsByCodigo(request.getCodigo()))
                .thenReturn(false);

        when(materiaRepository.save(any(Materia.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MateriaResponse response = materiaService.actualizarMateria(1L, request);

        assertNotNull(response);
        assertEquals("MATE2", response.getCodigo());
        assertEquals("Matematica II", response.getNombre());
        assertEquals("Calculo avanzado", response.getDescripcion());
        assertEquals(2, response.getAnioCursada());

        ArgumentCaptor<Materia> captor = ArgumentCaptor.forClass(Materia.class);
        verify(materiaRepository).save(captor.capture());

        Materia materiaModificada = captor.getValue();
        assertEquals("MATE2", materiaModificada.getCodigo());
        assertEquals("Matematica II", materiaModificada.getNombre());
        assertEquals(2, materiaModificada.getAnioCursada());
        assertTrue(materiaModificada.getActivo());
    }

    @Test
    void modificarMateria_deberiaLanzarErrorCuandoMateriaNoExiste() {
        MateriaRequest request = crearRequestValido();

        when(materiaRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> materiaService.actualizarMateria(999L, request)
        );

        verify(materiaRepository, never()).save(any(Materia.class));
    }

    @Test
    void modificarMateria_deberiaLanzarErrorCuandoCodigoPerteneceAOtraMateria() {
        Materia materiaExistente = crearMateriaActiva();
        MateriaRequest request = crearRequestValido();
        request.setCodigo("OTRO");

        when(materiaRepository.findById(1L))
                .thenReturn(Optional.of(materiaExistente));

        when(materiaRepository.existsByCodigo(request.getCodigo()))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> materiaService.actualizarMateria(1L, request)
        );

        verify(materiaRepository, never()).save(any(Materia.class));
    }

    @Test
    void darDeBaja_deberiaMarcarMateriaComoInactiva() {
        Materia materia = crearMateriaActiva();

        when(materiaRepository.findById(1L))
                .thenReturn(Optional.of(materia));

        when(materiaRepository.save(any(Materia.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        materiaService.eliminarMateria(1L);

        assertFalse(materia.getActivo());
        verify(materiaRepository).save(materia);
    }

    @Test
    void darDeBaja_deberiaLanzarErrorCuandoMateriaNoExiste() {
        when(materiaRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> materiaService.eliminarMateria(999L)
        );

        verify(materiaRepository, never()).save(any(Materia.class));
    }

    @Test
    void darDeBaja_deberiaLanzarErrorCuandoMateriaYaEstaInactiva() {
        Materia materia = crearMateriaActiva();
        materia.setActivo(false);

        when(materiaRepository.findById(1L))
                .thenReturn(Optional.of(materia));

        assertThrows(
                RecursoInactivoException.class,
                () -> materiaService.eliminarMateria(1L)
        );

        verify(materiaRepository, never()).save(any(Materia.class));
    }

}
