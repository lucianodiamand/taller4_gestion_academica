package com.ips.gestion_academica.dto.materia;

public class MateriaResponse {

    private long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private int anioCursada;
    private Boolean activo;

    public MateriaResponse(long id, String codigo, String nombre, String descripcion, int anioCursada, Boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.anioCursada = anioCursada;
        this.activo = activo;
    }

    public long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getAnioCursada() {
        return anioCursada;
    }

    public Boolean getActivo() {
        return activo;
    }

}