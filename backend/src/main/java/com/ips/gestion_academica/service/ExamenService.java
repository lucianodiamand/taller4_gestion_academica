package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.curso.CursoResumenResponse;
import com.ips.gestion_academica.dto.examen.ExamenRequest;
import com.ips.gestion_academica.dto.examen.ExamenResponse;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.model.Curso;
import com.ips.gestion_academica.model.Examen;
import com.ips.gestion_academica.repository.CursoRepository;
import com.ips.gestion_academica.repository.ExamenRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamenService {

    private final ExamenRepository examenRepository;
    private final CursoRepository cursoRepository;

    public ExamenService(
            ExamenRepository examenRepository,
            CursoRepository cursoRepository) {

        this.examenRepository = examenRepository;
        this.cursoRepository = cursoRepository;
    }

    public List<ExamenResponse> listarExamenes() {
        return examenRepository.findByActivoTrue()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public ExamenResponse buscarPorId(Long id) {
        Examen examen = examenRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Examen", id)
                );

        if (!Boolean.TRUE.equals(examen.getActivo())) {
            throw new RecursoInactivoException(
                    "El examen con ID ",
                    id
            );
        }

        return convertirAResponse(examen);
    }

    public ExamenResponse crearExamen(ExamenRequest request) {
        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Curso",
                                request.getCursoId()
                        )
                );

        if (!Boolean.TRUE.equals(curso.getActivo())) {
            throw new RecursoInactivoException(
                    "El curso con ID ",
                    curso.getId()
            );
        }

        if (examenRepository
                .existsByCursoIdAndFechaAndTipo(
                        curso.getId(),
                        request.getFecha(),
                        request.getTipo()
                )) {

            throw new RecursoDuplicadoException(
                    "Ya existe un examen para ese curso, fecha y tipo"
            );
        }

        Examen examen = new Examen();
        examen.setFecha(request.getFecha());
        examen.setTipo(request.getTipo());
        examen.setDescripcion(request.getDescripcion());
        examen.setCurso(curso);
        examen.setActivo(true);

        Examen examenGuardado = examenRepository.save(examen);

        return convertirAResponse(examenGuardado);
    }

    public ExamenResponse modificarExamen(
            Long id,
            ExamenRequest request) {

        Examen examen = examenRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Examen", id)
                );

        if (!Boolean.TRUE.equals(examen.getActivo())) {
            throw new RecursoInactivoException(
                    "El examen con ID ",
                    id
            );
        }

        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Curso",
                                request.getCursoId()
                        )
                );

        if (!Boolean.TRUE.equals(curso.getActivo())) {
            throw new RecursoInactivoException(
                    "El curso con ID ",
                    curso.getId()
            );
        }

        if (examenRepository
                .existsByCursoIdAndFechaAndTipoAndIdNot(
                        curso.getId(),
                        request.getFecha(),
                        request.getTipo(),
                        id
                )) {

            throw new RecursoDuplicadoException(
                    "Ya existe otro examen para ese curso, fecha y tipo"
            );
        }

        examen.setFecha(request.getFecha());
        examen.setTipo(request.getTipo());
        examen.setDescripcion(request.getDescripcion());
        examen.setCurso(curso);

        Examen examenGuardado = examenRepository.save(examen);

        return convertirAResponse(examenGuardado);
    }

    public void darDeBaja(Long id) {
        Examen examen = examenRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Examen", id)
                );

        if (!Boolean.TRUE.equals(examen.getActivo())) {
            throw new RecursoInactivoException(
                    "El examen con ID ",
                    id
            );
        }

        examen.setActivo(false);
        examenRepository.save(examen);
    }

    private ExamenResponse convertirAResponse(Examen examen) {
        return new ExamenResponse(
                examen.getId(),
                examen.getFecha(),
                examen.getTipo(),
                examen.getDescripcion(),
                examen.getActivo(),
                convertirCursoAResumen(examen.getCurso())
        );
    }

    private CursoResumenResponse convertirCursoAResumen(Curso curso) {
        return new CursoResumenResponse(
                curso.getId(),
                curso.getAnio(),
                curso.getCuatrimestre(),
                curso.getComision()
        );
    }
}