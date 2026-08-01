package com.ips.gestion_academica.exception;

public class RecursoInactivoException extends RuntimeException{
    public RecursoInactivoException(String recurso, Long id) {
        super("El " + recurso + " con el id " + id + " se encuentra inactivo");
    }
}
