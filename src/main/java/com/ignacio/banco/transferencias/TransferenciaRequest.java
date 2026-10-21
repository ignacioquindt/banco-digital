package com.ignacio.banco.transferencias;



import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferenciaRequest(

    @NotNull(message = "La cuenta de origen es obligatoria")
    @Positive(message = "El ID de origen debe ser positivo")
    Long origenId,

    @NotNull(message = "La cuenta de destino es obligatoria")
    @Positive(message = "El ID de destino debe ser positivo")
    Long destinoId,

    @NotNull(message = "El importe es obligatorio")
    @DecimalMin(
        value = "0.01",
        message = "El importe debe ser de al menos $0,01"
    )
    @Digits(
        integer = 17,
        fraction = 2,
        message = "El importe admite hasta 17 enteros y 2 decimales"
    )
    BigDecimal importe,

    @NotNull(message = "La clave de idempotencia es obligatoria")
    UUID claveIdempotencia

) {
}