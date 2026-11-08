package com.ignacio.banco.eventos;

import com.ignacio.banco.auditoria.AuditoriaService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AuditoriaEventListenerTest {

    @Test
    void registraEventoDeAuditoriaAlRecibirTransferencia() {
        AuditoriaService auditoriaService = mock(AuditoriaService.class);
        AuditoriaEventListener listener = new AuditoriaEventListener(auditoriaService);

        TransferenciaRealizadaEvent evento = new TransferenciaRealizadaEvent(
                100L,
                1L,
                2L,
                new BigDecimal("500.00"),
                UUID.randomUUID(),
                5L,
                OffsetDateTime.now(),
                "test-corr-id"
        );

        listener.alCompletarTransferencia(evento);

        verify(auditoriaService).registrar(
                eq(5L),
                isNull(),
                eq("TRANSFERENCIA"),
                contains("500.00"),
                isNull(),
                eq(true)
        );
    }
}
