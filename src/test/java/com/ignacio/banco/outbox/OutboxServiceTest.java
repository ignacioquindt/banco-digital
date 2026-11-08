package com.ignacio.banco.outbox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OutboxServiceTest {

    @Test
    void guardarPersisteEventoConEstadoPendiente() {
        OutboxRepository repository = mock(OutboxRepository.class);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        OutboxService service = new OutboxService(repository);
        OutboxEvento evento = service.guardar("CUENTA", "10", "DEPOSITO_REALIZADO", "{\"monto\":100}");

        assertNotNull(evento);
        assertEquals("CUENTA", evento.getAggregateType());
        assertEquals("10", evento.getAggregateId());
        assertEquals("DEPOSITO_REALIZADO", evento.getEventType());
        assertEquals("PENDIENTE", evento.getEstado());
        assertEquals(0, evento.getReintentos());
        verify(repository).save(any(OutboxEvento.class));
    }
}
