package com.ips.gestion_academica.controller;

import com.ips.gestion_academica.dto.inscripcion.InscripcionEstadoRequest;
import com.ips.gestion_academica.dto.inscripcion.InscripcionRequest;
import com.ips.gestion_academica.dto.inscripcion.InscripcionResponse;
import com.ips.gestion_academica.service.InscripcionService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @GetMapping
    public ResponseEntity<List<InscripcionResponse>> listarInscripciones() {
        return ResponseEntity.ok(
                inscripcionService.listarInscripciones()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<InscripcionResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                inscripcionService.buscarPorId(id)
        );
    }

    @PostMapping
    public ResponseEntity<InscripcionResponse> crearInscripcion(
            @Valid @RequestBody InscripcionRequest request) {

        InscripcionResponse response =
                inscripcionService.crearInscripcion(request);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<InscripcionResponse> modificarEstado(
            @PathVariable Long id,
            @Valid @RequestBody InscripcionEstadoRequest request) {

        InscripcionResponse response =
                inscripcionService.modificarEstado(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(
            @PathVariable Long id) {

        inscripcionService.darDeBaja(id);

        return ResponseEntity.noContent().build();
    }
}