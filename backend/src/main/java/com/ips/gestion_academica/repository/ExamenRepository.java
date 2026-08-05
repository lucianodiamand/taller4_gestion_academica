package com.ips.gestion_academica.repository;

import com.ips.gestion_academica.model.Examen;
import com.ips.gestion_academica.model.TipoExamen;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExamenRepository
        extends JpaRepository<Examen, Long> {

    List<Examen> findByActivoTrue();

    boolean existsByCursoIdAndFechaAndTipo(
            Long cursoId,
            LocalDate fecha,
            TipoExamen tipo
    );

    boolean existsByCursoIdAndFechaAndTipoAndIdNot(
            Long cursoId,
            LocalDate fecha,
            TipoExamen tipo,
            Long id
    );
}