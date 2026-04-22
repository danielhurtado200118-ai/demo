package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Usamos NoOp para que acepte "12345" directamente desde Neon
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Permite peticiones POST desde el HTML
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Elimina el error rojo de CORS
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    // Permite conectar con Render y ejecutar tus scripts internos
                    .policyDirectives("default-src 'self'; connect-src 'self' https://backend-agricola.onrender.com; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';"))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/api/usuarios/login").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {}); // Necesario para el AUTH_HEADER del JavaScript

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("*")); // Permite que tu frontend hable con el backend
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
