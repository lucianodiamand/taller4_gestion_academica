package com.ips.gestion_academica.dto.inscripcion;

import java.time.LocalDate;

import com.ips.gestion_academica.dto.curso.CursoResumenResponse;
import com.ips.gestion_academica.dto.usuario.UsuarioResumeResponse;
import com.ips.gestion_academica.model.EstadoInscripcion;

public class InscripcionResponse {

    private Long id;
    private LocalDate fechaInscripcion;
    private EstadoInscripcion estado;
    private Boolean activo;

    private UsuarioResumeResponse alumno;
    private CursoResumenResponse curso;

    public InscripcionResponse(
            Long id,
            LocalDate fechaInscripcion,
            EstadoInscripcion estado,
            Boolean activo,
            UsuarioResumeResponse alumno,
            CursoResumenResponse curso) {

        this.id = id;
        this.fechaInscripcion = fechaInscripcion;
        this.estado = estado;
        this.activo = activo;
        this.alumno = alumno;
        this.curso = curso;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public EstadoInscripcion getEstado() {
        return estado;
    }

    public Boolean getActivo() {
        return activo;
    }

    public UsuarioResumeResponse getAlumno() {
        return alumno;
    }

    public CursoResumenResponse getCurso() {
        return curso;
    }
}