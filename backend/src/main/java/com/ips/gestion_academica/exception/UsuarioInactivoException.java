package com.ips.gestion_academica.exception;

public class UsuarioInactivoException extends RuntimeException{
    public UsuarioInactivoException(Long id) {
        super("El usuario con id " + id + " ya se encuentra inactivo");
    }
}
