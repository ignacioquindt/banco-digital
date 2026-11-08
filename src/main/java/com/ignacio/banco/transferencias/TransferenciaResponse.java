package com.ignacio.banco.transferencias;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransferenciaResponse(
        Long id,
        Long origenId,
        Long destinoId,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal importe,
        OffsetDateTime fecha,
        UUID claveIdempotencia
) {
    public static TransferenciaResponse desde(Transferencia t) {
        if (t == null) {
            return null;
        }
        return new TransferenciaResponse(
                t.getId(),
                t.getOrigenId(),
                t.getDestinoId(),
                t.getImporte(),
                t.getFecha(),
                t.getClaveIdempotencia()
        );
    }
}
