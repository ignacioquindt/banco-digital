package com.ignacio.banco.recuperacion;

import com.ignacio.banco.usuarios.Usuario;
import com.ignacio.banco.usuarios.UsuarioRepository;
import com.ignacio.banco.usuarios.PerfilService;

import java.time.Instant;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RecuperacionService {
    private final UsuarioRepository usuarios;
    private final RecuperacionRepository tokens;
    private final JavaMailSender correo;
    private final PasswordEncoder encoder;
    private final PerfilService perfil;
    private final boolean habilitado;
    private final String baseUrl;
    private final String remitente;
    private final SecureRandom random=new SecureRandom();
    public RecuperacionService(UsuarioRepository usuarios, RecuperacionRepository tokens,
            JavaMailSender correo, PasswordEncoder encoder, PerfilService perfil,
            @Value("${app.mail.enabled:false}") boolean habilitado,
            @Value("${app.base-url:http://localhost:8080}") String baseUrl,
            @Value("${app.mail.from:no-reply@banco.local}") String remitente) {
        this.usuarios=usuarios; this.tokens=tokens; this.correo=correo; this.encoder=encoder;
        this.perfil=perfil; this.habilitado=habilitado; this.baseUrl=baseUrl; this.remitente=remitente;
    }
    @Transactional
    public void solicitar(String email) {
        if (!habilitado) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "El envío de correos todavía no está configurado. Consultá la guía de instalación.");
        var encontrado=usuarios.findByEmail(email.trim().toLowerCase(Locale.ROOT));
        if (encontrado.isEmpty() || !encontrado.get().isActivo()) return;
        Usuario usuario=encontrado.get();
        byte[] bytes=new byte[32]; random.nextBytes(bytes);
        String secreto=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.saveAndFlush(new RecuperacionToken(hash(secreto), usuario.getId(),
            Instant.now().plusSeconds(1200), usuario.getCredencialesVersion()));
        SimpleMailMessage mensaje=new SimpleMailMessage();
        mensaje.setFrom(remitente); mensaje.setTo(usuario.getEmail());
        mensaje.setSubject("Restablecé tu contraseña · Banco Digital");
        mensaje.setText("Solicitaste recuperar tu acceso al simulador Banco Digital.\n\n"
            + baseUrl + "/restablecer.html#token=" + secreto
            + "\n\nEl enlace vence en 20 minutos y se usa una sola vez."
            + " Si no lo solicitaste, ignorá este correo.");
        try { correo.send(mensaje); }
        catch (org.springframework.mail.MailException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "No se pudo enviar el correo. Intentá más tarde.");
        }
    }
    @Transactional
    public void restablecer(String secreto, String password) {
        PerfilService.validarPassword(password);
        RecuperacionToken token=tokens.buscarBloqueado(hash(secreto)).orElseThrow(this::invalido);
        Usuario usuario=usuarios.buscarBloqueado(token.getUsuarioId()).orElseThrow(this::invalido);
        if (!usuario.isActivo() || !token.esValido(Instant.now(), usuario.getCredencialesVersion()))
            throw invalido();
        usuario.cambiarPassword(encoder.encode(password));
        token.consumir();
        perfil.expirarDespuesDelCommit(usuario.getEmail());
    }
    static String hash(String valor) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(valor.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
    private ResponseStatusException invalido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enlace es inválido o venció");
    }
}
