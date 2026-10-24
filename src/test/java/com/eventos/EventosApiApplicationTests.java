package com.eventos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * El test mas basico de todos: verifica que la aplicacion completa
 * logra arrancar (controladores, servicios, seguridad, base de datos).
 * Si algo esta mal configurado, este test falla.
 */
@SpringBootTest
@ActiveProfiles("test") // usa application-test.properties -> H2 en memoria, no MySQL
class EventosApiApplicationTests {

    @Test
    void contextLoads() {
        // No hace falta escribir nada: si el contexto no arranca, el test falla solo.
    }
}
