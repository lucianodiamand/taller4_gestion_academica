package com.ips.gestion_academica.dto.inscripcionexamen;

import jakarta.validation.constraints.NotNull;

public class InscripcionExamenRequest {

    private Long alumnoId;

    @NotNull(message = "El examen es obligatorio")
    private Long examenId;

    public InscripcionExamenRequest() {
    }

    public Long getAlumnoId() {
        return alumnoId;
    }

    public void setAlumnoId(Long alumnoId) {
        this.alumnoId = alumnoId;
    }

    public Long getExamenId() {
        return examenId;
    }

    public void setExamenId(Long examenId) {
        this.examenId = examenId;
    }
}
