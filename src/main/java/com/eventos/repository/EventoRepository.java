package com.eventos.repository;

import com.eventos.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    Optional<Evento> findByNombreIgnoreCase(String nombre);
}
