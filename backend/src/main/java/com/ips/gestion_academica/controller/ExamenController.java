package com.ips.gestion_academica.controller;

import com.ips.gestion_academica.dto.examen.ExamenRequest;
import com.ips.gestion_academica.dto.examen.ExamenResponse;
import com.ips.gestion_academica.service.ExamenService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/examenes")
public class ExamenController {

    private final ExamenService examenService;

    public ExamenController(ExamenService examenService) {
        this.examenService = examenService;
    }

    @GetMapping
    public ResponseEntity<List<ExamenResponse>> listarExamenes() {
        return ResponseEntity.ok(examenService.listarExamenes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamenResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(examenService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ExamenResponse> crearExamen(@Valid @RequestBody ExamenRequest request) {
        ExamenResponse response = examenService.crearExamen(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamenResponse> modificarExamen(
            @PathVariable Long id,
            @Valid @RequestBody ExamenRequest request) {

        ExamenResponse response = examenService.modificarExamen(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        examenService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}
