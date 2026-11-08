# Arquitectura y API

## Responsabilidades y Capas

| Componente | Responsabilidad |
|---|---|
| Frontend (HTML/JS / React) | Presentación, formularios y consumo de la API REST |
| CorrelationIdFilter | Asignación y propagación del header `X-Correlation-ID` en MDC y respuestas |
| SecurityConfig & RBAC | Autorización declarativa por roles (`ROLE_CLIENTE`, `ROLE_ADMIN`, `ROLE_AUDITOR`) con `@EnableMethodSecurity` |
| Controllers | Validación de entrada, orquestación y mapeo a DTOs (Java Records) |
| Services | Lógica de negocio (transferencias, cuentas, depósitos/extracciones, perfil, administración) |
| Transactional Outbox | Persistencia atómica de eventos en `outbox_eventos` y despacho desacoplado (`OutboxPublisherJob`) |
| Domain Events & Listeners | Desacoplamiento transaccional (`AFTER_COMMIT`) para auditoría y notificaciones |
| Repositories | Consultas JPA, paginación (`Pageable`), agregación y bloqueos pesimistas ordenados |
| Entities | Modelo de persistencia relacional con validaciones y restricciones de integridad |
| Flyway | Migraciones incrementales del esquema de base de datos (`V1`, `V2`, `V3`) |
| PostgreSQL | Motor de base de datos transaccional con restricciones de unicidad y cheques |

---

## Endpoints del Sistema

### Rutas de Cuentas y Fondos (Cash-In y Cash-Out)
| Método | Ruta | Acceso | Propósito |
|---|---|---|---|
| GET | `/api/cuentas` | Autenticado | Listar cuentas bancarias propias (`List<CuentaResponse>`) |
| GET | `/api/cuentas/{id}` | Autenticado (titular) | Detalle de cuenta propia (`CuentaResponse`) |
| POST | `/api/cuentas` | Autenticado | Creación de cuenta propia con saldo inicial cero |
| POST | `/api/cuentas/{id}/depositar` | Autenticado (titular) | **Ingresar dinero (Cash-In)** con idempotencia, bloqueo pesimista y outbox |
| POST | `/api/cuentas/{id}/extraer` | Autenticado (titular) | **Extraer dinero (Cash-Out)** validando saldo disponible |
| GET | `/api/cuentas/{id}/operaciones` | Autenticado (titular) | Historial de depósitos y extracciones de la cuenta |
| GET | `/api/cuentas/{id}/movimientos` | Autenticado (titular) | Movimientos de transferencias (formato lista) |
| GET | `/api/cuentas/{id}/movimientos/paginado` | Autenticado (titular) | Movimientos de transferencias con paginación empresarial (`Page<TransferenciaResponse>`) |

### Transferencias y Paginación
| Método | Ruta | Acceso | Propósito |
|---|---|---|---|
| GET | `/api/transferencias` | Autenticado | Lista completa de transferencias propias |
| GET | `/api/transferencias/paginado` | Autenticado | Historial paginado (`Page<TransferenciaResponse>`) con `page`, `size` y `sort` |
| POST | `/api/transferencias` | Autenticado (origen propio) | Ejecutar transferencia atómica con bloqueo pesimista ordenado, idempotencia y outbox |

### Rutas Administrativas y Auditoría (ROLE_ADMIN / ROLE_AUDITOR)
| Método | Ruta | Acceso | Propósito |
|---|---|---|---|
| GET | `/api/admin/usuarios` | `ROLE_ADMIN` | Listar todos los usuarios del sistema, sus roles y estado |
| PATCH | `/api/admin/usuarios/{id}/rol` | `ROLE_ADMIN` | Modificar rol (`ROLE_CLIENTE`, `ROLE_ADMIN`, `ROLE_AUDITOR`) |
| PATCH | `/api/admin/usuarios/{id}/estado` | `ROLE_ADMIN` | Activar o desactivar cuenta de usuario |
| GET | `/api/admin/metricas` | `ROLE_ADMIN` o `ROLE_AUDITOR` | Métricas globales (total usuarios, cuentas, saldo total circulante y transferencias) |
| GET | `/api/admin/auditoria` | `ROLE_ADMIN` o `ROLE_AUDITOR` | Consultar los últimos 100 eventos de auditoría registrados con correlation ID |

---

## Patrones de Arquitectura Clave

### 1. Transactional Outbox Pattern (Garantía de Entrega Sin Dual-Write)
En arquitecturas distribuidas y fintechs (Mercado Pago, Stripe), disparar una llamada a Kafka o a una API externa dentro de una transacción `@Transactional` genera riesgo de inconsistencia (si la base confirma pero la red falla, el mensaje se pierde).
El patrón implementado funciona así:
1. Dentro de la transacción del depósito o transferencia, se guarda el movimiento contable y un registro en la tabla `outbox_eventos` con estado `PENDIENTE`.
2. Ambos confirman o revierten juntos (atomicidad ACID).
3. El proceso en segundo plano `OutboxPublisherJob` consulta los eventos pendientes cada 5 segundos, los despacha hacia el bus/topic de streaming y los marca como `PROCESADO`.

### 2. Bloqueo Pesimista Ordenado (Deadlock Prevention)
Al transferir entre dos cuentas, se ordenan sus IDs (`minId` y `maxId`) para que cualquier transacción concurrente siempre adquiera los bloqueos en el mismo orden, eliminando el riesgo de abrazo mortal (*deadlock*).

### 3. Paginación y Eficiencia de Memoria
Se implementó `Pageable` para permitir consultar movimientos en bloques controlados (ej. `GET /api/cuentas/1/movimientos/paginado?page=0&size=10&sort=fecha,desc`), protegiendo a la JVM de sobrecargas de memoria ante cuentas con miles de movimientos.
