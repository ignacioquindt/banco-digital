package com.ignacio.banco.outbox;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<OutboxEvento, Long> {

    List<OutboxEvento> findTop50ByEstadoOrderByCreadoEnAsc(String estado);
}
