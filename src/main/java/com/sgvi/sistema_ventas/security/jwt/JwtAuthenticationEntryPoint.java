package com.sgvi.sistema_ventas.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgvi.sistema_ventas.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Punto de entrada de autenticación JWT.
 * Maneja los errores de autenticación y devuelve respuestas JSON.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.error("Error de autenticación: {}", authException.getMessage());

        // Crear respuesta de error
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .error("No autorizado")
                .mensaje("Debe autenticarse para acceder a este recurso")
                .path(request.getRequestURI())
                .build();

        // Configurar respuesta HTTP
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Escribir JSON en la respuesta
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // Para serializar LocalDateTime
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
