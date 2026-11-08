package com.ignacio.banco.outbox;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisherJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherJob.class);
    private final OutboxRepository outboxRepository;

    public OutboxPublisherJob(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void despacharEventosPendientes() {
        List<OutboxEvento> pendientes = outboxRepository.findTop50ByEstadoOrderByCreadoEnAsc("PENDIENTE");

        if (pendientes.isEmpty()) {
            return;
        }

        for (OutboxEvento evento : pendientes) {
            try {
                // Simulación de entrega a Kafka / Message Broker
                log.info("Outbox Dispatcher -> Publicando evento '{}' a topic {}. Payload: {}",
                        evento.getEventType(),
                        evento.getAggregateType().toLowerCase() + "-events",
                        evento.getPayload());

                evento.marcarProcesado();
                outboxRepository.save(evento);
            } catch (Exception ex) {
                log.error("Error al despachar evento Outbox #{}: {}", evento.getId(), ex.getMessage());
                evento.registrarFallo();
                outboxRepository.save(evento);
            }
        }
    }
}
