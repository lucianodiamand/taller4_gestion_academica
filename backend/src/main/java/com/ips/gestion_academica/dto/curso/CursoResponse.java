package com.ips.gestion_academica.dto.curso;

import com.ips.gestion_academica.dto.usuario.UsuarioResumeResponse;
import com.ips.gestion_academica.dto.materia.MateriaResponse;

public class CursoResponse {
    private Long id;
    private Integer anio;
    private Integer cuatrimestre;
    private String comision;
    private boolean activo;
    private UsuarioResumeResponse profesor;
    private MateriaResponse materia;

    public CursoResponse(
            Long id,
            Integer anio,
            Integer cuatrimestre,
            String comision,
            boolean activo,
            UsuarioResumeResponse profesor,
            MateriaResponse materia) {
        this.id = id;
        this.anio =anio;
        this.cuatrimestre = cuatrimestre;
        this.comision = comision;
        this.activo = activo;
        this.profesor = profesor;
        this.materia = materia;
    }

    public Long getId() {
        return id;
    }


    public Integer getAnio() {
        return anio;
    }


    public Integer getCuatrimestre() {
        return cuatrimestre;
    }


    public String getComision() {
        return comision;
    }


    public boolean getActivo() {
        return activo;
    }

    public UsuarioResumeResponse getProfesor(){
        return profesor;
    }

    public MateriaResponse getMateria(){
        return materia;
    }

}
