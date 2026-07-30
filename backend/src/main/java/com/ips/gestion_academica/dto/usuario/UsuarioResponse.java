package com.ips.gestion_academica.dto.usuario;

import com.ips.gestion_academica.model.Rol;

public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String apellido;
    private String legajo;
    private String email;
    private boolean activo;
    private Rol rol;

    public UsuarioResponse(
            Long id,
            String nombre,
            String apellido,
            String legajo,
            String email,
            boolean activo,
            Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.legajo = legajo;
        this.email = email;
        this.activo = activo;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getLegajo() {
        return legajo;
    }

    public String getEmail() {
        return email;
    }

    public boolean getActivo() {
        return activo;
    }

    public Rol getRol() {
        return rol;
    }

}