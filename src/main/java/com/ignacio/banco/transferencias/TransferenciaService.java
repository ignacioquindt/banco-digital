package com.ignacio.banco.transferencias;

import com.ignacio.banco.cuentas.Cuenta;
import com.ignacio.banco.cuentas.CuentaRepository;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransferenciaService {

    private final CuentaRepository cuentaRepository;
    private final TransferenciaRepository transferenciaRepository;

    public TransferenciaService(
            CuentaRepository cuentaRepository,
            TransferenciaRepository transferenciaRepository) {

        this.cuentaRepository = cuentaRepository;
        this.transferenciaRepository = transferenciaRepository;
    }

    @Transactional
    public Transferencia transferir(
            Long origenId,
            Long destinoId,
            BigDecimal importe,
            UUID claveIdempotencia,
            Long usuarioId) {

        if (usuarioId == null || usuarioId <= 0) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Debés iniciar sesión"
            );
        }

        if (origenId == null || destinoId == null
                || origenId <= 0 || destinoId <= 0
                || claveIdempotencia == null
                || importe == null
                || importe.signum() <= 0
                || importe.stripTrailingZeros().scale() > 2
                || importe.compareTo(
                    new BigDecimal("99999999999999999.99")
                ) > 0) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Datos de transferencia inválidos"
            );
        }

        if (origenId.equals(destinoId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Las cuentas deben ser distintas"
            );
        }

        Long primeroId = Math.min(origenId, destinoId);
        Long segundoId = Math.max(origenId, destinoId);

        Cuenta primera = buscarCuenta(primeroId);
        Cuenta segunda = buscarCuenta(segundoId);

        Cuenta origen = origenId.equals(primeroId)
            ? primera : segunda;

        Cuenta destino = destinoId.equals(primeroId)
            ? primera : segunda;

        if (!usuarioId.equals(origen.getUsuarioId())) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cuenta de origen no encontrada"
            );
        }

        var existente = transferenciaRepository
            .findByClaveIdempotencia(claveIdempotencia);

        if (existente.isPresent()) {
            Transferencia anterior = existente.get();

            if (!anterior.getOrigenId().equals(origenId)
                    || !anterior.getDestinoId().equals(destinoId)
                    || anterior.getImporte().compareTo(importe) != 0) {

                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La clave ya fue utilizada con otros datos"
                );
            }

            return anterior;
        }

        if (!origen.getMoneda().equals(destino.getMoneda())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Las cuentas deben tener la misma moneda"
            );
        }

        try {
            origen.debitar(importe);
            destino.acreditar(importe);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
            );
        }

        return transferenciaRepository.saveAndFlush(
            new Transferencia(
                origenId,
                destinoId,
                importe,
                claveIdempotencia
            )
        );
    }

    private Cuenta buscarCuenta(Long id) {
        return cuentaRepository.buscarParaTransferencia(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cuenta no encontrada"
            ));
    }
}