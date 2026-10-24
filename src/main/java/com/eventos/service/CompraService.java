package com.eventos.service;

import com.eventos.dto.CompraRequestDTO;
import com.eventos.dto.CompraResponseDTO;
import com.eventos.model.Cliente;
import com.eventos.model.Compra;
import com.eventos.model.Evento;
import com.eventos.repository.CompraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final EventoService eventoService;

    @Transactional
    public CompraResponseDTO comprarEntradas(CompraRequestDTO request, Cliente cliente) {
        Evento evento = eventoService.obtenerEntidadPorId(request.getEventoId());

        BigDecimal precioUnitario = evento.getPrecio();
        BigDecimal precioTotal = precioUnitario.multiply(BigDecimal.valueOf(request.getCantidad()));

        Compra compra = new Compra();
        compra.setCliente(cliente);
        compra.setEvento(evento);
        compra.setCantidad(request.getCantidad());
        compra.setPrecioUnitario(precioUnitario);
        compra.setPrecioTotal(precioTotal);
        compra.setFechaCompra(LocalDateTime.now());
        compraRepository.save(compra);

        return new CompraResponseDTO(evento.getNombre(), request.getCantidad(), precioUnitario, precioTotal);
    }
}
