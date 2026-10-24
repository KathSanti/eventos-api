package com.eventos.security;

import com.eventos.model.Cliente;
import com.eventos.model.Rol;
import com.eventos.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Crea el primer usuario ADMINISTRADOR si todavía no existe ninguno; el registro público solo crea CLIENTE. */
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.nombre}")
    private String adminNombre;

    @Override
    public void run(ApplicationArguments args) {
        boolean existeAdmin = clienteRepository.findAll().stream().anyMatch(c -> c.getRol() == Rol.ADMINISTRADOR);
        if (existeAdmin || clienteRepository.existsByEmail(adminEmail)) {
            return;
        }

        Cliente admin = new Cliente();
        admin.setNombre(adminNombre);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRol(Rol.ADMINISTRADOR);
        clienteRepository.save(admin);
    }
}
