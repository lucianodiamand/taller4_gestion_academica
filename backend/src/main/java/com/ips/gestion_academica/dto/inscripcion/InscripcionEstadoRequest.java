package com.ips.gestion_academica.dto.inscripcion;
import com.ips.gestion_academica.model.EstadoInscripcion;
import jakarta.validation.constraints.NotNull;

public class InscripcionEstadoRequest {

    @NotNull
    private EstadoInscripcion estado;

    public InscripcionEstadoRequest() {
    }

    public EstadoInscripcion getEstado() {
        return estado;
    }

    public void setEstado(EstadoInscripcion estado) {
        this.estado = estado;
    }
}
