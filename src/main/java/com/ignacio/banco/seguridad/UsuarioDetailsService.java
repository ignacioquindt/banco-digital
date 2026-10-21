package com.ignacio.banco.seguridad;

import com.ignacio.banco.usuarios.Usuario;
import com.ignacio.banco.usuarios.UsuarioRepository;

import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        String emailNormalizado = email.trim()
            .toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
            .findByEmail(emailNormalizado)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Credenciales inválidas"
            ));

        return User.withUsername(usuario.getEmail())
            .password(usuario.getPasswordHash())
            .roles("CLIENTE")
            .disabled(!usuario.isActivo())
            .build();
    }
}