package com.ips.gestion_academica.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ips.gestion_academica.dto.inscripcionexamen.InscripcionExamenNotaRequest;
import com.ips.gestion_academica.dto.inscripcionexamen.InscripcionExamenRequest;
import com.ips.gestion_academica.dto.inscripcionexamen.InscripcionExamenResponse;
import com.ips.gestion_academica.service.InscripcionExamenService;

@RestController
@RequestMapping("/api/inscripciones-examen")
public class InscripcionExamenController {

    private final InscripcionExamenService inscripcionExamenService;

    public InscripcionExamenController(InscripcionExamenService inscripcionExamenService) {
        this.inscripcionExamenService = inscripcionExamenService;
    }

    @GetMapping
    public ResponseEntity<List<InscripcionExamenResponse>> listar() {
        return ResponseEntity.ok(inscripcionExamenService.listar());
    }

    @PostMapping
    public ResponseEntity<InscripcionExamenResponse> crear(
            @Valid @RequestBody InscripcionExamenRequest request) {
        return ResponseEntity.ok(inscripcionExamenService.crear(request));
    }

    @PutMapping("/{id}/nota")
    public ResponseEntity<InscripcionExamenResponse> cargarNota(
            @PathVariable Long id,
            @Valid @RequestBody InscripcionExamenNotaRequest request) {
        return ResponseEntity.ok(inscripcionExamenService.cargarNota(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        inscripcionExamenService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
