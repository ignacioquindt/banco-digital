package com.ignacio.banco.recuperacion;



import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
@io.swagger.v3.oas.annotations.tags.Tag(name = "Recuperación")
@RestController
@RequestMapping("/api/auth")
public class RecuperacionController {
    private final RecuperacionService servicio;
    public RecuperacionController(RecuperacionService servicio) { this.servicio=servicio; }
    public record Solicitud(@NotBlank @Email @Size(max=254) String email) {}
    public record Restablecer(@NotBlank @Size(max=128) String token,
        @NotBlank @Size(min=12,max=64) String password) {}
    @PostMapping("/recuperar") public Map<String,String> recuperar(@Valid @RequestBody Solicitud datos) {
        servicio.solicitar(datos.email());
        return Map.of("mensaje", "Si el correo está registrado, recibirás un enlace de recuperación.");
    }
    @PostMapping("/restablecer") public Map<String,String> restablecer(@Valid @RequestBody Restablecer datos) {
        servicio.restablecer(datos.token(),datos.password());
        return Map.of("mensaje", "Contraseña actualizada. Ya podés iniciar sesión.");
    }
}
