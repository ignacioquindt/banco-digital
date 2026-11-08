package com.ignacio.banco.cuentas;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OperacionResponse(
        Long id,
        Long cuentaId,
        String tipo,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal importe,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal saldoPosterior,
        OffsetDateTime fecha,
        UUID claveIdempotencia
) {
    public static OperacionResponse desde(OperacionCuenta op) {
        if (op == null) {
            return null;
        }
        return new OperacionResponse(
                op.getId(),
                op.getCuentaId(),
                op.getTipo(),
                op.getImporte(),
                op.getSaldoPosterior(),
                op.getFecha(),
                op.getClaveIdempotencia()
        );
    }
}
