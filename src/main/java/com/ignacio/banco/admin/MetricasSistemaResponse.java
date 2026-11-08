package com.ignacio.banco.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;

public record MetricasSistemaResponse(
        long totalUsuarios,
        long totalCuentas,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal saldoTotalEnCirculacion,
        long totalTransferencias
) {
}
