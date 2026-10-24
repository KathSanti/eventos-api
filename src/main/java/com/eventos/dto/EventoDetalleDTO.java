package com.eventos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Detalle de un evento, usado por el flujo de compra para resolver
 * el id interno a partir del nombre antes de llamar a POST /compras.
 * No se usa en el listado general (GET /eventos).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoDetalleDTO {
    private Long id;
    private String nombre;
    private LocalDate fecha;
    private BigDecimal precio;
}
