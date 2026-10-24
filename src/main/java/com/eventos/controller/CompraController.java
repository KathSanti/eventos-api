package com.eventos.controller;

import com.eventos.dto.CompraRequestDTO;
import com.eventos.dto.CompraResponseDTO;
import com.eventos.model.Cliente;
import com.eventos.service.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    /** Solo un cliente autenticado puede comprar; primero debe registrarse en /auth/registro. */
    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping
    public ResponseEntity<CompraResponseDTO> comprarEntradas(@Valid @RequestBody CompraRequestDTO request,
                                                               @AuthenticationPrincipal Cliente cliente) {
        CompraResponseDTO resumen = compraService.comprarEntradas(request, cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(resumen);
    }
}
