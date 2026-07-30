package com.ips.gestion_academica.dto.usuario;

import com.ips.gestion_academica.model.Rol;

public class UsuarioRequest {
    
    private String nombre;
    private String apellido;
    private String legajo;
    private String email;
    private String dni;
    private String password;
    private Rol rol;


    public UsuarioRequest() {
    }


    public void setNombre(String nombre){
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setApellido(String apellido){
        this.apellido = apellido;
    }

    public String getApellido() {
        return apellido;
    }


    public void setLegajo(String legajo){
        this.legajo = legajo;
    }

    public String getLegajo() {
        return legajo;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setDni(String dni){
        this.dni = dni;
    }

    public String getDni(){
        return dni;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getPassword(){
        return password;
    }
    
    public void setRol(Rol rol){
        this.rol = rol;
    }

    public Rol getRol() {
        return rol;
    }
}
