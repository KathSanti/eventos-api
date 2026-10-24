package com.eventos.repository;

import com.eventos.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraRepository extends JpaRepository<Compra, Long> {
}
