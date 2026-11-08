package com.ignacio.banco.cuentas;

import com.ignacio.banco.auditoria.AuditoriaService;
import com.ignacio.banco.outbox.OutboxService;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CuentaServiceTest {

    private CuentaRepository cuentaRepository;
    private OperacionCuentaRepository operacionRepository;
    private AuditoriaService auditoriaService;
    private OutboxService outboxService;
    private CuentaService cuentaService;

    @BeforeEach
    void setUp() {
        cuentaRepository = mock(CuentaRepository.class);
        operacionRepository = mock(OperacionCuentaRepository.class);
        auditoriaService = mock(AuditoriaService.class);
        outboxService = mock(OutboxService.class);

        cuentaService = new CuentaService(
                cuentaRepository,
                operacionRepository,
                auditoriaService,
                outboxService
        );
    }

    @Test
    void depositarAcreditaFondosYRegistraOutbox() {
        Cuenta cuenta = new Cuenta("CTA-001", 1L);
        UUID clave = UUID.randomUUID();

        when(cuentaRepository.buscarParaTransferencia(10L)).thenReturn(Optional.of(cuenta));
        when(operacionRepository.findByClaveIdempotencia(clave)).thenReturn(Optional.empty());
        when(operacionRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        OperacionResponse res = cuentaService.depositar(10L, new BigDecimal("5000.00"), clave, 1L);

        assertEquals("DEPOSITO", res.tipo());
        assertEquals(0, new BigDecimal("5000.00").compareTo(cuenta.getSaldo()));
        verify(outboxService).guardar(eq("CUENTA"), eq("10"), eq("DEPOSITO_REALIZADO"), any());
        verify(auditoriaService).registrar(eq(1L), isNull(), eq("DEPOSITO"), any(), isNull(), eq(true));
    }

    @Test
    void extraerDebitaFondosSiHaySaldo() {
        Cuenta cuenta = new Cuenta("CTA-001", 1L);
        cuenta.acreditar(new BigDecimal("10000.00"));
        UUID clave = UUID.randomUUID();

        when(cuentaRepository.buscarParaTransferencia(10L)).thenReturn(Optional.of(cuenta));
        when(operacionRepository.findByClaveIdempotencia(clave)).thenReturn(Optional.empty());
        when(operacionRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        OperacionResponse res = cuentaService.extraer(10L, new BigDecimal("3000.00"), clave, 1L);

        assertEquals("EXTRACCION", res.tipo());
        assertEquals(0, new BigDecimal("7000.00").compareTo(cuenta.getSaldo()));
        verify(outboxService).guardar(eq("CUENTA"), eq("10"), eq("EXTRACCION_REALIZADA"), any());
    }

    @Test
    void extraerLanzaErrorSiSaldoEsInsuficiente() {
        Cuenta cuenta = new Cuenta("CTA-001", 1L);
        UUID clave = UUID.randomUUID();

        when(cuentaRepository.buscarParaTransferencia(10L)).thenReturn(Optional.of(cuenta));

        assertThrows(ResponseStatusException.class, () ->
                cuentaService.extraer(10L, new BigDecimal("1000.00"), clave, 1L)
        );
    }

    @Test
    void rechazaOperacionEnCuentaAjena() {
        Cuenta cuenta = new Cuenta("CTA-001", 2L); // Pertenece a usuario 2
        UUID clave = UUID.randomUUID();

        when(cuentaRepository.buscarParaTransferencia(10L)).thenReturn(Optional.of(cuenta));

        assertThrows(ResponseStatusException.class, () ->
                cuentaService.depositar(10L, new BigDecimal("1000.00"), clave, 1L) // Usuario 1 intenta operar
        );
    }
}
