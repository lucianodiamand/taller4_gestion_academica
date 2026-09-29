package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.materia.MateriaRequest;
import com.ips.gestion_academica.dto.materia.MateriaResponse;
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
        request.setNombre("Matematica I");
        request.setDescripcion("Algebra y geometria");
        request.setContenido("Unidades 1 a 4");
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

    private Materia crearMateria(String codigo) {
        Materia materia = new Materia();
        materia.setId(1L);
        materia.setCodigo(codigo);
        materia.setNombre("Materia");
        materia.setAnioCursada(1);
        materia.setActivo(true);
        return materia;
    }

    @Test
    void crearMateria_deberiaCrearMateriaConCodigoAutogenerado() {
        MateriaRequest request = crearRequestValido();

        when(materiaRepository.findAll()).thenReturn(List.of());

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
        assertEquals("Unidades 1 a 4", response.getContenido());
        assertEquals(1, response.getAnioCursada());
        assertTrue(response.getActivo());

        ArgumentCaptor<Materia> captor = ArgumentCaptor.forClass(Materia.class);
        verify(materiaRepository).save(captor.capture());

        Materia materiaGuardada = captor.getValue();
        assertEquals("MAT-001", materiaGuardada.getCodigo());
        assertEquals("Matematica I", materiaGuardada.getNombre());
        assertEquals("Unidades 1 a 4", materiaGuardada.getContenido());
        assertEquals(1, materiaGuardada.getAnioCursada());
        assertTrue(materiaGuardada.getActivo());
    }

    @Test
    void crearMateria_deberiaGenerarCodigoIncremental() {
        MateriaRequest request = crearRequestValido();

        when(materiaRepository.findAll())
                .thenReturn(List.of(crearMateria("MAT-001"), crearMateria("MAT-002")));

        when(materiaRepository.save(any(Materia.class)))
                .thenAnswer(invocation -> {
                    Materia m = invocation.getArgument(0);
                    m.setId(10L);
                    return m;
                });

        MateriaResponse response = materiaService.crearMateria(request);

        assertEquals("MAT-003", response.getCodigo());
    }

    @Test
    void crearMateria_deberiaNoColisionarConMateriaInactiva() {
        MateriaRequest request = crearRequestValido();

        Materia inactiva = crearMateria("MAT-002");
        inactiva.setActivo(false);

        when(materiaRepository.findAll())
                .thenReturn(List.of(crearMateria("MAT-001"), inactiva));

        when(materiaRepository.save(any(Materia.class)))
                .thenAnswer(invocation -> {
                    Materia m = invocation.getArgument(0);
                    m.setId(10L);
                    return m;
                });

        MateriaResponse response = materiaService.crearMateria(request);

        assertEquals("MAT-003", response.getCodigo());
    }

    @Test
    void obtenerProximoCodigo_deberiaIgnorarCodigosConFormatoDistinto() {
        when(materiaRepository.findAll())
                .thenReturn(List.of(crearMateria("MAT-005"), crearMateria("LEGACY"), crearMateria("OTRO-999")));

        assertEquals("MAT-006", materiaService.obtenerProximoCodigo());
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
    void buscarPorId_deberiaLanzarErrorCuandoMateriaEstaInactiva() {
        Materia materia = crearMateriaActiva();
        materia.setActivo(false);

        when(materiaRepository.findById(1L))
                .thenReturn(Optional.of(materia));

        assertThrows(
                RecursoInactivoException.class,
                () -> materiaService.obtenerMateriaPorId(1L)
        );
    }

    @Test
    void modificarMateria_deberiaModificarMateriaSinCambiarCodigo() {
        Materia materiaExistente = crearMateriaActiva();

        MateriaRequest request = crearRequestValido();
        request.setNombre("Matematica II");
        request.setDescripcion("Calculo avanzado");
        request.setAnioCursada(2);

        when(materiaRepository.findById(1L))
                .thenReturn(Optional.of(materiaExistente));

        when(materiaRepository.save(any(Materia.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MateriaResponse response = materiaService.actualizarMateria(1L, request);

        assertNotNull(response);
        assertEquals("MAT-001", response.getCodigo());
        assertEquals("Matematica II", response.getNombre());
        assertEquals("Calculo avanzado", response.getDescripcion());
        assertEquals(2, response.getAnioCursada());

        ArgumentCaptor<Materia> captor = ArgumentCaptor.forClass(Materia.class);
        verify(materiaRepository).save(captor.capture());

        Materia materiaModificada = captor.getValue();
        assertEquals("MAT-001", materiaModificada.getCodigo());
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
    void modificarMateria_deberiaLanzarErrorCuandoMateriaEstaInactiva() {
        Materia materia = crearMateriaActiva();
        materia.setActivo(false);
        MateriaRequest request = crearRequestValido();

        when(materiaRepository.findById(1L))
                .thenReturn(Optional.of(materia));

        assertThrows(
                RecursoInactivoException.class,
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
