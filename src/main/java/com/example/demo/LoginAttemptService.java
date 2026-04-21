package com.example.demo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
    private final int MAX_INTENTOS = 5;
    private final long TIEMPO_BLOQUEO = 5; // minutos
    
    // Guarda: "usuario" -> [intentos, timestamp del ultimo fallo]
    private ConcurrentHashMap<String, IntentosData> usuariosBloqueados = new ConcurrentHashMap<>();

    public void loginFallido(String username) {
        IntentosData data = usuariosBloqueados.getOrDefault(username, new IntentosData());
        data.intentos++;
        data.ultimoFallo = System.currentTimeMillis();
        usuariosBloqueados.put(username, data);
    }

    public boolean estaBloqueado(String username) {
        if (!usuariosBloqueados.containsKey(username)) return false;
        
        IntentosData data = usuariosBloqueados.get(username);
        if (data.intentos >= MAX_INTENTOS) {
            long transcurrido = System.currentTimeMillis() - data.ultimoFallo;
            if (transcurrido < TimeUnit.MINUTES.toMillis(TIEMPO_BLOQUEO)) {
                return true; // Sigue bloqueado
            } else {
                usuariosBloqueados.remove(username); // Ya pasaron los 5 min
            }
        }
        return false;
    }

    public void loginExitoso(String username) {
        usuariosBloqueados.remove(username);
    }

    private static class IntentosData {
        int intentos = 0;
        long ultimoFallo = 0;
    }
}