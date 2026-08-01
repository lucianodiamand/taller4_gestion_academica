package com.ips.gestion_academica.dto.curso;

public class CursoRequest {

    private Integer anio;
    private Integer cuatrimestre;
    private String comision;
    private Long profesorId;
    private Long materiaId;
    
    public CursoRequest() {
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Integer getCuatrimestre() {
        return cuatrimestre;
    }

    public void setCuatrimestre(Integer cuatrimestre) {
        this.cuatrimestre = cuatrimestre;
    }

    public String getComision() {
        return comision;
    }

    public void setComision(String comision) {
        this.comision = comision;
    }

    public Long getProfesorId() {
        return profesorId;
    }

    public Long getMateriaId(){
        return materiaId;
    }

    public void setProfesorId(Long profesorId) {
        this.profesorId = profesorId;
    }

    public void setMateriaId(Long materiaId){
        this.materiaId =materiaId;
    }
}