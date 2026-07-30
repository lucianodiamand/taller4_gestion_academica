package com.ips.gestion_academica.dto.usuario;


public class UsuarioResumeResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String legajo;
    private String email;

    public UsuarioResumeResponse(
            Long id,
            String nombre,
            String apellido,
            String legajo,
            String email) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.legajo = legajo;
        this.email = email;
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

}
