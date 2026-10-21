package com.ignacio.banco.cuentas;



import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CuentaTest {

    @Test
    void acreditarSumaAlSaldo() {
        Cuenta cuenta = new Cuenta("TEST-001");

        cuenta.acreditar(new BigDecimal("100.00"));

        assertEquals(0,
            new BigDecimal("100.00").compareTo(cuenta.getSaldo()));
    }

    @Test
    void debitarRestaDelSaldo() {
        Cuenta cuenta = new Cuenta("TEST-002");
        cuenta.acreditar(new BigDecimal("100.00"));

        cuenta.debitar(new BigDecimal("30.00"));

        assertEquals(0,
            new BigDecimal("70.00").compareTo(cuenta.getSaldo()));
    }

    @Test
    void noPermiteDebitarSinSaldoSuficiente() {
        Cuenta cuenta = new Cuenta("TEST-003");
        cuenta.acreditar(new BigDecimal("50.00"));

        assertThrows(IllegalArgumentException.class,
            () -> cuenta.debitar(new BigDecimal("60.00")));

        assertEquals(0,
            new BigDecimal("50.00").compareTo(cuenta.getSaldo()));
    }

    @Test
    void noPermiteAcreditarImportesNegativos() {
        Cuenta cuenta = new Cuenta("TEST-004");

        assertThrows(IllegalArgumentException.class,
            () -> cuenta.acreditar(new BigDecimal("-10.00")));

        assertEquals(0,
            BigDecimal.ZERO.compareTo(cuenta.getSaldo()));
    }

    @Test
    void noPermiteImportesConMasDeDosDecimales() {
        Cuenta cuenta = new Cuenta("TEST-005");

        assertThrows(IllegalArgumentException.class,
            () -> cuenta.acreditar(new BigDecimal("0.001")));

        assertEquals(0,
            BigDecimal.ZERO.compareTo(cuenta.getSaldo()));
    }
    
    @Test
    void cuentaSinDineroNoTieneSaldo() {
    Cuenta cuenta = new Cuenta("TEST-001", 1L);

    assertFalse(cuenta.tieneSaldo());
    }
    @Test
    void cuentaConDineroTieneSaldo() {
    Cuenta cuenta = new Cuenta("TEST-002", 1L);

    cuenta.acreditar(new BigDecimal("100.00"));

    assertTrue(cuenta.tieneSaldo());
    }

        @Test
    void alGastarTodoElSaldoLaCuentaQuedaSinDinero() {
        Cuenta cuenta = new Cuenta("TEST-003", 1L);
        cuenta.acreditar(new BigDecimal("100.00"));
        assertTrue(cuenta.tieneSaldo());
        cuenta.debitar(new BigDecimal("100.00"));
        assertFalse(cuenta.tieneSaldo());
    }
}