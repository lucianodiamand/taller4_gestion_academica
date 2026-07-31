package com.ips.gestion_academica.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso,Long id) {
        super("No se encontro el " + recurso + " con id " + id);
    }
}
