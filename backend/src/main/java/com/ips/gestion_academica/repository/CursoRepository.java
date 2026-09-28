package com.ips.gestion_academica.repository;

import com.ips.gestion_academica.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    boolean existsByAnioAndCuatrimestreAndComisionAndMateria_Id(Integer anio,Integer cuatrimestre, String comision, Long materiaId);
    boolean existsByAnioAndCuatrimestreAndComisionAndMateria_IdAndIdNot(Integer anio, Integer cuatrimestre, String comision, Long materiaId, Long id);
    List<Curso> findByActivoTrue();


}