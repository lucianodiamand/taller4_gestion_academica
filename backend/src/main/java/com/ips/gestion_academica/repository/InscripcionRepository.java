package com.ips.gestion_academica.repository;

import com.ips.gestion_academica.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscripcionRepository
        extends JpaRepository<Inscripcion, Long> {

    List<Inscripcion> findByActivoTrue();

    List<Inscripcion> findByAlumnoId(Long alumnoId);

    boolean existsByAlumnoIdAndCursoIdAndActivoTrue(
            Long alumnoId,
            Long cursoId
    );

    boolean existsByAlumnoIdAndCursoId(
            Long alumnoId,
            Long cursoId
    );

    boolean existsByAlumnoIdAndCursoIdAndIdNot(
            Long alumnoId,
            Long cursoId,
            Long id
    );
}