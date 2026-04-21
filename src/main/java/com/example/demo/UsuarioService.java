package com.example.demo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario registrar(Usuario usuario) {
        // RF-02: Cifrado con BCrypt factor 12
        String hash = encoder.encode(usuario.getPassword());
        usuario.setPassword(hash);
        return repository.save(usuario);
    }
    public boolean autenticar(String username, String password) {
    return repository.findByUsername(username)
        .map(u -> encoder.matches(password, u.getPassword()))
        .orElse(false);
}
}

