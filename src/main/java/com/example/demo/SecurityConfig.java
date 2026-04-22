package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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
        // CAMBIO: Usamos NoOp para que acepte "12345" tal cual está en Neon
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // AGREGADO: Configuración de CORS para que el HTML pueda hablar con el Backend
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            .headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
                // CAMBIO: CSP relajado para permitir la conexión a Render
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; connect-src 'self' https://backend-agricola.onrender.com; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';"))
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/api/usuarios/login").permitAll()
                .requestMatchers("/api/usuarios/logs").hasAuthority("SUPERADMIN")
                .requestMatchers(HttpMethod.POST, "/api/usuarios/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/usuarios").hasAnyAuthority("SUPERADMIN", "ADMIN", "REGISTRADOR")
                .requestMatchers(HttpMethod.POST, "/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN", "REGISTRADOR")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN", "REGISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                .anyRequest().authenticated()
            )
            // IMPORTANTE: Para que el login del HTML funcione con AUTH_HEADER
            .httpBasic(Customizer.withDefaults());
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("*")); // Permite acceso desde cualquier lado
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setExposedHeaders(Arrays.asList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
