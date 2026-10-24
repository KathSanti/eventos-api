package com.eventos.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de los endpoints /eventos.
 *
 * @SpringBootTest        -> levanta la app completa (igual que cuando la corres).
 * @AutoConfigureMockMvc  -> nos da "mvc", un cliente HTTP falso para llamar endpoints
 *                           sin levantar un servidor real.
 * @ActiveProfiles("test")-> usa H2 en memoria (application-test.properties).
 * @Transactional         -> al final de cada test se deshace lo que se guardo en la BD,
 *                           asi cada test parte limpio y no se pisan entre ellos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventoControllerTests {

    @Autowired
    MockMvc mvc;

    private static final String EVENTO_JSON = """
            {"nombre":"Concierto de prueba", "fecha":"2026-12-31", "precio":15000}
            """;

    // ---------- GET (publico) ----------

    @Test
    void listarEventosSinLoginRetornaOk() throws Exception {
        mvc.perform(get("/eventos"))
                .andExpect(status().isOk());
    }

    @Test
    void buscarEventoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/eventos/no-existe"))
                .andExpect(status().isNotFound());
    }

    // ---------- POST (solo ADMINISTRADOR) ----------

    @Test
    void crearEventoSinLoginRetorna401() throws Exception {
        mvc.perform(post("/eventos")
                        .with(csrf())                         // simula el token CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENTO_JSON))
                .andExpect(status().isUnauthorized());        // nadie inicio sesion
    }

    @Test
    @WithMockUser(roles = "CLIENTE") // simula un usuario logueado con rol CLIENTE
    void crearEventoComoClienteRetorna403() throws Exception {
        mvc.perform(post("/eventos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENTO_JSON))
                .andExpect(status().isForbidden());           // logueado, pero sin permiso
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crearEventoComoAdminRetorna201() throws Exception {
        mvc.perform(post("/eventos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENTO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Concierto de prueba"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crearEventoSinTokenCsrfRetorna403() throws Exception {
        // Sin .with(csrf()) -> Spring Security lo rechaza aunque seas admin
        mvc.perform(post("/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENTO_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crearEventoConPrecioNegativoRetorna400() throws Exception {
        String jsonInvalido = """
                {"nombre":"Evento malo", "fecha":"2026-12-31", "precio":-5}
                """;
        mvc.perform(post("/eventos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());          // falla la validacion @Positive
    }
}
