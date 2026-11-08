package com.ignacio.banco.cuentas;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "operaciones_cuenta")
public class OperacionCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_id", nullable = false)
    private Long cuentaId;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal importe;

    @Column(name = "saldo_posterior", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoPosterior;

    @Column(name = "clave_idempotencia", nullable = false, unique = true)
    private UUID claveIdempotencia;

    @Column(nullable = false)
    private OffsetDateTime fecha;

    protected OperacionCuenta() {
    }

    public OperacionCuenta(
            Long cuentaId,
            String tipo,
            BigDecimal importe,
            BigDecimal saldoPosterior,
            UUID claveIdempotencia) {

        this.cuentaId = cuentaId;
        this.tipo = tipo;
        this.importe = importe;
        this.saldoPosterior = saldoPosterior;
        this.claveIdempotencia = claveIdempotencia;
        this.fecha = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public String getTipo() {
        return tipo;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public BigDecimal getSaldoPosterior() {
        return saldoPosterior;
    }

    public UUID getClaveIdempotencia() {
        return claveIdempotencia;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }
}
