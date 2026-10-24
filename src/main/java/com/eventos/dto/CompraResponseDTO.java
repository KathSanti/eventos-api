package com.eventos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Resumen de la compra devuelto por POST /compras. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraResponseDTO {
    private String evento;
    private Integer cantidadEntradas;
    private BigDecimal precioUnitario;
    private BigDecimal precioTotal;
}
