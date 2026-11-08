package com.ignacio.banco.eventos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificacionEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCompletarTransferencia(TransferenciaRealizadaEvent evento) {
        log.info("Notificación asíncrona: Transferencia #{} acreditada exitosamente hacia cuenta #{}. Importe: {} ARS",
                evento.transferenciaId(), evento.destinoId(), evento.importe());
    }
}
