package com.ips.gestion_academica.dto.curso;

public class CursoResumenResponse {

    private Long id;
    private Integer anio;
    private Integer cuatrimestre;
    private String comision;

    public CursoResumenResponse(
            Long id,
            Integer anio,
            Integer cuatrimestre,
            String comision) {

        this.id = id;
        this.anio = anio;
        this.cuatrimestre = cuatrimestre;
        this.comision = comision;
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
}