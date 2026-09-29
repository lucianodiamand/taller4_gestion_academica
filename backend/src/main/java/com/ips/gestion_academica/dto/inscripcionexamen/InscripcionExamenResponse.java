package com.ips.gestion_academica.dto.inscripcionexamen;

import java.time.LocalDate;

import com.ips.gestion_academica.dto.examen.ExamenResponse;
import com.ips.gestion_academica.dto.usuario.UsuarioResumeResponse;

public class InscripcionExamenResponse {

    private Long id;
    private LocalDate fechaInscripcion;
    private Integer nota;
    private Boolean activo;
    private UsuarioResumeResponse alumno;
    private ExamenResponse examen;

    public InscripcionExamenResponse(
            Long id,
            LocalDate fechaInscripcion,
            Integer nota,
            Boolean activo,
            UsuarioResumeResponse alumno,
            ExamenResponse examen) {

        this.id = id;
        this.fechaInscripcion = fechaInscripcion;
        this.nota = nota;
        this.activo = activo;
        this.alumno = alumno;
        this.examen = examen;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public Integer getNota() {
        return nota;
    }

    public Boolean getActivo() {
        return activo;
    }

    public UsuarioResumeResponse getAlumno() {
        return alumno;
    }

    public ExamenResponse getExamen() {
        return examen;
    }
}
