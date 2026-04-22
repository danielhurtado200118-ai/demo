package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .headers(headers -> headers.frameOptions(frame -> frame.deny()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**", "/api/usuarios/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/usuarios/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/usuarios/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                .anyRequest().authenticated()
            )
            // CONFIGURACIÓN ANTI-POPUP NEGRO
            .httpBasic(basic -> basic.authenticationEntryPoint((request, response, authException) -> {
                // Forzamos 401 pero sin el encabezado que dispara el cuadro del navegador
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.addHeader("X-Suppress-Basic", "true"); 
                response.getWriter().write("{\"error\": \"Sesion requerida\"}");
            }))
            .formLogin(form -> form.disable())
            .logout(logout -> logout.permitAll());

        return http.build();
    }
}
