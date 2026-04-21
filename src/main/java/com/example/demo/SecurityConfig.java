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
            // 1. Deshabilitamos CSRF para que el botón de Guardar funcione en la nube
            .csrf(csrf -> csrf.disable())

            // 2. HEADERS DE SEGURIDAD (Regla RS-06 de la UTN)
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
                // Rutas públicas: página de inicio, login y archivos de diseño
                .requestMatchers("/", "/index.html", "/api/usuarios/login", "/css/**", "/js/**", "/static/**").permitAll()
                
                // LOGS DE AUDITORÍA: Solo el SuperAdmin
                .requestMatchers("/api/usuarios/logs").hasAuthority("SUPERADMIN")

                // GESTIÓN DE PRODUCTOS Y USUARIOS (Para que las tablas aparezcan)
                // Permitimos VER (GET) y GUARDAR (POST/PUT) a cualquier usuario autenticado
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/usuarios/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/usuarios/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/productos/**").authenticated()
                
                // ELIMINAR: Mantenemos la restricción estricta de la UTN
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                
                // Bloqueo por defecto para cualquier otra cosa
                .anyRequest().authenticated()
            )
            // Configuramos para que no salga la ventana gris del navegador
            .httpBasic(basic -> basic.disable())
            
            // Usamos tu formulario de login del index.html
            .formLogin(form -> form
                .loginPage("/")
                .permitAll()
            )
            .logout(logout -> logout.permitAll());

        return http.build();
    }
}

