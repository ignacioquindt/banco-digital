package com.ignacio.banco.cuentas;

import com.ignacio.banco.auditoria.AuditoriaService;
import com.ignacio.banco.outbox.OutboxService;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final OperacionCuentaRepository operacionRepository;
    private final AuditoriaService auditoriaService;
    private final OutboxService outboxService;

    public CuentaService(
            CuentaRepository cuentaRepository,
            OperacionCuentaRepository operacionRepository,
            AuditoriaService auditoriaService,
            OutboxService outboxService) {

        this.cuentaRepository = cuentaRepository;
        this.operacionRepository = operacionRepository;
        this.auditoriaService = auditoriaService;
        this.outboxService = outboxService;
    }

    @Transactional
    public Cuenta crearCuenta(String numeroCuenta, Long usuarioId) {
        if (cuentaRepository.existsByNumeroCuenta(numeroCuenta)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El número de cuenta ya existe"
            );
        }

        try {
            Cuenta nueva = cuentaRepository.saveAndFlush(new Cuenta(numeroCuenta, usuarioId));
            if (auditoriaService != null) {
                auditoriaService.registrar(
                    usuarioId,
                    null,
                    "ALTA_CUENTA",
                    "Número de cuenta: " + numeroCuenta,
                    null,
                    true
                );
            }
            return nueva;
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se pudo crear la cuenta por un conflicto de datos"
            );
        }
    }

    @Transactional
    public OperacionResponse depositar(
            Long cuentaId,
            BigDecimal importe,
            UUID claveIdempotencia,
            Long usuarioId) {

        validarImporteYClave(importe, claveIdempotencia);

        Cuenta cuenta = buscarCuentaBloqueada(cuentaId);
        validarPropiedad(cuenta, usuarioId);

        Optional<OperacionCuenta> existente = operacionRepository.findByClaveIdempotencia(claveIdempotencia);
        if (existente.isPresent()) {
            OperacionCuenta anterior = existente.get();
            if (!anterior.getCuentaId().equals(cuentaId)
                    || anterior.getImporte().compareTo(importe) != 0
                    || !"DEPOSITO".equals(anterior.getTipo())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La clave de idempotencia ya fue usada con otros datos");
            }
            return OperacionResponse.desde(anterior);
        }

        try {
            cuenta.acreditar(importe);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        cuentaRepository.saveAndFlush(cuenta);

        OperacionCuenta operacion = operacionRepository.saveAndFlush(
                new OperacionCuenta(cuentaId, "DEPOSITO", importe, cuenta.getSaldo(), claveIdempotencia)
        );

        if (outboxService != null) {
            String payload = String.format("{\"operacionId\":%d,\"cuentaId\":%d,\"tipo\":\"DEPOSITO\",\"importe\":%s,\"saldoPosterior\":%s}",
                    operacion.getId(), cuentaId, importe, cuenta.getSaldo());
            outboxService.guardar("CUENTA", String.valueOf(cuentaId), "DEPOSITO_REALIZADO", payload);
        }

        if (auditoriaService != null) {
            auditoriaService.registrar(
                    usuarioId,
                    null,
                    "DEPOSITO",
                    "Acreditación de " + importe + " ARS en cuenta #" + cuentaId + ". Nuevo saldo: " + cuenta.getSaldo(),
                    null,
                    true
            );
        }

        return OperacionResponse.desde(operacion);
    }

    @Transactional
    public OperacionResponse extraer(
            Long cuentaId,
            BigDecimal importe,
            UUID claveIdempotencia,
            Long usuarioId) {

        validarImporteYClave(importe, claveIdempotencia);

        Cuenta cuenta = buscarCuentaBloqueada(cuentaId);
        validarPropiedad(cuenta, usuarioId);

        Optional<OperacionCuenta> existente = operacionRepository.findByClaveIdempotencia(claveIdempotencia);
        if (existente.isPresent()) {
            OperacionCuenta anterior = existente.get();
            if (!anterior.getCuentaId().equals(cuentaId)
                    || anterior.getImporte().compareTo(importe) != 0
                    || !"EXTRACCION".equals(anterior.getTipo())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La clave de idempotencia ya fue usada con otros datos");
            }
            return OperacionResponse.desde(anterior);
        }

        try {
            cuenta.debitar(importe);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        cuentaRepository.saveAndFlush(cuenta);

        OperacionCuenta operacion = operacionRepository.saveAndFlush(
                new OperacionCuenta(cuentaId, "EXTRACCION", importe, cuenta.getSaldo(), claveIdempotencia)
        );

        if (outboxService != null) {
            String payload = String.format("{\"operacionId\":%d,\"cuentaId\":%d,\"tipo\":\"EXTRACCION\",\"importe\":%s,\"saldoPosterior\":%s}",
                    operacion.getId(), cuentaId, importe, cuenta.getSaldo());
            outboxService.guardar("CUENTA", String.valueOf(cuentaId), "EXTRACCION_REALIZADA", payload);
        }

        if (auditoriaService != null) {
            auditoriaService.registrar(
                    usuarioId,
                    null,
                    "EXTRACCION",
                    "Débito de " + importe + " ARS en cuenta #" + cuentaId + ". Nuevo saldo: " + cuenta.getSaldo(),
                    null,
                    true
            );
        }

        return OperacionResponse.desde(operacion);
    }

    public Cuenta buscarCuentaPropia(Long id, Long usuarioId) {
        return cuentaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));
    }

    private Cuenta buscarCuentaBloqueada(Long id) {
        return cuentaRepository.buscarParaTransferencia(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));
    }

    private void validarPropiedad(Cuenta cuenta, Long usuarioId) {
        if (!usuarioId.equals(cuenta.getUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada");
        }
    }

    private void validarImporteYClave(BigDecimal importe, UUID claveIdempotencia) {
        if (importe == null || importe.signum() <= 0 || importe.stripTrailingZeros().scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Importe monetario inválido");
        }
        if (claveIdempotencia == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clave de idempotencia requerida");
        }
    }
}
