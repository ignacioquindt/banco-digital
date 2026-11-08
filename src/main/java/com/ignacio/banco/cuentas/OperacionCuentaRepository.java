package com.ignacio.banco.cuentas;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperacionCuentaRepository extends JpaRepository<OperacionCuenta, Long> {

    Optional<OperacionCuenta> findByClaveIdempotencia(UUID claveIdempotencia);

    List<OperacionCuenta> findByCuentaIdOrderByFechaDesc(Long cuentaId);
}
