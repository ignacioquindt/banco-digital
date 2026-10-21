package com.ignacio.banco.usuarios;



import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PerfilService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final SessionRegistry sesiones;
    public PerfilService(UsuarioRepository usuarios, PasswordEncoder encoder, SessionRegistry sesiones) {
        this.usuarios=usuarios; this.encoder=encoder; this.sesiones=sesiones;
    }
    @Transactional(readOnly=true)
    public PerfilController.PerfilRespuesta obtener(Long id) { return respuesta(buscar(id)); }
    @Transactional
    public PerfilController.PerfilRespuesta actualizar(Long id, String nombre) {
        Usuario usuario = bloqueado(id);
        usuario.actualizarNombre(nombre.trim());
        return respuesta(usuario);
    }
    @Transactional
    public void cambiarPassword(Long id, String actual, String nueva) {
        Usuario usuario = bloqueado(id);
        if (!encoder.matches(actual, usuario.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta");
        validarPassword(nueva);
        if (encoder.matches(nueva, usuario.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Elegí una contraseña diferente");
        usuario.cambiarPassword(encoder.encode(nueva));
        expirarDespuesDelCommit(usuario.getEmail());
    }
    public static void validarPassword(String password) {
        if (password==null || password.isBlank() || password.length()<12 || password.length()>64
                || password.getBytes(StandardCharsets.UTF_8).length>72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "La contraseña debe tener de 12 a 64 caracteres y hasta 72 bytes UTF-8");
    }
    public void expirarDespuesDelCommit(String email) {
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
            new org.springframework.transaction.support.TransactionSynchronization() {
                @Override public void afterCommit() {
                    sesiones.getAllPrincipals().stream()
                        .filter(p -> p instanceof UserDetails u && email.equals(u.getUsername()))
                        .forEach(p -> sesiones.getAllSessions(p, false).forEach(s -> s.expireNow()));
                }
            });
    }
    private Usuario buscar(Long id) { return usuarios.findById(id).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")); }
    private Usuario bloqueado(Long id) { return usuarios.buscarBloqueado(id).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")); }
    private PerfilController.PerfilRespuesta respuesta(Usuario u) {
        return new PerfilController.PerfilRespuesta(u.getId(),u.getNombre(),u.getEmail());
    }
}
