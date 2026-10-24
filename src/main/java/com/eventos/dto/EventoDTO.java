package com.eventos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representación pública de un evento para el listado (GET /eventos).
 * No incluye el id interno de la entidad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoDTO {
    private String nombre;
    private LocalDate fecha;
    private BigDecimal precio;
}
