package com.ignacio.banco.cuentas;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CrearCuentaRequest(

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Pattern(
        regexp = "[A-Za-z0-9-]{1,20}",
        message = "Debe tener entre 1 y 20 letras, números o guiones"
    )
    String numeroCuenta

) {
}