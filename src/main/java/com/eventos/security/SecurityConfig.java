package com.eventos.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Login con sesión (cookie JSESSIONID) + CSRF real: el frontend debe pedir
 * el token en GET /auth/csrf y reenviarlo en el header X-XSRF-TOKEN en cada
 * POST/PUT/DELETE. Sin ese token, una página externa que intente disparar un
 * POST contra esta API (ej. un form autoenviado) es rechazada.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Spring la autoconfigura con el UserDetailsService y el PasswordEncoder ya registrados como beans. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Sin esto, un error (400/403/500...) que caiga en /error
                        // termina disfrazado de 401 Unauthorized.
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/eventos", "/eventos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/registro", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/csrf").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autenticado"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permisos para esta acción")));

        return http.build();
    }
}
