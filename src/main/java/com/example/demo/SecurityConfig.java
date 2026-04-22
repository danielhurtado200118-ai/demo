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
                // Recursos públicos
                .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**", "/resources/**", "/api/usuarios/login").permitAll()
                
                // TABLAS: Permite a cualquier logueado (necesario para que carguen los datos)
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/usuarios/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/usuarios/**").authenticated()
                
                // Solo Admin/SuperAdmin pueden borrar
                .requestMatchers(HttpMethod.DELETE, "/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                
                .anyRequest().authenticated()
            )
            // IMPORTANTE: Esto habilita que el AUTH_HEADER del JavaScript funcione
            .httpBasic(Customizer.withDefaults())
            
            // Si la API falla, responde con error 401 en vez de mandar el HTML del login
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            
            .formLogin(form -> form.loginPage("/").permitAll())
            .logout(logout -> logout.permitAll());

        return http.build();
    }
}
