package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Usamos el encoder que ya tenías configurado
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF para facilitar las peticiones fetch
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                // Permitimos acceso total a la interfaz y al login
                .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**", "/api/usuarios/login").permitAll()
                
                // Las tablas requieren estar logueado
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/usuarios/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/usuarios/**").authenticated()
                
                // Solo Admin/SuperAdmin pueden borrar, como pediste en tu código
                .requestMatchers(HttpMethod.DELETE, "/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                .anyRequest().authenticated()
            )
            // ESTO ELIMINA EL POPUP GRIS Y EL ERROR DE JSON
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    // Si no hay permiso, enviamos 401 puro sin avisar al navegador que saque el popup
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"No autorizado\"}");
                })
            )
            // Mantenemos esto para que el AUTH_HEADER del JS funcione
            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form.loginPage("/").permitAll())
            .logout(logout -> logout.permitAll());

        return http.build();
    }
}
