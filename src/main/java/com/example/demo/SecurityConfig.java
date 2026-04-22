package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // RF-02: BCrypt con factor de costo 12 (Requisito de la Fase 1)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitado para permitir peticiones desde el HTML
            .cors(cors -> cors.configurationSource(request -> {
                var config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("*")); // En producción cambiar por la URL de Render
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                return config;
            }))
            // RS-06: Headers de Seguridad HTTP obligatorios
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';"))
                .frameOptions(frame -> frame.deny()) // RS-06: X-Frame-Options (Clickjacking)
                .contentTypeOptions(Customizer.withDefaults()) // RS-06: X-Content-Type-Options
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
            )
            .authorizeHttpRequests(auth -> auth
                // Permisos públicos
                .requestMatchers("/", "/index.html", "/api/usuarios/login").permitAll()
                
                // RF-05: Control de Acceso Basado en Roles (RBAC)
                // Gestión de Usuarios: Solo SuperAdmin y Admin
                .requestMatchers("/api/usuarios/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                
                // Gestión de Productos: SuperAdmin, Admin y Registrador
                .requestMatchers("/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN", "REGISTRADOR")
                
                // El Auditor tiene acceso de solo lectura (GET) a todo
                .requestMatchers("/api/**").hasAuthority("AUDITOR")
                
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults()); 

        return http.build();
    }
}
