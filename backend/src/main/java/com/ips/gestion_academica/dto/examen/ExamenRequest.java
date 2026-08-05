package com.ips.gestion_academica.dto.examen;

import java.time.LocalDate;

import com.ips.gestion_academica.model.TipoExamen;

import jakarta.validation.constraints.NotNull;

public class ExamenRequest {

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "El tipo de examen es obligatorio")
    private TipoExamen tipo;

    private String descripcion;

    @NotNull(message = "El curso es obligatorio")
    private Long cursoId;

    public ExamenRequest() {
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public TipoExamen getTipo() {
        return tipo;
    }

    public void setTipo(TipoExamen tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }
}