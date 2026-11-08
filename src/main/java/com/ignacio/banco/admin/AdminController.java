package com.ignacio.banco.admin;

import com.ignacio.banco.auditoria.AuditoriaOperacion;
import com.ignacio.banco.auditoria.AuditoriaRepository;
import com.ignacio.banco.auditoria.AuditoriaResponse;
import com.ignacio.banco.cuentas.CuentaRepository;
import com.ignacio.banco.transferencias.TransferenciaRepository;
import com.ignacio.banco.usuarios.Usuario;
import com.ignacio.banco.usuarios.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Administración")
@SecurityRequirement(name = "sesion")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final CuentaRepository cuentaRepository;
    private final TransferenciaRepository transferenciaRepository;

    public AdminController(
            UsuarioRepository usuarioRepository,
            AuditoriaRepository auditoriaRepository,
            CuentaRepository cuentaRepository,
            TransferenciaRepository transferenciaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.cuentaRepository = cuentaRepository;
        this.transferenciaRepository = transferenciaRepository;
    }

    @Operation(summary = "Listar usuarios registrados (Solo ADMIN)")
    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminUsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(AdminUsuarioResponse::desde)
                .toList();
    }

    @Operation(summary = "Modificar rol de usuario (Solo ADMIN)")
    @PatchMapping("/usuarios/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminUsuarioResponse cambiarRol(
            @PathVariable("id") Long id,
            @Valid @RequestBody CambiarRolRequest request) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        usuario.asignarRol(request.rol());
        usuarioRepository.saveAndFlush(usuario);
        return AdminUsuarioResponse.desde(usuario);
    }

    @Operation(summary = "Modificar estado de activación de usuario (Solo ADMIN)")
    @PatchMapping("/usuarios/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminUsuarioResponse cambiarEstado(
            @PathVariable("id") Long id,
            @RequestBody CambiarEstadoRequest request) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        usuario.setActivo(request.activo());
        usuarioRepository.saveAndFlush(usuario);
        return AdminUsuarioResponse.desde(usuario);
    }

    @Operation(summary = "Métricas globales del sistema (ADMIN o AUDITOR)")
    @GetMapping("/metricas")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public MetricasSistemaResponse obtenerMetricas() {
        long totalUsuarios = usuarioRepository.count();
        long totalCuentas = cuentaRepository.count();
        BigDecimal totalSaldos = cuentaRepository.calcularSaldoTotal();
        long totalTransferencias = transferenciaRepository.count();

        return new MetricasSistemaResponse(
                totalUsuarios,
                totalCuentas,
                totalSaldos,
                totalTransferencias
        );
    }

    @Operation(summary = "Consultar registros de auditoría (ADMIN o AUDITOR)")
    @GetMapping("/auditoria")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public List<AuditoriaResponse> listarAuditoria() {
        return auditoriaRepository.findTop100ByOrderByFechaDesc()
                .stream()
                .map(AuditoriaResponse::desde)
                .toList();
    }
}
