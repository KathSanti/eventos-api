package com.eventos.controller;

import com.eventos.model.Cliente;
import com.eventos.model.Evento;
import com.eventos.model.Rol;
import com.eventos.repository.ClienteRepository;
import com.eventos.repository.EventoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de POST /compras.
 *
 * Aqui NO usamos @WithMockUser, porque CompraController recibe
 * "@AuthenticationPrincipal Cliente cliente": necesita un Cliente REAL
 * (guardado en la BD), no un usuario de mentira. Por eso primero
 * guardamos un cliente y un evento, y despues "iniciamos sesion" con
 * .with(user(cliente)).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CompraControllerTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    EventoRepository eventoRepository;

    // ---- Metodos de ayuda para preparar datos ----

    private Cliente guardarUsuario(String email, Rol rol) {
        Cliente c = new Cliente();
        c.setNombre("Usuario Test");
        c.setEmail(email);
        c.setPassword("no-importa-en-este-test");
        c.setRol(rol);
        return clienteRepository.save(c);
    }

    private Evento guardarEvento() {
        Evento e = new Evento();
        e.setNombre("Festival Test");
        e.setFecha(LocalDate.of(2026, 12, 31));
        e.setPrecio(new BigDecimal("10000"));
        return eventoRepository.save(e);
    }

    // ---- Tests ----

    @Test
    void clienteCompraEntradasRetorna201YCalculaTotal() throws Exception {
        Cliente cliente = guardarUsuario("cliente@test.com", Rol.CLIENTE);
        Evento evento = guardarEvento();

        String json = """
                {"eventoId": %d, "cantidad": 3}
                """.formatted(evento.getId());

        mvc.perform(post("/compras")
                        .with(user(cliente))   // "inicia sesion" como ese cliente
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.evento").value("Festival Test"))
                .andExpect(jsonPath("$.cantidadEntradas").value(3))
                .andExpect(jsonPath("$.precioTotal").value(30000)); // 3 x 10000
    }

    @Test
    void adminNoPuedeComprarRetorna403() throws Exception {
        Cliente admin = guardarUsuario("otro-admin@test.com", Rol.ADMINISTRADOR);
        Evento evento = guardarEvento();

        String json = """
                {"eventoId": %d, "cantidad": 1}
                """.formatted(evento.getId());

        mvc.perform(post("/compras")
                        .with(user(admin))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void comprarEventoInexistenteRetorna404() throws Exception {
        Cliente cliente = guardarUsuario("cliente2@test.com", Rol.CLIENTE);

        mvc.perform(post("/compras")
                        .with(user(cliente))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventoId": 99999, "cantidad": 1}
                                """))
                .andExpect(status().isNotFound());
    }
}
