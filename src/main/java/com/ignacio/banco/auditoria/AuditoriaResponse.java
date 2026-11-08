package com.ignacio.banco.auditoria;

import java.time.OffsetDateTime;

public record AuditoriaResponse(
        Long id,
        Long usuarioId,
        String email,
        String operacion,
        String detalle,
        String ip,
        String correlationId,
        OffsetDateTime fecha,
        boolean exito
) {
    public static AuditoriaResponse desde(AuditoriaOperacion a) {
        if (a == null) {
            return null;
        }
        return new AuditoriaResponse(
                a.getId(),
                a.getUsuarioId(),
                a.getEmail(),
                a.getOperacion(),
                a.getDetalle(),
                a.getIp(),
                a.getCorrelationId(),
                a.getFecha(),
                a.isExito()
        );
    }
}
