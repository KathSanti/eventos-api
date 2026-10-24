package com.eventos.dto;

import com.eventos.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos públicos del cliente autenticado, nunca incluye la contraseña. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    private String nombre;
    private String email;
    private Rol rol;
}
