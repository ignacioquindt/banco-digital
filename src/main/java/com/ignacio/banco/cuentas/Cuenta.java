package com.ignacio.banco.cuentas;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "cuentas")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", nullable = false,
            unique = true, length = 20)
    private String numeroCuenta;

    @Column(nullable = false, length = 3)
    private String moneda = "ARS";

    @Column(nullable = false, precision = 19, scale = 2)
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private BigDecimal saldo = new BigDecimal("0.00");
    
    @Column(name = "usuario_id")
    private Long usuarioId;

    protected Cuenta() {
        // Constructor requerido por JPA.
    }

    public Cuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public Long getId() {
        return id;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public String getMoneda() {
        return moneda;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
    
    public boolean tieneSaldo() {
    return saldo.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public Long getUsuarioId() {
    return usuarioId;
}
    
    public void debitar(BigDecimal importe) {
    validarImporte(importe);

    if (saldo.compareTo(importe) < 0) {
        throw new IllegalArgumentException("Saldo insuficiente");
    }

    saldo = saldo.subtract(importe);
}

public void acreditar(BigDecimal importe) {
    validarImporte(importe);

    BigDecimal nuevoSaldo = saldo.add(importe);

    if (nuevoSaldo.compareTo(
            new BigDecimal("99999999999999999.99")) > 0) {
        throw new IllegalArgumentException(
            "El saldo supera el máximo permitido"
        );
    }

    saldo = nuevoSaldo;
}

public Cuenta(String numeroCuenta, Long usuarioId) {
    this(numeroCuenta);
    this.usuarioId = usuarioId;
}

private void validarImporte(BigDecimal importe) {
    if (importe == null || importe.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException(
            "El importe debe ser mayor que cero"
        );
    }

    if (importe.stripTrailingZeros().scale() > 2) {
        throw new IllegalArgumentException(
            "El importe no puede tener más de dos decimales"
        );
    }
}
}