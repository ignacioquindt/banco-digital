package com.ignacio.banco.cuentas;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;

public record CuentaResponse(
        Long id,
        String numeroCuenta,
        String moneda,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal saldo,
        Long usuarioId
) {
    public static CuentaResponse desde(Cuenta cuenta) {
        if (cuenta == null) {
            return null;
        }
        return new CuentaResponse(
                cuenta.getId(),
                cuenta.getNumeroCuenta(),
                cuenta.getMoneda(),
                cuenta.getSaldo(),
                cuenta.getUsuarioId()
        );
    }
}
