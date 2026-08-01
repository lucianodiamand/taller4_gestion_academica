package com.ips.gestion_academica.controller;

import com.ips.gestion_academica.dto.curso.CursoRequest;
import com.ips.gestion_academica.dto.curso.CursoResponse;
import com.ips.gestion_academica.service.CursoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    public List<CursoResponse> listarCursos() {
        return cursoService.listarCursos();
    }

    @GetMapping("/{id}")
        public ResponseEntity<CursoResponse> buscarPorId(@PathVariable Long id) {
            return ResponseEntity.ok(cursoService.buscarPorId(id));
    }
    @PostMapping
    public ResponseEntity<CursoResponse> crearCurso(@RequestBody CursoRequest curso) {
        CursoResponse cursoCreado = cursoService.crearCurso(curso);
        return ResponseEntity.ok(cursoCreado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoResponse> modificarCurso(
            @PathVariable Long id,
            @RequestBody CursoRequest curso) {

        CursoResponse cursoModificado =
                cursoService.modificarCurso(id, curso);

        return ResponseEntity.ok(cursoModificado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        cursoService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}