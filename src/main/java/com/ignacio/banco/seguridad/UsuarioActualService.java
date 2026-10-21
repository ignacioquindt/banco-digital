package com.ignacio.banco.seguridad;

import com.ignacio.banco.usuarios.Usuario;
import com.ignacio.banco.usuarios.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioActualService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioActualService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Long obtenerId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Debés iniciar sesión"
            );
        }

        return usuarioRepository.findByEmail(authentication.getName())
            .filter(Usuario::isActivo)
            .map(Usuario::getId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuario no disponible"
            ));
    }
}