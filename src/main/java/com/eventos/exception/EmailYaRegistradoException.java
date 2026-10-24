package com.eventos.exception;

public class EmailYaRegistradoException extends RuntimeException {
    public EmailYaRegistradoException(String mensaje) {
        super(mensaje);
    }
}
