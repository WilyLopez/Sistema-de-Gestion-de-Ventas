package com.sgvi.sistema_ventas.config;

import com.sgvi.sistema_ventas.security.jwt.JwtAuthenticationEntryPoint;
import com.sgvi.sistema_ventas.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad de Spring Security.
 * Implementa autenticación JWT y control de acceso basado en roles.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Configuración del filtro de seguridad
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF (no necesario para API REST con JWT)
                .csrf(csrf -> csrf.disable())

                // Configurar manejo de excepciones
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // Configurar sesión stateless (sin sesiones)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configurar autorización de endpoints
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (sin autenticación)
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/public/**").permitAll()

                        // Swagger/OpenAPI (si está configurado)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Endpoints de administración (solo ADMIN)
                        .requestMatchers("/api/v1/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/v1/roles/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMINISTRADOR")

                        // Endpoints de ventas (ADMIN y VENDEDOR)
                        .requestMatchers("/api/v1/ventas/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                        .requestMatchers("/api/v1/clientes/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")

                        // Endpoints de productos (todos autenticados)
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/productos/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/productos/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")

                        // Reportes (todos los roles autenticados)
                        .requestMatchers("/api/v1/reportes/**").authenticated()

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )

                // Agregar filtro JWT antes del filtro de autenticación por defecto
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean para encriptar contraseñas con BCrypt
     * RNF-002.1: Encriptación con BCrypt (mínimo 10 saltos)
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // 10 saltos
    }

    /**
     * Bean para el AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
