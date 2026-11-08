package com.ignacio.banco.cuentas;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record OperacionRequest(
        @NotNull(message = "El importe es obligatorio")
        @DecimalMin(value = "0.01", message = "El importe debe ser mayor que cero")
        BigDecimal importe,

        @NotNull(message = "La clave de idempotencia es obligatoria")
        UUID claveIdempotencia
) {
}
