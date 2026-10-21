package com.ignacio.banco.transferencias;

import com.ignacio.banco.cuentas.Cuenta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransferenciaRepository
        extends JpaRepository<Transferencia, Long> {

    Optional<Transferencia> findByClaveIdempotencia(UUID claveIdempotencia);

    @Query("""
        SELECT t FROM Transferencia t
        WHERE t.origenId = :cuentaId OR t.destinoId = :cuentaId
        ORDER BY t.fecha DESC, t.id DESC
        """)
    List<Transferencia> buscarPorCuenta(
        @Param("cuentaId") Long cuentaId
    );

    @Query("""
        SELECT t FROM Transferencia t
        WHERE t.origenId IN (
            SELECT c.id FROM Cuenta c WHERE c.usuarioId = :usuarioId
        )
        OR t.destinoId IN (
            SELECT c.id FROM Cuenta c WHERE c.usuarioId = :usuarioId
        )
        ORDER BY t.fecha DESC, t.id DESC
        """)
    List<Transferencia> buscarPorUsuario(
        @Param("usuarioId") Long usuarioId
    );
}