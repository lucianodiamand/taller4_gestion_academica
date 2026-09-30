package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.curso.CursoResumenResponse;
import com.ips.gestion_academica.dto.examen.ExamenRequest;
import com.ips.gestion_academica.dto.examen.ExamenResponse;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.model.Curso;
import com.ips.gestion_academica.model.Examen;
import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.repository.CursoRepository;
import com.ips.gestion_academica.repository.ExamenRepository;
import com.ips.gestion_academica.repository.UsuarioRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamenService {

    private final ExamenRepository examenRepository;
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;

    public ExamenService(
            ExamenRepository examenRepository,
            CursoRepository cursoRepository,
            UsuarioRepository usuarioRepository) {

        this.examenRepository = examenRepository;
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ExamenResponse> listarExamenes() {
        return examenRepository.findByActivoTrue()
                .stream()
                .filter(e -> esProfesorDelCurso(e.getCurso()))
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

        validarProfesorDelCurso(examen.getCurso());

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

        validarProfesorDelCurso(curso);

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

        validarProfesorDelCurso(examen.getCurso());

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

        validarProfesorDelCurso(curso);

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

        validarProfesorDelCurso(examen.getCurso());

        examen.setActivo(false);
        examenRepository.save(examen);
    }

    private boolean esProfesor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROFESOR"));
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String legajo = (String) auth.getPrincipal();
        return usuarioRepository.findByLegajo(legajo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", null));
    }

    private boolean esProfesorDelCurso(Curso curso) {
        if (!esProfesor()) {
            return true;
        }
        Usuario actual = obtenerUsuarioActual();
        return curso.getProfesor() != null && curso.getProfesor().getId().equals(actual.getId());
    }

    private void validarProfesorDelCurso(Curso curso) {
        if (!esProfesorDelCurso(curso)) {
            throw new AccessDeniedException("Solo el profesor de la comision puede realizar esta accion");
        }
    }

    private ExamenResponse convertirAResponse(Examen examen) {
        return new ExamenResponse(
                examen.getId(),
                examen.getFecha(),
                examen.getTipo(),
                examen.getDescripcion(),
                examen.getActivo(),
                convertirCursoAResumen(examen.getCurso()),
                examen.getCurso().getMateria().getNombre()
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