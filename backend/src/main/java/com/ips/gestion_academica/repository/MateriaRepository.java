package com.ips.gestion_academica.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ips.gestion_academica.model.Materia;

public interface MateriaRepository extends JpaRepository<Materia, Long> {
    boolean existsByCodigo(String codigo);

}
