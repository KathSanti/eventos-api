package com.eventos.service;

import com.eventos.dto.EventoDTO;
import com.eventos.dto.EventoDetalleDTO;
import com.eventos.dto.NuevoEventoDTO;
import com.eventos.exception.EventoYaExisteException;
import com.eventos.exception.RecursoNoEncontradoException;
import com.eventos.model.Evento;
import com.eventos.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    public List<EventoDTO> listarEventos() {
        return eventoRepository.findAll().stream()
                .map(e -> new EventoDTO(e.getNombre(), e.getFecha(), e.getPrecio()))
                .toList();
    }

    public EventoDTO agregarEvento(NuevoEventoDTO datos) {
        eventoRepository.findByNombreIgnoreCase(datos.getNombre()).ifPresent(e -> {
            throw new EventoYaExisteException("Ya existe un evento con el nombre '" + datos.getNombre() + "'");
        });

        Evento evento = new Evento();
        evento.setNombre(datos.getNombre());
        evento.setFecha(datos.getFecha());
        evento.setPrecio(datos.getPrecio());
        Evento guardado = eventoRepository.save(evento);

        return new EventoDTO(guardado.getNombre(), guardado.getFecha(), guardado.getPrecio());
    }

    public EventoDetalleDTO buscarPorNombre(String nombre) {
        Evento evento = eventoRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un evento con el nombre '" + nombre + "'"));
        return new EventoDetalleDTO(evento.getId(), evento.getNombre(), evento.getFecha(), evento.getPrecio());
    }

    public Evento obtenerEntidadPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un evento con id " + id));
    }
}
