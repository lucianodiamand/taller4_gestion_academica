package com.ips.gestion_academica.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ips.gestion_academica.dto.materia.MateriaRequest;
import com.ips.gestion_academica.dto.materia.MateriaResponse;
import com.ips.gestion_academica.service.MateriaService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/materias")
public class MateriaController {
    private final MateriaService materiaService;

    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @PostMapping
    public ResponseEntity<MateriaResponse> createMateria(@Valid @RequestBody MateriaRequest request) {
        MateriaResponse materiaCreada = materiaService.crearMateria(request);
        return ResponseEntity.ok(materiaCreada);
    }

    @GetMapping("{id}")
    public ResponseEntity<MateriaResponse> getMateriaById(@PathVariable("id") Long id) {
        MateriaResponse materia = materiaService.obtenerMateriaPorId(id);
        return ResponseEntity.ok(materia);
    }

    @GetMapping
    public ResponseEntity<List<MateriaResponse>> getMateriasActivas() {
        List<MateriaResponse> materias = materiaService.obtenerMateriasActivas();
        return ResponseEntity.ok(materias);
    }

    @PutMapping("{id}")
    public ResponseEntity<MateriaResponse> updateMateria(@PathVariable("id") Long id, @Valid @RequestBody MateriaRequest request) {
        MateriaResponse materiaActualizada = materiaService.actualizarMateria(id, request);
        return ResponseEntity.ok(materiaActualizada);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteMateria(@PathVariable("id") Long id) {
        materiaService.eliminarMateria(id);
        return ResponseEntity.noContent().build();
    }
}
