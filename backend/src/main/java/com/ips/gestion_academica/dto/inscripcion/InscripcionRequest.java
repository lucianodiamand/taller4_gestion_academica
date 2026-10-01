package com.ips.gestion_academica.dto.inscripcion;

public class InscripcionRequest {

    private Long alumnoId;
    private Long cursoId;

    public InscripcionRequest() {
    }

    public Long getAlumnoId() {
        return alumnoId;
    }

    public void setAlumnoId(Long alumnoId) {
        this.alumnoId = alumnoId;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }
}