package com.ignacio.banco.outbox;

import com.ignacio.banco.comun.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxService {

    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);
    private final OutboxRepository outboxRepository;

    public OutboxService(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public OutboxEvento guardar(
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload) {

        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);

        OutboxEvento evento = new OutboxEvento(
                aggregateType,
                aggregateId,
                eventType,
                payload,
                correlationId
        );

        OutboxEvento guardado = outboxRepository.save(evento);
        log.info("Transactional Outbox: Evento '{}' persistido para agregado {}#{} [ID: {}]",
                eventType, aggregateType, aggregateId, guardado.getId());

        return guardado;
    }
}
