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
                // ESTO ARREGLA EL TEXTO DERRAMADO: Permitimos acceso total a archivos JS y CSS
                .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**", "/resources/**", "/api/usuarios/login").permitAll()
                
                // TABLAS: Cualquier usuario logueado puede ver los datos
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/usuarios/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/usuarios/**").authenticated()
                
                // Solo Admin/SuperAdmin borran
                .requestMatchers(HttpMethod.DELETE, "/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                
                .anyRequest().authenticated()
            )
            // QUITA LA VENTANA GRIS: Deshabilitamos el popup de "Acceder"
            .httpBasic(basic -> basic.disable())
            
            .formLogin(form -> form.loginPage("/").permitAll())
            .logout(logout -> logout.permitAll());

        return http.build();
    }
}
