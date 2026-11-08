package com.ignacio.banco.cuentas;

import com.ignacio.banco.seguridad.UsuarioActualService;
import com.ignacio.banco.transferencias.TransferenciaRepository;
import com.ignacio.banco.transferencias.TransferenciaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cuentas")
@SecurityRequirement(name = "sesion")
@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaRepository cuentaRepository;
    private final CuentaService cuentaService;
    private final OperacionCuentaRepository operacionRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final UsuarioActualService usuarioActualService;

    public CuentaController(
            CuentaRepository cuentaRepository,
            CuentaService cuentaService,
            OperacionCuentaRepository operacionRepository,
            TransferenciaRepository transferenciaRepository,
            UsuarioActualService usuarioActualService) {

        this.cuentaRepository = cuentaRepository;
        this.cuentaService = cuentaService;
        this.operacionRepository = operacionRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.usuarioActualService = usuarioActualService;
    }

    @Operation(summary = "Listar cuentas del usuario autenticado")
    @GetMapping
    public List<CuentaResponse> listarCuentas(Authentication authentication) {
        Long usuarioId = usuarioActualService.obtenerId(authentication);

        return cuentaRepository.findByUsuarioIdOrderByIdAsc(usuarioId)
                .stream()
                .map(CuentaResponse::desde)
                .toList();
    }

    @Operation(summary = "Obtener detalle de cuenta propia")
    @GetMapping("/{id}")
    public CuentaResponse obtenerCuenta(
            @PathVariable("id") Long id,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);
        return CuentaResponse.desde(cuentaService.buscarCuentaPropia(id, usuarioId));
    }

    @Operation(summary = "Crear nueva cuenta bancaria")
    @PostMapping
    public ResponseEntity<CuentaResponse> crearCuenta(
            @Valid @RequestBody CrearCuentaRequest solicitud,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);
        Cuenta cuenta = cuentaService.crearCuenta(solicitud.numeroCuenta(), usuarioId);

        return ResponseEntity
            .created(URI.create("/api/cuentas/" + cuenta.getId()))
            .body(CuentaResponse.desde(cuenta));
    }

    @Operation(summary = "Ingresar dinero / Cash-In (Depósito)")
    @PostMapping("/{id}/depositar")
    public OperacionResponse depositar(
            @PathVariable("id") Long id,
            @Valid @RequestBody OperacionRequest solicitud,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);
        return cuentaService.depositar(id, solicitud.importe(), solicitud.claveIdempotencia(), usuarioId);
    }

    @Operation(summary = "Extraer dinero / Cash-Out")
    @PostMapping("/{id}/extraer")
    public OperacionResponse extraer(
            @PathVariable("id") Long id,
            @Valid @RequestBody OperacionRequest solicitud,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);
        return cuentaService.extraer(id, solicitud.importe(), solicitud.claveIdempotencia(), usuarioId);
    }

    @Operation(summary = "Historial de depósitos y extracciones de la cuenta")
    @GetMapping("/{id}/operaciones")
    public List<OperacionResponse> listarOperaciones(
            @PathVariable("id") Long id,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);
        cuentaService.buscarCuentaPropia(id, usuarioId);

        return operacionRepository.findByCuentaIdOrderByFechaDesc(id)
                .stream()
                .map(OperacionResponse::desde)
                .toList();
    }

    @Operation(summary = "Movimientos de transferencias de la cuenta (Sin paginar)")
    @GetMapping("/{id}/movimientos")
    public List<TransferenciaResponse> obtenerMovimientos(
            @PathVariable("id") Long id,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);
        cuentaService.buscarCuentaPropia(id, usuarioId);

        return transferenciaRepository.buscarPorCuenta(id)
                .stream()
                .map(TransferenciaResponse::desde)
                .toList();
    }

    @Operation(summary = "Movimientos de transferencias con paginación empresarial")
    @GetMapping("/{id}/movimientos/paginado")
    public Page<TransferenciaResponse> obtenerMovimientosPaginados(
            @PathVariable("id") Long id,
            @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);
        cuentaService.buscarCuentaPropia(id, usuarioId);

        return transferenciaRepository.buscarPorCuentaPaginado(id, pageable)
                .map(TransferenciaResponse::desde);
    }
}
