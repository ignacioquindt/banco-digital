package com.ignacio.banco.outbox;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "outbox_eventos")
public class OutboxEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(nullable = false)
    private int reintentos = 0;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "procesado_en")
    private OffsetDateTime procesadoEn;

    protected OutboxEvento() {
    }

    public OutboxEvento(
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload,
            String correlationId) {

        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.correlationId = correlationId;
        this.creadoEn = OffsetDateTime.now(ZoneOffset.UTC);
        this.estado = "PENDIENTE";
    }

    public void marcarProcesado() {
        this.estado = "PROCESADO";
        this.procesadoEn = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void registrarFallo() {
        this.reintentos++;
        if (this.reintentos >= 3) {
            this.estado = "FALLIDO";
        }
    }

    public Long getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getEstado() {
        return estado;
    }

    public int getReintentos() {
        return reintentos;
    }

    public OffsetDateTime getCreadoEn() {
        return creadoEn;
    }

    public OffsetDateTime getProcesadoEn() {
        return procesadoEn;
    }
}
