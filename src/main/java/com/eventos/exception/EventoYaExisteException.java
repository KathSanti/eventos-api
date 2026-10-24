package com.eventos.exception;

public class EventoYaExisteException extends RuntimeException {
    public EventoYaExisteException(String mensaje) {
        super(mensaje);
    }
}
