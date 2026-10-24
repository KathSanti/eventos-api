package com.eventos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cuerpo de entrada para POST /eventos. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NuevoEventoDTO {

    @NotBlank(message = "El nombre del evento es obligatorio")
    private String nombre;

    @NotNull(message = "La fecha del evento es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private BigDecimal precio;
}
