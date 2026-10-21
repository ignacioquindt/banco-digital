package com.ignacio.banco.recuperacion;

import com.ignacio.banco.usuarios.PerfilService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RecuperacionTokenTest {
    @Test void venceEnElInstanteLimite() {
        Instant ahora=Instant.parse("2026-10-15T12:00:00Z");
        var token=new RecuperacionToken("hash",1L,ahora,0);
        assertFalse(token.esValido(ahora,0));
    }
    @Test void soloSeUsaUnaVez() {
        Instant ahora=Instant.now();var token=new RecuperacionToken("hash",1L,ahora.plusSeconds(60),0);
        assertTrue(token.esValido(ahora,0));token.consumir();assertFalse(token.esValido(ahora,0));
    }
    @Test void unCambioDePasswordInvalidaEnlacesAnteriores() {
        Instant ahora=Instant.now();var token=new RecuperacionToken("hash",1L,ahora.plusSeconds(60),0);
        assertFalse(token.esValido(ahora,1));
    }
    @Test void hashEsDeterministaYNoGuardaElSecreto() {
        String secreto="un-token-de-prueba";String hash=RecuperacionService.hash(secreto);
        assertEquals(64,hash.length());assertEquals(hash,RecuperacionService.hash(secreto));assertNotEquals(secreto,hash);
    }
    @Test void passwordValidaBytesAdemasDeCaracteres() {
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            ()->PerfilService.validarPassword("é".repeat(40)));
        assertDoesNotThrow(()->PerfilService.validarPassword("MiPassword2026!"));
    }
}
