package com.ignacio.banco.usuarios;



import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrar(RegistroRequest solicitud) {
        String nombre = solicitud.nombre().trim();
        String email = solicitud.email().trim()
            .toLowerCase(Locale.ROOT);

        PerfilService.validarPassword(solicitud.password());

        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El correo ya está registrado"
            );
        }

        String passwordHash = passwordEncoder.encode(
            solicitud.password()
        );

        Usuario usuario = new Usuario(
            nombre,
            email,
            passwordHash
        );

        return usuarioRepository.saveAndFlush(usuario);
    }
}