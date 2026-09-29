package com.ips.gestion_academica.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ips.gestion_academica.dto.curso.CursoResumenResponse;
import com.ips.gestion_academica.dto.examen.ExamenResponse;
import com.ips.gestion_academica.dto.inscripcionexamen.InscripcionExamenNotaRequest;
import com.ips.gestion_academica.dto.inscripcionexamen.InscripcionExamenRequest;
import com.ips.gestion_academica.dto.inscripcionexamen.InscripcionExamenResponse;
import com.ips.gestion_academica.dto.usuario.UsuarioResumeResponse;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.model.Curso;
import com.ips.gestion_academica.model.Examen;
import com.ips.gestion_academica.model.InscripcionExamen;
import com.ips.gestion_academica.model.Rol;
import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.repository.ExamenRepository;
import com.ips.gestion_academica.repository.InscripcionExamenRepository;
import com.ips.gestion_academica.repository.InscripcionRepository;
import com.ips.gestion_academica.repository.UsuarioRepository;

@Service
public class InscripcionExamenService {

    private final InscripcionExamenRepository inscripcionExamenRepository;
    private final InscripcionRepository inscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExamenRepository examenRepository;

    public InscripcionExamenService(
            InscripcionExamenRepository inscripcionExamenRepository,
            InscripcionRepository inscripcionRepository,
            UsuarioRepository usuarioRepository,
            ExamenRepository examenRepository) {

        this.inscripcionExamenRepository = inscripcionExamenRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.usuarioRepository = usuarioRepository;
        this.examenRepository = examenRepository;
    }

    public InscripcionExamenResponse crear(InscripcionExamenRequest request) {
        Usuario alumno;
        if (esAlumno()) {
            alumno = obtenerUsuarioActual();
        } else {
            if (request.getAlumnoId() == null) {
                throw new IllegalArgumentException("Debe seleccionar un alumno");
            }
            alumno = usuarioRepository.findById(request.getAlumnoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("alumno", request.getAlumnoId()));
        }

        if (!Boolean.TRUE.equals(alumno.getActivo())) {
            throw new RecursoInactivoException("alumno", alumno.getId());
        }

        if (alumno.getRol() != Rol.ALUMNO) {
            throw new IllegalArgumentException("El usuario seleccionado no tiene rol de alumno");
        }

        Examen examen = examenRepository.findById(request.getExamenId())
                .orElseThrow(() -> new RecursoNoEncontradoException("examen", request.getExamenId()));

        if (!Boolean.TRUE.equals(examen.getActivo())) {
            throw new RecursoInactivoException("examen", examen.getId());
        }

        if (examen.getFecha().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede anotar a un examen cuya fecha ya paso");
        }

        if (!inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivoTrue(alumno.getId(), examen.getCurso().getId())) {
            throw new IllegalArgumentException("No podes anotarte a un examen de un curso al que no estas inscripto");
        }

        if (inscripcionExamenRepository.existsByAlumnoIdAndExamenIdAndActivoTrue(alumno.getId(), examen.getId())) {
            throw new RecursoDuplicadoException("El alumno ya esta inscripto a ese examen");
        }

        InscripcionExamen inscripcion = new InscripcionExamen();
        inscripcion.setAlumno(alumno);
        inscripcion.setExamen(examen);
        inscripcion.setFechaInscripcion(LocalDate.now());
        inscripcion.setActivo(true);

        return convertirAResponse(inscripcionExamenRepository.save(inscripcion));
    }

    public List<InscripcionExamenResponse> listar() {
        if (esAlumno()) {
            Usuario alumno = obtenerUsuarioActual();
            return inscripcionExamenRepository.findByAlumnoId(alumno.getId()).stream()
                    .filter(InscripcionExamen::getActivo)
                    .map(this::convertirAResponse)
                    .toList();
        }
        return inscripcionExamenRepository.findByActivoTrue().stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public InscripcionExamenResponse cargarNota(Long id, InscripcionExamenNotaRequest request) {
        InscripcionExamen inscripcion = inscripcionExamenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("inscripcion a examen", id));

        if (!Boolean.TRUE.equals(inscripcion.getActivo())) {
            throw new RecursoInactivoException("inscripcion a examen", id);
        }

        if (inscripcion.getExamen().getFecha().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede cargar la nota antes de la fecha del examen");
        }

        inscripcion.setNota(request.getNota());

        return convertirAResponse(inscripcionExamenRepository.save(inscripcion));
    }

    public void cancelar(Long id) {
        InscripcionExamen inscripcion = inscripcionExamenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("inscripcion a examen", id));

        if (!Boolean.TRUE.equals(inscripcion.getActivo())) {
            throw new RecursoInactivoException("inscripcion a examen", id);
        }

        if (inscripcion.getNota() != null) {
            throw new IllegalArgumentException("No se puede cancelar una inscripcion que ya tiene nota cargada");
        }

        if (inscripcion.getExamen().getFecha().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede cancelar una inscripcion a un examen cuya fecha ya paso");
        }

        if (esAlumno()) {
            Usuario actual = obtenerUsuarioActual();
            if (!inscripcion.getAlumno().getId().equals(actual.getId())) {
                throw new AccessDeniedException("Un alumno solo puede cancelar su propia inscripcion");
            }
        }

        // baja logica
        inscripcion.setActivo(false);
        inscripcionExamenRepository.save(inscripcion);
    }

    private boolean esAlumno() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ALUMNO"));
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String legajo = (String) auth.getPrincipal();
        return usuarioRepository.findByLegajo(legajo)
                .orElseThrow(() -> new RecursoNoEncontradoException("usuario", null));
    }

    private InscripcionExamenResponse convertirAResponse(InscripcionExamen inscripcion) {
        return new InscripcionExamenResponse(
                inscripcion.getId(),
                inscripcion.getFechaInscripcion(),
                inscripcion.getNota(),
                inscripcion.getActivo(),
                convertirAlumnoAResumen(inscripcion.getAlumno()),
                convertirExamenAResumen(inscripcion.getExamen())
        );
    }

    private UsuarioResumeResponse convertirAlumnoAResumen(Usuario alumno) {
        return new UsuarioResumeResponse(
                alumno.getId(),
                alumno.getNombre(),
                alumno.getApellido(),
                alumno.getLegajo(),
                alumno.getEmail()
        );
    }

    private ExamenResponse convertirExamenAResumen(Examen examen) {
        Curso curso = examen.getCurso();
        CursoResumenResponse cursoResumen = new CursoResumenResponse(
                curso.getId(),
                curso.getAnio(),
                curso.getCuatrimestre(),
                curso.getComision()
        );
        return new ExamenResponse(
                examen.getId(),
                examen.getFecha(),
                examen.getTipo(),
                examen.getDescripcion(),
                examen.getActivo(),
                cursoResumen,
                curso.getMateria().getNombre()
        );
    }
}
