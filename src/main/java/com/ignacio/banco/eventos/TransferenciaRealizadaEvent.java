package com.ignacio.banco.eventos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransferenciaRealizadaEvent(
        Long transferenciaId,
        Long origenId,
        Long destinoId,
        BigDecimal importe,
        UUID claveIdempotencia,
        Long usuarioId,
        OffsetDateTime fecha,
        String correlationId
) {
}
