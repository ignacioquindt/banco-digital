package com.ignacio.banco.comun;



import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EstadoController {

    @GetMapping("/api/estado")
    public Map<String, String> obtenerEstado() {
        return Map.of(
            "estado", "OK",
            "aplicacion", "banco-digital"
        );
    }
}