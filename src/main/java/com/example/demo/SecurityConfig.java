package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // 1. Mantenemos la sesión activa en el navegador para que no de error 401
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';"))
            )
            .authorizeHttpRequests(auth -> auth
                // Rutas que no necesitan login
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/api/usuarios/login").permitAll()
                
                // VER TABLAS (GET): Permitimos a cualquier usuario logueado para llenar la tabla
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/usuarios/**").authenticated()
                
                // ACCIONES: Mantenemos las reglas de la UTN
                .requestMatchers(HttpMethod.POST, "/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN", "REGISTRADOR")
                .requestMatchers(HttpMethod.POST, "/api/usuarios/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                
                .anyRequest().authenticated()
            )
            // 2. IMPORTANTE: Esto permite que el JavaScript use la sesión del login
            .formLogin(form -> form
                .loginPage("/")
                .loginProcessingUrl("/api/usuarios/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
