package com.ignacio.banco.comun;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI bancoOpenApi() {
        return new OpenAPI().info(new Info().title("Banco Digital · API")
            .version("1.0.0")
            .description("Simulador educativo con dinero ficticio. Autenticación mediante POST /login "
                + "(formulario username, password y _csrf). Las escrituras requieren sesión y X-CSRF-TOKEN "
                + "obtenido en GET /api/auth/csrf. Consultar colección Postman para el flujo completo."))
            .components(new Components().addSecuritySchemes("sesion",new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE).name("JSESSIONID")));
    }
}
