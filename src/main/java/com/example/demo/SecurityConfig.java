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
                // Permitir acceso a la web y al login
                .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**", "/api/usuarios/login").permitAll()
                // Proteger las APIs de datos
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/usuarios/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/usuarios/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                .anyRequest().authenticated()
            )
            // ESTO ELIMINA EL CUADRO NEGRO (POPUP) DEFINITIVAMENTE
            .httpBasic(basic -> basic.authenticationEntryPoint((request, response, authException) -> {
                // Enviamos 401 pero SIN el encabezado WWW-Authenticate
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
            }))
            .formLogin(form -> form.disable()) // Deshabilitamos el form de Spring para usar el tuyo
            .logout(logout -> logout.permitAll());

        return http.build();
    }
}
