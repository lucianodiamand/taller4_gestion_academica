package com.ips.gestion_academica.dto.materia;

import jakarta.validation.constraints.Pattern;

public class MateriaRequest {

    @Pattern(regexp = "^[A-Za-z]{3}-\\d{3}$",
             message = "El código debe tener el formato: 3 letras, un guión y 3 números (ej: MAT-001)")
    private String codigo;
    private String nombre;
    private String descripcion;
    private int anioCursada;

    public MateriaRequest() {
        
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getAnioCursada() {
        return anioCursada;
    }

    public void setAnioCursada(int anioCursada) {
        this.anioCursada = anioCursada;
    }
}
