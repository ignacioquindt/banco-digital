package com.ignacio.banco.auditoria;

import com.ignacio.banco.comun.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);
    private final AuditoriaRepository repository;

    public AuditoriaService(AuditoriaRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(
            Long usuarioId,
            String email,
            String operacion,
            String detalle,
            String ip,
            boolean exito) {

        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);

        try {
            repository.saveAndFlush(new AuditoriaOperacion(
                    usuarioId,
                    email,
                    operacion,
                    detalle,
                    ip,
                    correlationId,
                    exito
            ));
            log.info("Auditoría registrada: operacion={}, usuarioId={}, exito={}",
                    operacion, usuarioId, exito);
        } catch (Exception ex) {
            log.error("Fallo al registrar evento de auditoría: {}", ex.getMessage(), ex);
        }
    }
}
