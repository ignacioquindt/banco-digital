# Mapa de clases

Responsabilidades de las clases del backend, agrupadas por funcionalidad y paquete.

| Clase / paquete | Responsabilidad |
|---|---|
| `BancoDigitalApplication` (banco) | Punto de entrada con `@EnableScheduling`; inicia Spring Boot y tareas en segundo plano. |
| `AdminController` (admin) | Endpoints administrativos con RBAC (`ROLE_ADMIN`, `ROLE_AUDITOR`) para usuarios, roles, métricas globales y auditoría. |
| `AdminUsuarioResponse` (admin) | DTO seguro para visualización de usuarios en panel de control. |
| `MetricasSistemaResponse` (admin) | DTO con métricas globales del sistema financiero (usuarios, cuentas, circulante ARS y transferencias). |
| `AuditoriaOperacion` (auditoria) | Entidad JPA inmutable para el registro de auditoría y cumplimiento financiero. |
| `AuditoriaRepository` (auditoria) | Consultas de eventos de auditoría ordenados por fecha y correlation ID. |
| `AuditoriaService` (auditoria) | Registro transaccional independiente (`REQUIRES_NEW`) de eventos del sistema. |
| `AuditoriaResponse` (auditoria) | DTO de exposición de eventos de auditoría para operadores y auditores. |
| `CorrelationIdFilter` (comun) | Filtro HTTP prioritario que inyecta y propaga `X-Correlation-ID` en MDC (SLF4J) y cabeceras de respuesta. |
| `ApiExceptionHandler` (comun) | Respuestas JSON coherentes para validaciones y errores de negocio. |
| `EstadoController` (comun) | Healthcheck simple de disponibilidad del servicio. |
| `NavegacionController` (comun) | Redirecciones a las páginas HTML del portal. |
| `OpenApiConfig` (comun) | Metadatos y esquema de seguridad para Swagger UI. |
| `Cuenta` (cuentas) | Entidad de cuenta bancaria: saldo, moneda, titular, débito y crédito con `BigDecimal`. |
| `CuentaController` (cuentas) | Endpoints REST para creación, depósito (Cash-in), extracción (Cash-out), movimientos y paginación. |
| `CuentaService` (cuentas) | Servicio transaccional con bloqueos pesimistas, control de titularidad, depósitos y extracciones. |
| `CuentaResponse` (cuentas) | Record inmutable para desacoplar la entidad `Cuenta` de la capa web. |
| `CrearCuentaRequest` (cuentas) | Validación de datos para apertura de cuentas. |
| `OperacionCuenta` (cuentas) | Entidad de persistencia para depósitos y extracciones con saldo posterior e idempotencia. |
| `OperacionCuentaRepository` (cuentas) | Repositorio JPA para operaciones de caja y verificación de idempotencia. |
| `OperacionRequest` (cuentas) | DTO de solicitud de depósito/extracción con importe y UUID de idempotencia. |
| `OperacionResponse` (cuentas) | DTO de respuesta con detalles de depósito/extracción y saldo resultante. |
| `CuentaRepository` (cuentas) | Consultas JPA, agregación de saldos y bloqueo pesimista (`PESSIMISTIC_WRITE`). |
| `TransferenciaRealizadaEvent` (eventos) | Evento de dominio inmutable disparado tras completar una transferencia. |
| `AuditoriaEventListener` (eventos) | Escucha transaccional (`AFTER_COMMIT`) para registrar auditoría sin acoplar el servicio principal. |
| `NotificacionEventListener` (eventos) | Escucha asíncrona para simulación de alertas o downstream messaging. |
| `OutboxEvento` (outbox) | Entidad JPA para el patrón Transactional Outbox (estado `PENDIENTE`/`PROCESADO`). |
| `OutboxRepository` (outbox) | Consultas de eventos pendientes ordenados cronológicamente. |
| `OutboxService` (outbox) | Persiste eventos de dominio en la misma transacción atómica de la base de datos. |
| `OutboxPublisherJob` (outbox) | Tarea programada (`@Scheduled`) que despacha eventos pendientes a Kafka/mensajería. |
| `RecuperacionController` (recuperacion) | Endpoints de solicitud y restablecimiento de contraseña. |
| `RecuperacionRepository` (recuperacion) | Consulta y bloqueo del token durante su consumo. |
| `RecuperacionService` (recuperacion) | Genera secretos criptográficos, envía SMTP y consume tokens. |
| `RecuperacionToken` (recuperacion) | Hash SHA-256 del token, vencimiento, uso y versión de credenciales. |
| `CsrfController` (seguridad) | Entrega el token CSRF asociado al cliente. |
| `LimiteSolicitudesFilter` (seguridad) | Rate limiting en memoria por IP para rutas sensibles. |
| `SecurityConfig` (seguridad) | Rutas públicas/privadas, `@EnableMethodSecurity`, login, logout y sesiones. |
| `UsuarioActualService` (seguridad) | Identificación del usuario autenticado para autorización por titular. |
| `UsuarioDetailsService` (seguridad) | Carga de usuario y asignación de autoridades/roles (`Rol`) para Spring Security. |
| `Transferencia` (transferencias) | Entidad persistente de movimiento, fecha, importes y clave de idempotencia. |
| `TransferenciaController` (transferencias) | Endpoints REST de transferencias, consultas tradicionales y paginadas. |
| `TransferenciaResponse` (transferencias) | Record inmutable para la respuesta de movimientos bancarios. |
| `TransferenciaRepository` (transferencias) | Consultas de transferencias simples y paginadas (`Pageable`). |
| `TransferenciaRequest` (transferencias) | Datos validados de origen, destino, importe y UUID. |
| `TransferenciaService` (transferencias) | Bloqueo pesimista ordenado, validación de saldos, idempotencia, outbox y publicación de eventos. |
| `AuthController` (usuarios) | Endpoint de registro de usuarios. |
| `PerfilController` (usuarios) | Endpoints de perfil, nombre y cambio de contraseña. |
| `PerfilService` (usuarios) | Reglas de perfil y expiración de sesiones. |
| `RegistroRequest` (usuarios) | Validación de credenciales y datos de registro. |
| `RegistroService` (usuarios) | Normalización de email, validación de unicidad y hashing BCrypt. |
| `Rol` (usuarios) | Enum con los perfiles del sistema: `ROLE_CLIENTE`, `ROLE_ADMIN`, `ROLE_AUDITOR`. |
| `Usuario` (usuarios) | Entidad de usuario con credenciales, estado activo y rol asignado. |
| `UsuarioRepository` (usuarios) | Consultas y bloqueos de usuario para modificación de credenciales. |
