package com.eventos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cuerpo de entrada para POST /compras. El cliente sale del usuario autenticado, no del body. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraRequestDTO {

    @NotNull(message = "Debes indicar el id del evento (ver GET /eventos/{nombre})")
    private Long eventoId;

    @NotNull(message = "La cantidad de entradas es obligatoria")
    @Min(value = 1, message = "Debes comprar al menos 1 entrada")
    private Integer cantidad;
}
