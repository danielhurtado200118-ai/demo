package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Mantenemos CSRF deshabilitado para que el formulario funcione
            .csrf(csrf -> csrf.disable())

            // 2. Headers de seguridad (Mantenemos tus reglas de la UTN)
            .headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';"))
            )

            .authorizeHttpRequests(auth -> auth
                // Rutas públicas
                .requestMatchers("/", "/index.html", "/api/usuarios/login").permitAll()
                
                // AJUSTE AQUÍ: Permitimos que cualquier usuario logueado gestione productos
                // Esto es para que el usuario "admin" de Render pueda guardar sin problemas
                .requestMatchers("/api/productos/**").authenticated()
                .requestMatchers("/api/usuarios/**").authenticated()

                // Bloqueo por defecto
                .anyRequest().authenticated()
            )
            // Agregamos login por formulario básico por si acaso
            .formLogin(Customizer.withDefaults())
            .httpBasic(Customizer.withDefaults());
        
        return http.build();
    }
}
