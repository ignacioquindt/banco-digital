package com.ignacio.banco.auditoria;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<AuditoriaOperacion, Long> {

    List<AuditoriaOperacion> findTop100ByOrderByFechaDesc();

    List<AuditoriaOperacion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
