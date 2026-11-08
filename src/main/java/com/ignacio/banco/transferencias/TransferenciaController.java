package com.ignacio.banco.transferencias;

import com.ignacio.banco.seguridad.UsuarioActualService;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Transferencias")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "sesion")
@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;
    private final TransferenciaRepository transferenciaRepository;
    private final UsuarioActualService usuarioActualService;

    public TransferenciaController(
            TransferenciaService transferenciaService,
            TransferenciaRepository transferenciaRepository,
            UsuarioActualService usuarioActualService) {

        this.transferenciaService = transferenciaService;
        this.transferenciaRepository = transferenciaRepository;
        this.usuarioActualService = usuarioActualService;
    }

    @GetMapping
    public List<TransferenciaResponse> listarTransferencias(
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);
        return transferenciaRepository.buscarPorUsuario(usuarioId)
                .stream()
                .map(TransferenciaResponse::desde)
                .toList();
    }

    @GetMapping("/paginado")
    public org.springframework.data.domain.Page<TransferenciaResponse> listarTransferenciasPaginado(
            @org.springframework.data.web.PageableDefault(size = 10, sort = "fecha", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);
        return transferenciaRepository.buscarPorUsuarioPaginado(usuarioId, pageable)
                .map(TransferenciaResponse::desde);
    }

    @PostMapping
    public TransferenciaResponse transferir(
            @Valid @RequestBody TransferenciaRequest solicitud,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        try {
            Transferencia creada = transferenciaService.transferir(
                solicitud.origenId(),
                solicitud.destinoId(),
                solicitud.importe(),
                solicitud.claveIdempotencia(),
                usuarioId
            );
            return TransferenciaResponse.desde(creada);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Conflicto de datos al registrar la transferencia"
            );
        }
    }
}