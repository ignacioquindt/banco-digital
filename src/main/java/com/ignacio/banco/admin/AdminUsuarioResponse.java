package com.ignacio.banco.admin;

import com.ignacio.banco.usuarios.Rol;
import com.ignacio.banco.usuarios.Usuario;

public record AdminUsuarioResponse(
        Long id,
        String nombre,
        String email,
        Rol rol,
        boolean activo
) {
    public static AdminUsuarioResponse desde(Usuario u) {
        if (u == null) {
            return null;
        }
        return new AdminUsuarioResponse(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getRol(),
                u.isActivo()
        );
    }
}
