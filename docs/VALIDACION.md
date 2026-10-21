# Registro de validación

## Pruebas automatizadas — 20/10/2026

Java OpenJDK 21.0.12.1, Spring Boot 4.1.1, PostgreSQL 18.6. Pruebas ejecutadas sobre una base aislada.

| Clase | Casos | Fallos | Errores | Omitidos |
|---|---:|---:|---:|---:|
| `BancoDigitalApplicationTests` | 1 | 0 | 0 | 0 |
| `SeguridadIntegracionTest` | 1 | 0 | 0 | 0 |
| `CuentaTest` | 8 | 0 | 0 | 0 |
| `RecuperacionTokenTest` | 5 | 0 | 0 | 0 |
| `TransferenciaServiceTest` | 7 | 0 | 0 | 0 |
| `TransferenciasPostgresIT` | 7 | 0 | 0 | 0 |

**29 casos aprobados, cero fallos/errores/omitidos:** 22 pruebas rápidas (incluido flujo HTTP con H2) y 7 contra PostgreSQL. Los tests usan transacciones y bloqueos reales del motor PostgreSQL.

También se verificó:

- Empaquetado ejecutable e inicio del JAR sobre PostgreSQL.
- Swagger UI y JSON OpenAPI: respuesta HTTP 200; el test HTTP comprueba la presencia de la API y que no exponga passwordHash.
- Colección Postman ejecutada mediante Newman: 17 solicitudes incluyendo registro, sesión con CSRF, cuentas, perfil, transferencia, reintento, 409, validación 400, logout y acceso anónimo 401. Se preparó saldo ficticio en la base de pruebas. El envío SMTP opcional se omitió de esta ejecución Newman.
- Frontend con jsdom: carga, suma decimal, creación, confirmación, fallo de red, reintento con la misma clave, comprobante y fecha Buenos Aires.
- YAML de Compose y CI, JSON de Postman y paquete final inspeccionados.

## Verificaciones manuales — 21/10/2026

Se verificaron localmente el arranque con Docker Desktop, el registro, la creación de cuentas, la edición de perfil y el cambio de contraseña. Se confirmó la conservación de los datos tras reiniciar los contenedores. También se verificaron la adopción de la base existente y la recuperación por Gmail en ejecución nativa.

## Verificaciones independientes

- El estado de CI se consulta en GitHub Actions; este registro no certifica una ejecución remota.
- jsdom verifica comportamiento, no apariencia ni compatibilidad con dispositivos físicos.
- La prueba automatizada de recuperación utiliza un emisor simulado; SMTP requiere configuración y prueba en cada entorno.

## Límites del simulador

Dinero ficticio; sin integración bancaria real, KYC real, tarjetas ni partida doble. No equivale a un core bancario productivo ni a una prueba de carga masiva. El límite de intentos y las sesiones son locales a una instancia. El historial no tiene paginación y los tokens requieren una política de limpieza si crece el uso. Para publicación pública hacen falta HTTPS, operación de secretos, backups, monitoreo y pruebas adicionales. La documentación está expuesta como consulta para portfolio y puede desactivarse mediante configuración.
