package com.ignacio.banco.cuentas;

import com.ignacio.banco.transferencias.Transferencia;
import com.ignacio.banco.transferencias.TransferenciaRepository;
import com.ignacio.banco.seguridad.UsuarioActualService;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Cuentas")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "sesion")
@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaRepository cuentaRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final UsuarioActualService usuarioActualService;

    public CuentaController(
            CuentaRepository cuentaRepository,
            TransferenciaRepository transferenciaRepository,
            UsuarioActualService usuarioActualService) {

        this.cuentaRepository = cuentaRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.usuarioActualService = usuarioActualService;
    }

    @GetMapping
    public List<Cuenta> listarCuentas(Authentication authentication) {
        Long usuarioId = usuarioActualService.obtenerId(authentication);

        return cuentaRepository.findByUsuarioIdOrderByIdAsc(usuarioId);
    }

    @GetMapping("/{id}")
    public Cuenta obtenerCuenta(
            @PathVariable("id") Long id,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);
        return buscarCuentaPropia(id, usuarioId);
    }

    @GetMapping("/{id}/movimientos")
    public List<Transferencia> obtenerMovimientos(
            @PathVariable("id") Long id,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);
        buscarCuentaPropia(id, usuarioId);

        return transferenciaRepository.buscarPorCuenta(id);
    }

    @PostMapping
    public ResponseEntity<Cuenta> crearCuenta(
            @Valid @RequestBody CrearCuentaRequest solicitud,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        if (cuentaRepository.existsByNumeroCuenta(solicitud.numeroCuenta())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El número de cuenta ya existe"
            );
        }

        Cuenta cuenta;

        try {
            cuenta = cuentaRepository.saveAndFlush(
                new Cuenta(solicitud.numeroCuenta(), usuarioId)
            );
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se pudo crear la cuenta por un conflicto de datos"
            );
        }

        return ResponseEntity
            .created(URI.create("/api/cuentas/" + cuenta.getId()))
            .body(cuenta);
    }

    private Cuenta buscarCuentaPropia(Long id, Long usuarioId) {
        return cuentaRepository.findByIdAndUsuarioId(id, usuarioId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cuenta no encontrada"
            ));
    }
}