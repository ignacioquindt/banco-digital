package com.ignacio.banco.usuarios;

import com.ignacio.banco.seguridad.UsuarioActualService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Perfil")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "sesion")
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {
    private final PerfilService servicio;
    private final UsuarioActualService actual;
    public PerfilController(PerfilService servicio, UsuarioActualService actual) {
        this.servicio = servicio; this.actual = actual;
    }
    public record PerfilRespuesta(Long id, String nombre, String email) {}
    public record NombreRequest(@NotBlank @Size(max=100) String nombre) {}
    public record PasswordRequest(@NotBlank String passwordActual,
        @NotBlank @Size(min=12,max=64) String passwordNueva) {}
    @GetMapping public PerfilRespuesta obtener(Authentication auth) {
        return servicio.obtener(actual.obtenerId(auth));
    }
    @PutMapping public PerfilRespuesta actualizar(@Valid @RequestBody NombreRequest datos, Authentication auth) {
        return servicio.actualizar(actual.obtenerId(auth), datos.nombre());
    }
    @PostMapping("/password") public Map<String,String> cambiar(
            @Valid @RequestBody PasswordRequest datos, Authentication auth,
            jakarta.servlet.http.HttpServletRequest request) {
        servicio.cambiarPassword(actual.obtenerId(auth), datos.passwordActual(), datos.passwordNueva());
        request.getSession().invalidate();
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        return Map.of("mensaje", "Contraseña actualizada. Iniciá sesión nuevamente.");
    }
}
