package com.ips.gestion_academica.controller;

import com.ips.gestion_academica.dto.curso.CursoResumenResponse;
import com.ips.gestion_academica.dto.examen.ExamenRequest;
import com.ips.gestion_academica.dto.examen.ExamenResponse;
import com.ips.gestion_academica.model.TipoExamen;
import com.ips.gestion_academica.service.ExamenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ExamenControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExamenService examenService;

    @InjectMocks
    private ExamenController examenController;

    private ExamenResponse examenResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(examenController).build();

        CursoResumenResponse cursoResumen = new CursoResumenResponse(1L, 2026, 1, "A");
        examenResponse = new ExamenResponse(
                1L,
                LocalDate.of(2026, 10, 15),
                TipoExamen.PARCIAL,
                "Primer Parcial",
                true,
                cursoResumen
        );
    }

    @Test
    void listarExamenes_deberiaRetornarListaDeExamenes() throws Exception {
        when(examenService.listarExamenes()).thenReturn(List.of(examenResponse));

        mockMvc.perform(get("/api/examenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].tipo").value("PARCIAL"))
                .andExpect(jsonPath("$[0].descripcion").value("Primer Parcial"));

        verify(examenService).listarExamenes();
    }

    @Test
    void buscarPorId_deberiaRetornarExamen() throws Exception {
        when(examenService.buscarPorId(1L)).thenReturn(examenResponse);

        mockMvc.perform(get("/api/examenes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tipo").value("PARCIAL"));

        verify(examenService).buscarPorId(1L);
    }

    @Test
    void crearExamen_deberiaCrearYRetornarExamen() throws Exception {
        String jsonRequest = """
                {
                    "fecha": "2026-10-15",
                    "tipo": "PARCIAL",
                    "descripcion": "Primer Parcial",
                    "cursoId": 1
                }
                """;

        when(examenService.crearExamen(any(ExamenRequest.class))).thenReturn(examenResponse);

        mockMvc.perform(post("/api/examenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tipo").value("PARCIAL"));

        verify(examenService).crearExamen(any(ExamenRequest.class));
    }

    @Test
    void modificarExamen_deberiaModificarYRetornarExamen() throws Exception {
        String jsonRequest = """
                {
                    "fecha": "2026-10-15",
                    "tipo": "PARCIAL",
                    "descripcion": "Primer Parcial Actualizado",
                    "cursoId": 1
                }
                """;

        when(examenService.modificarExamen(eq(1L), any(ExamenRequest.class))).thenReturn(examenResponse);

        mockMvc.perform(put("/api/examenes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(examenService).modificarExamen(eq(1L), any(ExamenRequest.class));
    }

    @Test
    void darDeBaja_deberiaRetornarNoContent() throws Exception {
        doNothing().when(examenService).darDeBaja(1L);

        mockMvc.perform(delete("/api/examenes/1"))
                .andExpect(status().isNoContent());

        verify(examenService).darDeBaja(1L);
    }
}
