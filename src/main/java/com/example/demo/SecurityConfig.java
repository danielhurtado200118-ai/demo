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
                // 1. IMPORTANTE: Permitir el acceso a los recursos estáticos para que el JS cargue bien
                .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**", "/api/usuarios/login").permitAll()
                
                // 2. Permitir que cualquier usuario autenticado vea (GET) y guarde (POST)
                // Esto evita la redirección al login que rompe el JSON
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/usuarios/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/usuarios/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/productos/**").authenticated()
                
                // 3. Restricciones de borrado de la UTN
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**", "/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                
                .anyRequest().authenticated()
            )
            // 4. Evitamos que Spring redirija las peticiones de la API al login
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.sendError(401, "No autorizado");
                    } else {
                        response.sendRedirect("/");
                    }
                })
            )
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.loginPage("/").permitAll());

        return http.build();
    }
}
