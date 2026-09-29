package com.ips.gestion_academica.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ips.gestion_academica.model.InscripcionExamen;

public interface InscripcionExamenRepository extends JpaRepository<InscripcionExamen, Long> {

    List<InscripcionExamen> findByActivoTrue();

    List<InscripcionExamen> findByAlumnoId(Long alumnoId);

    boolean existsByAlumnoIdAndExamenIdAndActivoTrue(Long alumnoId, Long examenId);
}
