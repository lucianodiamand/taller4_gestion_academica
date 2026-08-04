package com.ips.gestion_academica.dto.examen;

import java.time.LocalDate;

import com.ips.gestion_academica.dto.curso.CursoResumenResponse;
import com.ips.gestion_academica.model.TipoExamen;

public class ExamenResponse {

    private Long id;
    private LocalDate fecha;
    private TipoExamen tipo;
    private String descripcion;
    private Boolean activo;

    private CursoResumenResponse curso;

    public ExamenResponse(
            Long id,
            LocalDate fecha,
            TipoExamen tipo,
            String descripcion,
            Boolean activo,
            CursoResumenResponse curso) {

        this.id = id;
        this.fecha = fecha;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.activo = activo;
        this.curso = curso;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public TipoExamen getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public CursoResumenResponse getCurso() {
        return curso;
    }
}