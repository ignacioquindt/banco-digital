package com.ignacio.banco.transferencias;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "transferencias")
public class Transferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "origen_id", nullable = false)
    private Long origenId;

    @Column(name = "destino_id", nullable = false)
    private Long destinoId;

    @Column(nullable = false, precision = 19, scale = 2)
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private BigDecimal importe;

    @Column(nullable = false)
    private OffsetDateTime fecha;

    @Column(name = "clave_idempotencia", nullable = false, unique = true)
    private UUID claveIdempotencia;

    protected Transferencia() {
    }

    public Transferencia(
            Long origenId,
            Long destinoId,
            BigDecimal importe,
            UUID claveIdempotencia) {

        this.origenId = origenId;
        this.destinoId = destinoId;
        this.importe = importe;
        this.claveIdempotencia = claveIdempotencia;
        this.fecha = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public Long getOrigenId() {
        return origenId;
    }

    public Long getDestinoId() {
        return destinoId;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }

    public UUID getClaveIdempotencia() {
        return claveIdempotencia;
    }
}