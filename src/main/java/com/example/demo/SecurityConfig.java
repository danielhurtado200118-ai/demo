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
            // 1. Deshabilitamos CSRF para que el frontend pueda enviar datos a Neon
            .csrf(csrf -> csrf.disable())

            // 2. CONFIGURACIÓN DE HEADERS DE SEGURIDAD (Regla RS-06)
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
                // Rutas públicas: página principal, login y recursos
                .requestMatchers("/", "/index.html", "/api/usuarios/login", "/static/**", "/css/**", "/js/**").permitAll()
                
                // 3. GESTIÓN DE USUARIOS
                .requestMatchers(HttpMethod.POST, "/api/usuarios/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                // CAMBIO: Permitimos que cualquier usuario logueado VEA la lista de usuarios (llena la tabla)
                .requestMatchers(HttpMethod.GET, "/api/usuarios/**").authenticated()
                
                // 4. GESTIÓN DE PRODUCTOS
                .requestMatchers(HttpMethod.POST, "/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN", "REGISTRADOR")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN", "REGISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                // CAMBIO: Permitimos que cualquier usuario logueado VEA los productos (llena la tabla)
                .requestMatchers(HttpMethod.GET, "/api/productos/**").authenticated()
                
                // 5. Bloqueo por defecto
                .anyRequest().authenticated()
            )
            // Deshabilitamos el login básico del navegador para usar tu formulario
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.loginPage("/").permitAll())
            .logout(logout -> logout.permitAll());
        
        return http.build();
    }
}
