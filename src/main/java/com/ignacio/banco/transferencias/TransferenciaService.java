package com.ignacio.banco.transferencias;

import com.ignacio.banco.comun.CorrelationIdFilter;
import com.ignacio.banco.cuentas.Cuenta;
import com.ignacio.banco.cuentas.CuentaRepository;
import com.ignacio.banco.eventos.TransferenciaRealizadaEvent;
import com.ignacio.banco.outbox.OutboxService;

import java.math.BigDecimal;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransferenciaService {

    private final CuentaRepository cuentaRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OutboxService outboxService;

    @org.springframework.beans.factory.annotation.Autowired
    public TransferenciaService(
            CuentaRepository cuentaRepository,
            TransferenciaRepository transferenciaRepository,
            ApplicationEventPublisher eventPublisher,
            OutboxService outboxService) {

        this.cuentaRepository = cuentaRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.eventPublisher = eventPublisher;
        this.outboxService = outboxService;
    }

    public TransferenciaService(
            CuentaRepository cuentaRepository,
            TransferenciaRepository transferenciaRepository) {

        this(cuentaRepository, transferenciaRepository, null, null);
    }

    @Transactional
    public Transferencia transferir(
            Long origenId,
            Long destinoId,
            BigDecimal importe,
            UUID claveIdempotencia,
            Long usuarioId) {

        if (usuarioId == null || usuarioId <= 0) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Debés iniciar sesión"
            );
        }

        if (origenId == null || destinoId == null
                || origenId <= 0 || destinoId <= 0
                || claveIdempotencia == null
                || importe == null
                || importe.signum() <= 0
                || importe.stripTrailingZeros().scale() > 2
                || importe.compareTo(
                    new BigDecimal("99999999999999999.99")
                ) > 0) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Datos de transferencia inválidos"
            );
        }

        if (origenId.equals(destinoId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Las cuentas deben ser distintas"
            );
        }

        Long primeroId = Math.min(origenId, destinoId);
        Long segundoId = Math.max(origenId, destinoId);

        Cuenta primera = buscarCuenta(primeroId);
        Cuenta segunda = buscarCuenta(segundoId);

        Cuenta origen = origenId.equals(primeroId)
            ? primera : segunda;

        Cuenta destino = destinoId.equals(primeroId)
            ? primera : segunda;

        if (!usuarioId.equals(origen.getUsuarioId())) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cuenta de origen no encontrada"
            );
        }

        var existente = transferenciaRepository
            .findByClaveIdempotencia(claveIdempotencia);

        if (existente.isPresent()) {
            Transferencia anterior = existente.get();

            if (!anterior.getOrigenId().equals(origenId)
                    || !anterior.getDestinoId().equals(destinoId)
                    || anterior.getImporte().compareTo(importe) != 0) {

                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La clave ya fue utilizada con otros datos"
                );
            }

            return anterior;
        }

        if (!origen.getMoneda().equals(destino.getMoneda())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Las cuentas deben tener la misma moneda"
            );
        }

        try {
            origen.debitar(importe);
            destino.acreditar(importe);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
            );
        }

        Transferencia guardada = transferenciaRepository.saveAndFlush(
            new Transferencia(
                origenId,
                destinoId,
                importe,
                claveIdempotencia
            )
        );

        if (outboxService != null) {
            String payload = String.format("{\"transferenciaId\":%d,\"origenId\":%d,\"destinoId\":%d,\"importe\":%s,\"claveIdempotencia\":\"%s\"}",
                guardada.getId(), origenId, destinoId, importe, claveIdempotencia);
            outboxService.guardar("TRANSFERENCIA", String.valueOf(guardada.getId()), "TRANSFERENCIA_REALIZADA", payload);
        }

        if (eventPublisher != null) {
            eventPublisher.publishEvent(new TransferenciaRealizadaEvent(
                guardada.getId(),
                guardada.getOrigenId(),
                guardada.getDestinoId(),
                guardada.getImporte(),
                guardada.getClaveIdempotencia(),
                usuarioId,
                guardada.getFecha(),
                MDC.get(CorrelationIdFilter.MDC_KEY)
            ));
        }

        return guardada;
    }

    private Cuenta buscarCuenta(Long id) {
        return cuentaRepository.buscarParaTransferencia(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cuenta no encontrada"
            ));
    }
}