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
        // Mantenemos el factor de costo 12 exigido por la UTN
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitamos CSRF para permitir peticiones desde el frontend actual
            .csrf(csrf -> csrf.disable())

            // CONFIGURACIÓN DE HEADERS DE SEGURIDAD (Regla RS-06)
            .headers(headers -> headers
                // 1. X-Content-Type-Options: nosniff (Previene MIME sniffing)
                .contentTypeOptions(Customizer.withDefaults())
                
                // 2. X-Frame-Options: DENY (Previene Clickjacking)
                .frameOptions(frame -> frame.deny())
                
                // 3. Strict-Transport-Security (HSTS): Forzar HTTPS por 1 año
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
                
                // 4. Content-Security-Policy (CSP): Restringir orígenes (self = solo este servidor)
                // Nota: 'unsafe-inline' se agrega para que funcionen tus estilos/scripts internos del index.html
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';"))
            )

            .authorizeHttpRequests(auth -> auth
                // 1. Rutas públicas
                .requestMatchers("/", "/index.html", "/api/usuarios/login").permitAll()
                
                // 2. LOGS DE AUDITORÍA: Solo el SuperAdmin tiene acceso
                .requestMatchers("/api/usuarios/logs").hasAuthority("SUPERADMIN")

                // 3. GESTIÓN DE USUARIOS
                .requestMatchers(HttpMethod.POST, "/api/usuarios/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/usuarios").hasAnyAuthority("SUPERADMIN", "ADMIN", "AUDITOR", "REGISTRADOR")
                
                // 4. GESTIÓN DE PRODUCTOS
                .requestMatchers(HttpMethod.POST, "/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN", "REGISTRADOR")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN", "REGISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/productos/**").authenticated()
                
                // 5. Bloqueo por defecto para cualquier otra ruta
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}
