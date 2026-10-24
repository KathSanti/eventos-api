package com.eventos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cuerpo de entrada para POST /auth/login. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
