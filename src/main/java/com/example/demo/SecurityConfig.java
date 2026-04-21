package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

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
            .headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';"))
            )
            .authorizeHttpRequests(auth -> auth
                // Recursos básicos libres
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/api/usuarios/login").permitAll()
                
                // TABLAS: Cualquier usuario logueado puede VER (GET)
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/usuarios/**").authenticated()
                
                // ACCIONES: Mantenemos roles UTN para guardar/editar/borrar
                .requestMatchers(HttpMethod.POST, "/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN", "REGISTRADOR")
                .requestMatchers(HttpMethod.POST, "/api/usuarios/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                
                .anyRequest().authenticated()
            )
            // ESTO ARREGLA EL ERROR DEL JSON: 
            // Si no hay permiso para la API, devuelve un error 401 en vez de redirigir al login (HTML)
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), 
                    request -> request.getRequestURI().startsWith("/api/")
                )
            )
            .formLogin(form -> form.loginPage("/").permitAll())
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
