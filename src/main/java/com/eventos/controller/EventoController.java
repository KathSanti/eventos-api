package com.eventos.controller;

import com.eventos.dto.EventoDTO;
import com.eventos.dto.EventoDetalleDTO;
import com.eventos.dto.NuevoEventoDTO;
import com.eventos.service.EventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    /** Único endpoint de listado: nombre, fecha y precio, sin exponer el id. */
    @GetMapping
    public List<EventoDTO> listarEventos() {
        return eventoService.listarEventos();
    }

    /** Solo administradores pueden crear eventos. */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<EventoDTO> agregarEvento(@Valid @RequestBody NuevoEventoDTO datos) {
        EventoDTO creado = eventoService.agregarEvento(datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Resuelve el id interno de un evento a partir de su nombre.
     * Lo usa el flujo de compra; no se usa en el listado general.
     */
    @GetMapping("/{nombre}")
    public EventoDetalleDTO buscarPorNombre(@PathVariable String nombre) {
        return eventoService.buscarPorNombre(nombre);
    }
}
