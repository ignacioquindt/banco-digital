package com.ignacio.banco.transferencias;

import com.ignacio.banco.cuentas.Cuenta;
import com.ignacio.banco.cuentas.CuentaRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransferenciaServiceTest {

    private CuentaRepository cuentaRepository;
    private TransferenciaRepository transferenciaRepository;
    private TransferenciaService servicio;

    private Cuenta origen;
    private Cuenta destino;
    private UUID clave;

    @BeforeEach
    void preparar() {
        cuentaRepository = mock(CuentaRepository.class);
        transferenciaRepository = mock(TransferenciaRepository.class);

        servicio = new TransferenciaService(
            cuentaRepository,
            transferenciaRepository
        );

        origen = new Cuenta("TEST-ORIGEN", 1L);
        destino = new Cuenta("TEST-DESTINO", 2L);

        origen.acreditar(new BigDecimal("1000.00"));
        destino.acreditar(new BigDecimal("200.00"));

        clave = UUID.randomUUID();

        lenient().when(cuentaRepository.buscarParaTransferencia(1L))
            .thenReturn(Optional.of(origen));

        lenient().when(cuentaRepository.buscarParaTransferencia(2L))
            .thenReturn(Optional.of(destino));

        lenient().when(
            transferenciaRepository.findByClaveIdempotencia(clave)
        ).thenReturn(Optional.empty());

        lenient().when(
            transferenciaRepository.saveAndFlush(any(Transferencia.class))
        ).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void transferirActualizaSaldosYGuardaLaTransferencia() {
        Transferencia resultado = servicio.transferir(
            1L, 2L, new BigDecimal("100.00"), clave, 1L
        );

        comprobarSaldo("900.00", origen);
        comprobarSaldo("300.00", destino);

        assertEquals(Long.valueOf(1L), resultado.getOrigenId());
        assertEquals(Long.valueOf(2L), resultado.getDestinoId());

        assertEquals(
            0,
            new BigDecimal("100.00").compareTo(resultado.getImporte())
        );

        assertEquals(clave, resultado.getClaveIdempotencia());

        verify(transferenciaRepository).saveAndFlush(resultado);
    }

    @Test
    void rechazaSaldoInsuficienteSinGuardar() {
        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> servicio.transferir(
                1L, 2L, new BigDecimal("1500.00"), clave, 1L
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());

        comprobarSaldo("1000.00", origen);
        comprobarSaldo("200.00", destino);

        verify(transferenciaRepository, never())
            .saveAndFlush(any(Transferencia.class));
    }

    @Test
    void repetirLaClaveConLosMismosDatosNoMueveDinero() {
        Transferencia anterior = new Transferencia(
            1L, 2L, new BigDecimal("100.00"), clave
        );

        when(transferenciaRepository.findByClaveIdempotencia(clave))
            .thenReturn(Optional.of(anterior));

        Transferencia resultado = servicio.transferir(
            1L, 2L, new BigDecimal("100.00"), clave, 1L
        );

        assertSame(anterior, resultado);

        comprobarSaldo("1000.00", origen);
        comprobarSaldo("200.00", destino);

        verify(transferenciaRepository, never())
            .saveAndFlush(any(Transferencia.class));
    }

    @Test
    void repetirLaClaveConOtroImporteDevuelveConflicto() {
        Transferencia anterior = new Transferencia(
            1L, 2L, new BigDecimal("100.00"), clave
        );

        when(transferenciaRepository.findByClaveIdempotencia(clave))
            .thenReturn(Optional.of(anterior));

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> servicio.transferir(
                1L, 2L, new BigDecimal("200.00"), clave, 1L
            )
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());

        comprobarSaldo("1000.00", origen);
        comprobarSaldo("200.00", destino);

        verify(transferenciaRepository, never())
            .saveAndFlush(any(Transferencia.class));
    }

    @Test
    void rechazaTransferirALaMismaCuenta() {
        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> servicio.transferir(
                1L, 1L, new BigDecimal("100.00"), clave, 1L
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());

        verifyNoInteractions(
            cuentaRepository,
            transferenciaRepository
        );
    }

    @Test
    void cuentaInexistenteDevuelveNoEncontrado() {
        when(cuentaRepository.buscarParaTransferencia(2L))
            .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> servicio.transferir(
                1L, 2L, new BigDecimal("100.00"), clave, 1L
            )
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());

        comprobarSaldo("1000.00", origen);
        comprobarSaldo("200.00", destino);

        verify(transferenciaRepository, never())
            .saveAndFlush(any(Transferencia.class));
    }

    @Test
    void noPermiteTransferirDesdeUnaCuentaAjena() {
        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> servicio.transferir(
                1L, 2L, new BigDecimal("100.00"), clave, 2L
            )
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());

        comprobarSaldo("1000.00", origen);
        comprobarSaldo("200.00", destino);

        verifyNoInteractions(transferenciaRepository);
    }

    private void comprobarSaldo(String esperado, Cuenta cuenta) {
        assertEquals(
            0,
            new BigDecimal(esperado).compareTo(cuenta.getSaldo())
        );
    }
}