package com.ips.gestion_academica.dto.inscripcionexamen;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class InscripcionExamenNotaRequest {

    @NotNull(message = "La nota es obligatoria")
    @Min(value = 1, message = "La nota debe ser entre 1 y 10")
    @Max(value = 10, message = "La nota debe ser entre 1 y 10")
    private Integer nota;

    public InscripcionExamenNotaRequest() {
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }
}
