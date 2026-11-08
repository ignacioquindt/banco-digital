# Demostración y decisiones técnicas para entrevistas

## Resumen del proyecto (Pitch de 30 segundos)

> *"Banco Digital es una API financiera desarrollada en Java 21 y Spring Boot con persistencia en PostgreSQL, diseñada bajo estándares de ingeniería de sistemas bancarios y fintechs de alta concurrencia. Implementa el ciclo completo de fondos (Cash-in, Cash-out y transferencias P2P), bloqueo pesimista ordenado para prevenir deadlocks, idempotencia basada en UUIDs, el patrón Transactional Outbox para entrega confiable de eventos sin dual-write, trazabilidad con Correlation ID en MDC, control de acceso basado en roles (RBAC) y migraciones de base de datos continuas con Flyway."*

---

## Recorrido de 5 minutos en una entrevista técnica

1. **Ciclo de Fondos y Experiencia de Usuario (1 min):**
   - Abrir el portal en `http://localhost:8080/index.html`.
   - Mostrar una cuenta y hacer clic en **"Cargar saldo"**: ingresar `$50.000 ARS`.
   - Explicar que la operación viaja al endpoint `POST /api/cuentas/{id}/depositar` con una clave de idempotencia única generada por el cliente y bloqueo pesimista en base de datos.
2. **Transferencias y Concurrencia (1.5 min):**
   - Abrir `TransferenciaService.java`.
   - Mostrar el bloqueo pesimista ordenado (`minId` / `maxId`) explicando cómo previene abrazos mortales (*deadlocks*) cuando dos usuarios se transfieren mutuamente en el mismo instante.
   - Explicar cómo la restricción de unicidad en base de datos protege contra débitos duplicados si se corta la red y el cliente reintenta la petición.
3. **El Patrón de Oro: Transactional Outbox (1.5 min):**
   - Abrir `OutboxService.java` y `OutboxPublisherJob.java`.
   - Explicar por qué llamar a Kafka o microservicios externos dentro de `@Transactional` es un antipatrón en finanzas (*dual-write problem*).
   - Mostrar cómo el evento se persiste atómicamente en la tabla `outbox_eventos` dentro de la misma transacción contable, y cómo el *scheduler* asíncrono lo despacha garantizando entrega *at-least-once*.
4. **Seguridad, Auditoría y Observabilidad (1 min):**
   - Mostrar `CorrelationIdFilter.java` y cómo el `X-Correlation-ID` viaja en el MDC de SLF4J, en los logs y en cada registro inmutable de la tabla `auditoria_operaciones`.
   - Mostrar los endpoints administrativos en `AdminController.java` (`ROLE_ADMIN`) y cómo se consultan las métricas globales y la auditoría.

---

## Preguntas técnicas típicas y respuestas recomendadas

| Pregunta de entrevista | Respuesta conceptual y defendible |
|---|---|
| **¿Qué es el *Transactional Outbox Pattern* y qué problema resuelve?** | En sistemas distribuidos, si debitás una cuenta y en el mismo método llamás a Kafka, podés sufrir una inconsistencia dual: si Kafka falla, tenés que hacer rollback; si la base confirma pero Kafka pierde conexión, el evento nunca se publicó. El patrón Outbox resuelve esto persistiendo el evento en una tabla de la misma base de datos dentro de la misma transacción ACID. Luego, un proceso independiente lee la tabla y lo envía al broker, garantizando entrega confiable (*at-least-once*). |
| **¿Cómo prevenís deadlocks en transferencias concurrentes?** | Si el usuario A transfiere a B y B a A simultáneamente, un bloqueo desordenado genera espera circular. Al ordenar los bloqueos por ID (`Math.min` y luego `Math.max`), todas las transacciones compiten por los recursos en el mismo orden lineal, eliminando la posibilidad de deadlock. |
| **¿Para qué sirve la clave de idempotencia?** | Si una app envía una transferencia o depósito y se corta la conexión antes de recibir la respuesta HTTP, el cliente reintenta. La base de datos garantiza mediante una restricción `UNIQUE` que no se vuelva a debitar; el backend detecta el reintento y devuelve la operación existente. |
| **¿Por qué usar `BigDecimal` en lugar de `double` o `float`?** | Los tipos flotantes binarios sufren errores de precisión por redondeo en operaciones repetidas. En sistemas bancarios, cada centavo debe ser exacto; `BigDecimal` ofrece precisión arbitraria y control estricto de escala. |
| **¿Por qué paginar con `Pageable`?** | Previene que una consulta de movimientos cargue miles de entidades en memoria de golpe, evitando saturar el garbage collector y provocar caídas por `OutOfMemoryError` en la JVM. |
| **¿Qué es un Correlation ID y por qué usar MDC?** | El filtro asigna o propaga un identificador único que se almacena en el `Mapped Diagnostic Context` (MDC) del hilo, logrando que cada línea de log incluya el mismo ID para rastrear el ciclo de vida completo de la petición en un sistema distribuido. |
