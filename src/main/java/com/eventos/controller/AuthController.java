package com.eventos.controller;

import com.eventos.dto.LoginRequestDTO;
import com.eventos.dto.RegistroRequestDTO;
import com.eventos.dto.UsuarioResponseDTO;
import com.eventos.model.Cliente;
import com.eventos.model.Rol;
import com.eventos.exception.EmailYaRegistradoException;
import com.eventos.repository.ClienteRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    /** El frontend llama esto primero para obtener la cookie XSRF-TOKEN y el valor a reenviar en el header. */
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of("header", csrfToken.getHeaderName(), "token", csrfToken.getToken());
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registro(@Valid @RequestBody RegistroRequestDTO datos) {
        if (clienteRepository.existsByEmail(datos.getEmail())) {
            throw new EmailYaRegistradoException("Ya existe una cuenta con el email '" + datos.getEmail() + "'");
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(datos.getNombre());
        cliente.setEmail(datos.getEmail());
        cliente.setPassword(passwordEncoder.encode(datos.getPassword()));
        cliente.setRol(Rol.CLIENTE);
        clienteRepository.save(cliente);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UsuarioResponseDTO(cliente.getNombre(), cliente.getEmail(), cliente.getRol()));
    }

    @PostMapping("/login")
    public UsuarioResponseDTO login(@Valid @RequestBody LoginRequestDTO datos,
                                     HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(datos.getEmail(), datos.getPassword()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        Cliente cliente = (Cliente) authentication.getPrincipal();
        return new UsuarioResponseDTO(cliente.getNombre(), cliente.getEmail(), cliente.getRol());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
