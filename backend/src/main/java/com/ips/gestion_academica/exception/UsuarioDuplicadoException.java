package com.ips.gestion_academica.exception;

public class UsuarioDuplicadoException extends RuntimeException {

    public UsuarioDuplicadoException(String mensaje){
        super(mensaje);
    }
    
}