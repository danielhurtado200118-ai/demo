package com.example.demo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final UsuarioRepository repository;
    private final AuditoriaRepository auditoriaRepository;
    private final PasswordEncoder passwordEncoder;
    // 1. Inyectamos el servicio de intentos
    private final LoginAttemptService loginAttemptService;

    public UsuarioController(UsuarioService service, UsuarioRepository repository, 
                             AuditoriaRepository auditoriaRepository, 
                             PasswordEncoder passwordEncoder,
                             LoginAttemptService loginAttemptService) {
        this.service = service;
        this.repository = repository;
        this.auditoriaRepository = auditoriaRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
    }

    @GetMapping
    public List<Usuario> listar() {
        return repository.findAll();
    }

    @PostMapping
    public Usuario guardar(@RequestBody Usuario usuario) {
        String clavePlana = usuario.getPassword();
        String claveEncriptada = passwordEncoder.encode(clavePlana);
        usuario.setPassword(claveEncriptada);
        return repository.save(usuario);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        repository.deleteById(id);
    }

    @GetMapping("/logs")
    public List<Auditoria> listarLogs() {
        return auditoriaRepository.findAll();
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest login, HttpServletRequest request) {
        String username = login.getUsername();
        
        // 2. REGLA RS-07: Verificar si el usuario ya está bloqueado
        if (loginAttemptService.estaBloqueado(username)) {
            Auditoria logBloqueo = new Auditoria();
            logBloqueo.setUsuario(username);
            logBloqueo.setIpOrigen(request.getRemoteAddr());
            logBloqueo.setFechaHora(LocalDateTime.now());
            logBloqueo.setEvento("BLOQUEO_CUENTA");
            logBloqueo.setDetalles("Intento de acceso en cuenta bloqueada temporalmente (5 min).");
            auditoriaRepository.save(logBloqueo);
            
            return "❌ Cuenta bloqueada. Demasiados intentos fallidos. Intente en 5 minutos.";
        }

        var usuarioOpt = repository.findByUsername(username);
        boolean esValido = service.autenticar(username, login.getPassword());
        
        Auditoria log = new Auditoria();
        log.setUsuario(username);
        log.setIpOrigen(request.getRemoteAddr());
        log.setFechaHora(LocalDateTime.now());
        
        if (esValido && usuarioOpt.isPresent()) {
            // 3. Login exitoso: Limpiamos el contador de fallos
            loginAttemptService.loginExitoso(username);
            
            Usuario u = usuarioOpt.get();
            log.setEvento("LOGIN_EXITOSO");
            log.setDetalles("El usuario ingresó correctamente al sistema.");
            auditoriaRepository.save(log);

            return "✅|" + u.getRol() + "|TOKEN_SIMULADO";
        } else {
            // 4. Login fallido: Registramos el intento fallido en el servicio
            loginAttemptService.loginFallido(username);
            
            log.setEvento("LOGIN_FALLIDO");
            log.setDetalles("Intento de login fallido.");
            auditoriaRepository.save(log);
            return "❌ Error: Usuario o contraseña incorrectos.";
        }
    }
}