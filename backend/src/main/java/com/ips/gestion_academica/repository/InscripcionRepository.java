package com.ips.gestion_academica.repository;

import com.ips.gestion_academica.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscripcionRepository
        extends JpaRepository<Inscripcion, Long> {

    List<Inscripcion> findByActivoTrue();

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