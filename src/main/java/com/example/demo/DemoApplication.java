package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UsuarioService service, UsuarioRepository repository) {
        return args -> {
            // --- 1. CREACIÓN DEL ADMINISTRADOR (RF-05) ---
            // Buscamos si 'daniel' ya existe para no crear duplicados cada vez que reinicies
            if (repository.findByUsername("daniel").isEmpty()) {
                Usuario u = new Usuario();
                u.setUsername("daniel");
                u.setPassword("12345");
                u.setEmail("daniel@correo.com");
                u.setRol("ADMIN"); // Rol con todos los permisos

                service.registrar(u);
                System.out.println("✅ ADMIN 'daniel' listo en la base de datos.");
            }

            // --- 2. CREACIÓN DEL AUDITOR (REQUERIMIENTO RF-05) ---
            // Este usuario servirá para probar que alguien con menos permisos NO puede crear productos
            if (repository.findByUsername("auditor1").isEmpty()) {
                Usuario a = new Usuario();
                a.setUsername("auditor1");
                a.setPassword("pass123"); // Contraseña diferente para el auditor
                a.setEmail("auditor@correo.com");
                a.setRol("AUDITOR"); // Rol restringido

                service.registrar(a);
                System.out.println("✅ AUDITOR 'auditor1' listo en la base de datos.");
            }
        };
    }
}