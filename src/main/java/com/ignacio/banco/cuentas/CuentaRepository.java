package com.ignacio.banco.cuentas;



import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    boolean existsByNumeroCuenta(String numeroCuenta);

    List<Cuenta> findByUsuarioIdOrderByIdAsc(Long usuarioId);

    Optional<Cuenta> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cuenta c WHERE c.id = :id")
    Optional<Cuenta> buscarParaTransferencia(@Param("id") Long id);
}