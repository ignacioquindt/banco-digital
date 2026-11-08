# Banco Digital

**API bancaria y simulador transaccional de nivel empresarial · Java 21, Spring Boot y PostgreSQL**

Aplicación financiera orientada a servicios transaccionales de alta concurrencia, construida bajo estándares de la industria bancaria y fintech (Mercado Pago, Ualá, banca corporativa). Implementa el ciclo completo de fondos (Cash-in, Cash-out y transferencias P2P), manejo de concurrencia con bloqueo pesimista ordenado, idempotencia estricta por UUID, el patrón **Transactional Outbox** para entrega confiable de eventos, desacoplamiento por eventos de dominio (`AFTER_COMMIT`), trazabilidad mediante Correlation ID (MDC), control de acceso basado en roles (RBAC), paginación con `Pageable` y migraciones de esquema continuas con Flyway.

---

## Capacidades y Arquitectura

- **Ciclo Completo de Fondos:** Endpoints de depósito (**Cash-in**) y extracción (**Cash-out**) con snapshot de saldo posterior y validaciones de escala monetaria en `BigDecimal`.
- **Transactional Outbox Pattern:** Persistencia atómica de eventos en la tabla `outbox_eventos` dentro de la misma transacción contable, despachados de forma desacoplada y asíncrona hacia brokers de mensajería (Kafka/RabbitMQ) sin riesgo de inconsistencia dual (*dual-write problem*).
- **Prevención de Deadlocks:** Bloqueo pesimista ordenado (`PESSIMISTIC_WRITE`) sobre las cuentas involucradas (`minId` y `maxId`) para mitigar bloqueos mutuos ante transferencias simultáneas.
- **Idempotencia Garantizada:** Clave de idempotencia única (UUID) para evitar duplicación de movimientos ante cortes de red y reintentos automáticos del cliente.
- **Seguridad y Control de Acceso (RBAC):** Autorización granular por roles (`ROLE_CLIENTE`, `ROLE_ADMIN`, `ROLE_AUDITOR`) con `@EnableMethodSecurity`.
- **Auditoría Transaccional:** Registro inmutable e independiente (`REQUIRES_NEW`) de cada operación bancaria y evento administrativo.
- **Trazabilidad y Observabilidad:** Inyección de `X-Correlation-ID` en el MDC de SLF4J a través de `CorrelationIdFilter` para seguimiento extremo a extremo en logs.
- **Paginación Empresarial:** Consultas paginadas mediante `Pageable` de Spring Data (`page`, `size`, `sort`) para proteger el uso de memoria de la JVM.
- **Evolución de Base de Datos:** Versionado declarativo e incremental mediante Flyway (`V1__esquema_inicial.sql`, `V2__roles_y_auditoria.sql`, `V3__outbox_y_operaciones.sql`).
- **Pruebas Automatizadas:** 31 pruebas unitarias y de integración con JUnit 5 y Mockito.

---

## Tecnologías

| Capa / Módulo | Tecnología |
|---|---|
| **Backend** | Java 21, Spring Boot 4.1.1 |
| **Persistencia** | Spring Data JPA, Hibernate, PostgreSQL |
| **Migraciones** | Flyway DB (V1, V2, V3) |
| **Seguridad** | Spring Security, BCrypt, RBAC, protección CSRF y rate limiting |
| **Patrón Outbox** | Transactional Outbox Pattern con `@Scheduled` dispatcher |
| **Eventos** | Spring ApplicationEventPublisher / Kafka ready |
| **Observabilidad** | SLF4J, Logback con MDC (Correlation ID) |
| **Documentación** | Swagger / OpenAPI 3, colección Postman |
| **Frontend** | HTML5, CSS3, JavaScript modular con botones de Cash-in / Cash-out |
| **DevOps** | Docker Compose (PostgreSQL y Kafka 3.8 con KRaft), GitHub Actions |

---

## Ejecución Local

### NetBeans / CLI (PostgreSQL local)

Requisitos: JDK 21+ y PostgreSQL local con base `banco_digital`.

```sh
# Ejecutar suite completa de pruebas unitarias (31 tests)
mvnw.cmd test

# Iniciar la aplicación
mvnw.cmd spring-boot:run
```

- Portal web: http://localhost:8080/index.html
- Swagger UI: http://localhost:8080/swagger-ui.html

---

## Organización del Backend

```
src/main/java/com/ignacio/banco/
├── admin/            # Endpoints y DTOs administrativos (usuarios, roles, auditoría, métricas)
├── auditoria/        # Entidad, repositorio y servicio de auditoría transaccional
├── comun/            # Filtro CorrelationId, manejador global de excepciones, OpenAPI
├── cuentas/          # Entidad Cuenta, DTOs, CuentaService (Cash-in/Cash-out) y controlador
├── eventos/          # Eventos de dominio y oyentes transaccionales (AFTER_COMMIT)
├── outbox/           # Transactional Outbox Pattern (Entidad, repositorio, servicio y scheduler)
├── recuperacion/     # Tokens temporales y servicio SMTP de recuperación
├── seguridad/        # Configuración de Spring Security, RBAC, UserDetails y CSRF
├── transferencias/   # Servicio transaccional, bloqueos, idempotencia y controlador
└── usuarios/         # Entidad Usuario, enum Rol y registro
```

---

## Documentación y Guías de Entrevista

- **[Demostración y decisiones técnicas para entrevistas](docs/DEMO_Y_ENTREVISTA.md):** Pitch de 30 segundos, recorrido guiado y respuestas a preguntas técnicas de selección.
- **[Arquitectura y especificación de endpoints](docs/ARQUITECTURA_Y_API.md):** Diagramas, modelo por capas y catálogo completo de rutas.
- **[Mapa de clases y responsabilidades](docs/MAPA_DE_CLASES.md):** Detalle técnico de cada componente del sistema.
