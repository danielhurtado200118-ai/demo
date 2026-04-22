package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // CUMPLE RF-02: BCrypt con factor de costo 12 (Obligatorio)
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // RS-03: Deshabilitado para simplificar con JWT/Auth básica
            .cors(cors -> cors.configurationSource(request -> {
                var config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("*"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                return config;
            }))
            // CUMPLE RS-06: Headers de Seguridad HTTP
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; connect-src 'self' https://backend-agricola.onrender.com; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';"))
                .frameOptions(frame -> frame.deny()) // Previene Clickjacking
                .contentTypeOptions(Customizer.withDefaults()) // Previene MIME Sniffing
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/api/usuarios/login").permitAll()
                // CUMPLE RF-05: Validación de roles en el servidor
                .requestMatchers("/api/usuarios/**").hasAuthority("SUPERADMIN")
                .requestMatchers("/api/productos/**").hasAnyAuthority("SUPERADMIN", "REGISTRADOR")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults()); 

        return http.build();
    }
}
