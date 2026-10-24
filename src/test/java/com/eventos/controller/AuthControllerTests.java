package com.eventos.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Tests de registro y login (/auth). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTests {

    @Autowired
    MockMvc mvc;

    private static final String REGISTRO_JSON = """
            {"nombre":"Kat", "email":"kat@test.com", "password":"12345678"}
            """;

    @Test
    void registroRetorna201ConRolCliente() throws Exception {
        mvc.perform(post("/auth/registro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTRO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void registroConEmailRepetidoRetorna409() throws Exception {
        // primer registro: OK
        mvc.perform(post("/auth/registro").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(REGISTRO_JSON));

        // segundo registro con el mismo email: conflicto
        mvc.perform(post("/auth/registro").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(REGISTRO_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void loginConAdminDelPerfilTestRetornaOk() throws Exception {
        // Este admin lo crea AdminBootstrap con los datos de application-test.properties
        mvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.com", "password":"AdminTest123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"));
    }

    @Test
    void loginConClaveIncorrectaRetorna401() throws Exception {
        mvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.com", "password":"clave-mala"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
