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
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';"))
            )

            .authorizeHttpRequests(auth -> auth
                // 1. Rutas públicas
                .requestMatchers("/", "/index.html", "/api/usuarios/login", "/css/**", "/js/**").permitAll()
                
                // 2. LOGS DE AUDITORÍA: Solo el SuperAdmin tiene acceso
                .requestMatchers("/api/usuarios/logs").hasAuthority("SUPERADMIN")

                // 3. GESTIÓN DE USUARIOS
                .requestMatchers(HttpMethod.POST, "/api/usuarios/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                // CAMBIO: Permitimos que cualquier usuario autenticado (incluyendo tu admin de Render) vea la lista
                .requestMatchers(HttpMethod.GET, "/api/usuarios/**").authenticated()
                
                // 4. GESTIÓN DE PRODUCTOS
                // Para guardar o editar, permitimos a cualquier usuario autenticado por ahora (para que funcione el botón)
                .requestMatchers(HttpMethod.POST, "/api/productos/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/productos/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                // CAMBIO: Cualquier usuario logueado puede VER los productos en la tabla
                .requestMatchers(HttpMethod.GET, "/api/productos/**").authenticated()
                
                // 5. Bloqueo por defecto para cualquier otra ruta
                .anyRequest().authenticated()
            )
            // Agregamos esto para que el sistema reconozca el login del formulario
            .formLogin(form -> form.loginPage("/").permitAll())
            .httpBasic(Customizer.withDefaults());
        
        return http.build();
    }
}
