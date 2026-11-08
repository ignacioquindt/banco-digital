package com.ignacio.banco.admin;

import com.ignacio.banco.usuarios.Rol;
import jakarta.validation.constraints.NotNull;

public record CambiarRolRequest(@NotNull Rol rol) {
}
