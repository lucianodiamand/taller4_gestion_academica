package com.ips.gestion_academica.exception;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Credenciales invalidas");
    }
}
