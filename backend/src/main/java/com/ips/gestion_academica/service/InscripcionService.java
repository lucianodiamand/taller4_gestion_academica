package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.curso.CursoResumenResponse;
import com.ips.gestion_academica.dto.inscripcion.InscripcionEstadoRequest;
import com.ips.gestion_academica.dto.inscripcion.InscripcionRequest;
import com.ips.gestion_academica.dto.inscripcion.InscripcionResponse;
import com.ips.gestion_academica.dto.usuario.UsuarioResumeResponse;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.model.Curso;
import com.ips.gestion_academica.model.EstadoInscripcion;
import com.ips.gestion_academica.model.Inscripcion;
import com.ips.gestion_academica.model.Rol;
import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.repository.CursoRepository;
import com.ips.gestion_academica.repository.InscripcionRepository;
import com.ips.gestion_academica.repository.UsuarioRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;

    public InscripcionService(
            InscripcionRepository inscripcionRepository,
            UsuarioRepository usuarioRepository,
            CursoRepository cursoRepository) {

        this.inscripcionRepository = inscripcionRepository;
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
    }

    public InscripcionResponse crearInscripcion(InscripcionRequest request) {

        Usuario alumno = usuarioRepository.findById(request.getAlumnoId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Alumno",
                                request.getAlumnoId()
                        )
                );

        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Curso",
                                request.getCursoId()
                        )
                );

        if (!Boolean.TRUE.equals(alumno.getActivo())) {
            throw new RecursoInactivoException(
                    "El alumno con ID ",
                    alumno.getId()
            );
        }

        if (alumno.getRol() != Rol.ALUMNO) {
            throw new IllegalArgumentException(
                    "El usuario seleccionado no tiene rol de alumno"
            );
        }

        if (!Boolean.TRUE.equals(curso.getActivo())) {
            throw new RecursoInactivoException(
                    "El curso con ID ",
                    curso.getId()
            );
        }

        if (inscripcionRepository.existsByAlumnoIdAndCursoId(
                alumno.getId(),
                curso.getId())) {

            throw new RecursoDuplicadoException(
                    "El alumno ya se encuentra inscripto en ese curso"
            );
        }

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setAlumno(alumno);
        inscripcion.setCurso(curso);
        inscripcion.setFechaInscripcion(LocalDate.now());
        inscripcion.setEstado(EstadoInscripcion.INSCRIPTO);
        inscripcion.setActivo(true);

        Inscripcion inscripcionGuardada =
                inscripcionRepository.save(inscripcion);

        return convertirAResponse(inscripcionGuardada);
    }

    public List<InscripcionResponse> listarInscripciones() {
        return inscripcionRepository.findByActivoTrue()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public InscripcionResponse buscarPorId(Long id) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Inscripción",
                                id
                        )
                );

        if (!Boolean.TRUE.equals(inscripcion.getActivo())) {
            throw new RecursoInactivoException(
                    "La inscripción con ID ",
                    id
            );
        }

        return convertirAResponse(inscripcion);
    }

    public InscripcionResponse modificarInscripcion(
            Long id,
            InscripcionRequest request) {

        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Inscripción",
                                id
                        )
                );

        if (!Boolean.TRUE.equals(inscripcion.getActivo())) {
            throw new RecursoInactivoException(
                    "La inscripción con ID ",
                    id
            );
        }

        Usuario alumno = usuarioRepository.findById(request.getAlumnoId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Alumno",
                                request.getAlumnoId()
                        )
                );

        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Curso",
                                request.getCursoId()
                        )
                );

        if (!Boolean.TRUE.equals(alumno.getActivo())) {
            throw new RecursoInactivoException(
                    "El alumno con ID ",
                    alumno.getId()
            );
        }

        if (alumno.getRol() != Rol.ALUMNO) {
            throw new IllegalArgumentException(
                    "El usuario seleccionado no tiene rol de alumno"
            );
        }

        if (!Boolean.TRUE.equals(curso.getActivo())) {
            throw new RecursoInactivoException(
                    "El curso con ID ",
                    curso.getId()
            );
        }

        if (inscripcionRepository
                .existsByAlumnoIdAndCursoIdAndIdNot(
                        alumno.getId(),
                        curso.getId(),
                        id)) {

            throw new RecursoDuplicadoException(
                    "Ya existe otra inscripción para ese alumno y curso"
            );
        }

        inscripcion.setAlumno(alumno);
        inscripcion.setCurso(curso);

        Inscripcion inscripcionGuardada =
                inscripcionRepository.save(inscripcion);

        return convertirAResponse(inscripcionGuardada);
    }

    public InscripcionResponse modificarEstado(
        Long id,
        InscripcionEstadoRequest request) {

        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Inscripción",
                                id
                        )
                );

        if (!Boolean.TRUE.equals(inscripcion.getActivo())) {
            throw new RecursoInactivoException(
                    "La inscripción con ID ",
                    id
            );
        }

        inscripcion.setEstado(request.getEstado());

        return convertirAResponse(
                inscripcionRepository.save(inscripcion)
        );
    }

    public void darDeBaja(Long id) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Inscripción",
                                id
                        )
                );

        if (!Boolean.TRUE.equals(inscripcion.getActivo())) {
            throw new RecursoInactivoException(
                    "La inscripción con ID ",
                    id
            );
        }

        inscripcion.setEstado(EstadoInscripcion.CANCELADO);
        inscripcion.setActivo(false);

        inscripcionRepository.save(inscripcion);
    }

    private InscripcionResponse convertirAResponse(
            Inscripcion inscripcion) {

        return new InscripcionResponse(
                inscripcion.getId(),
                inscripcion.getFechaInscripcion(),
                inscripcion.getEstado(),
                inscripcion.getActivo(),
                convertirAlumnoAResumen(inscripcion.getAlumno()),
                convertirCursoAResumen(inscripcion.getCurso())
        );
    }

    private UsuarioResumeResponse convertirAlumnoAResumen(
            Usuario alumno) {

        return new UsuarioResumeResponse(
                alumno.getId(),
                alumno.getNombre(),
                alumno.getApellido(),
                alumno.getLegajo(),
                alumno.getEmail()
        );
    }

    private CursoResumenResponse convertirCursoAResumen(
            Curso curso) {

        return new CursoResumenResponse(
                curso.getId(),
                curso.getAnio(),
                curso.getCuatrimestre(),
                curso.getComision()
        );
    }
}