package com.ignacio.banco.recuperacion;



import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name="recuperacion_tokens")
public class RecuperacionToken {
    @Id @Column(length=64) private String hash;
    @Column(name="usuario_id",nullable=false) private Long usuarioId;
    @Column(nullable=false) private Instant vence;
    @Column(nullable=false) private boolean usado;
    @Column(name="credenciales_version",nullable=false) private long credencialesVersion;
    protected RecuperacionToken() {}
    public RecuperacionToken(String hash, Long usuarioId, Instant vence, long version) {
        this.hash=hash; this.usuarioId=usuarioId; this.vence=vence; this.credencialesVersion=version;
    }
    public Long getUsuarioId() { return usuarioId; }
    public boolean esValido(Instant ahora, long version) {
        return !usado && vence.isAfter(ahora) && credencialesVersion==version;
    }
    public void consumir() { usado=true; }
}
