package com.ignacio.banco.usuarios;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
    String nombre,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 254, message = "El correo admite hasta 254 caracteres")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 12, max = 64,
          message = "La contraseña debe tener entre 12 y 64 caracteres")
    String password

) {
}