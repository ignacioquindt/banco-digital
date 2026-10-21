package com.ignacio.banco.comun;



import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.util.LinkedHashMap;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> manejarError(
            ResponseStatusException ex) {

        String mensaje = ex.getReason() != null
            ? ex.getReason()
            : "No se pudo completar la solicitud";

        return ResponseEntity
            .status(ex.getStatusCode())
            .body(Map.<String, Object>of(
                "status", ex.getStatusCode().value(),
                "mensaje", mensaje
            ));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, Object>> manejarValidacion(
        MethodArgumentNotValidException ex) {

    Map<String, String> campos = new LinkedHashMap<>();

    ex.getBindingResult().getFieldErrors().forEach(error ->
        campos.putIfAbsent(
            error.getField(),
            error.getDefaultMessage() != null
                ? error.getDefaultMessage()
                : "Valor inválido"
        )
    );

    return ResponseEntity
        .badRequest()
        .body(Map.<String, Object>of(
            "status", 400,
            "mensaje", "Revisá los datos enviados",
            "campos", campos
        ));
}

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String,Object>> jsonInvalido() {
        return ResponseEntity.badRequest().body(Map.of("status",400,"mensaje","El formato de los datos no es válido"));
    }
}