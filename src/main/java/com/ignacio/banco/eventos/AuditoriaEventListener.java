package com.ignacio.banco.eventos;

import com.ignacio.banco.auditoria.AuditoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AuditoriaEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaEventListener.class);
    private final AuditoriaService auditoriaService;

    public AuditoriaEventListener(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCompletarTransferencia(TransferenciaRealizadaEvent evento) {
        log.info("Procesando evento de auditoría para transferencia #{} [CorrelationId: {}]",
                evento.transferenciaId(), evento.correlationId());

        String detalle = String.format("Monto: %s ARS | Origen: %d -> Destino: %d | Clave: %s",
                evento.importe(), evento.origenId(), evento.destinoId(), evento.claveIdempotencia());

        auditoriaService.registrar(
                evento.usuarioId(),
                null,
                "TRANSFERENCIA",
                detalle,
                null,
                true
        );
    }
}
