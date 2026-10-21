package com.ignacio.banco.usuarios;



import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Registro")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistroService registroService;

    public AuthController(RegistroService registroService) {
        this.registroService = registroService;
    }

    @PostMapping("/registro")
    public ResponseEntity<RegistroRespuesta> registrar(
            @Valid @RequestBody RegistroRequest solicitud) {

        Usuario usuario;

        try {
            usuario = registroService.registrar(solicitud);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se pudo completar el registro por un conflicto de datos"
            );
        }

        RegistroRespuesta respuesta = new RegistroRespuesta(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getEmail()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(respuesta);
    }

    public record RegistroRespuesta(
        Long id,
        String nombre,
        String email
    ) {
    }
}