package com.ignacio.banco.auditoria;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "auditoria_operaciones")
public class AuditoriaOperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(length = 254)
    private String email;

    @Column(nullable = false, length = 100)
    private String operacion;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(length = 45)
    private String ip;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(nullable = false)
    private OffsetDateTime fecha;

    @Column(nullable = false)
    private boolean exito;

    protected AuditoriaOperacion() {
    }

    public AuditoriaOperacion(
            Long usuarioId,
            String email,
            String operacion,
            String detalle,
            String ip,
            String correlationId,
            boolean exito) {

        this.usuarioId = usuarioId;
        this.email = email;
        this.operacion = operacion;
        this.detalle = detalle;
        this.ip = ip;
        this.correlationId = correlationId;
        this.exito = exito;
        this.fecha = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getEmail() {
        return email;
    }

    public String getOperacion() {
        return operacion;
    }

    public String getDetalle() {
        return detalle;
    }

    public String getIp() {
        return ip;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }

    public boolean isExito() {
        return exito;
    }
}
