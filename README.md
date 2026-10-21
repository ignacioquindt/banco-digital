# Banco Digital

**Simulador de banca digital · Java, Spring Boot y PostgreSQL**

Aplicación full stack con backend Java/Spring Boot, PostgreSQL y frontend HTML, CSS y JavaScript. Permite registrarse, iniciar sesión, crear cuentas, transferir dinero ficticio y consultar movimientos. El objetivo es demostrar desarrollo de APIs, persistencia, seguridad y manejo de operaciones concurrentes.

## Funcionalidades

- Registro y login con sesión; contraseñas protegidas mediante BCrypt.
- Panel con saldo total en ARS y operaciones recientes.
- Creación de cuentas propias con saldo inicial cero.
- Transferencias con validación, confirmación y comprobante descargable.
- Idempotencia por UUID y restricción única en la base.
- Débito y crédito en una transacción; bloqueo pesimista de cuentas en orden de ID.
- Autorización por titular para cuentas, movimientos y cuenta de origen.
- Historial con filtros por cuenta, tipo y fecha; exportación CSV.
- Edición de nombre, cambio de contraseña e invalidación de sesiones.
- Recuperación de contraseña por SMTP, con token de un uso y vencimiento de 20 minutos.
- Protección CSRF y límite local de intentos por IP para rutas sensibles.
- Diseño adaptable; fechas en Buenos Aires y formato de 24 horas.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Java 21 objetivo, Spring Boot 4.1.1 |
| Persistencia | Spring Data JPA, Hibernate, PostgreSQL |
| Seguridad | Spring Security, BCrypt, sesiones y CSRF |
| Correo | Spring Mail / SMTP |
| Frontend | HTML, CSS y JavaScript; sin compilación frontend |
| Pruebas | JUnit, Mockito, H2, PostgreSQL, cliente HTTP y jsdom |
| Construcción | Maven Wrapper, Docker Compose, GitHub Actions |
| API y migraciones | Swagger/OpenAPI, Postman, Flyway |

## Ejecutar

Consultar la **[guía de instalación](LEEME_PRIMERO.md)**. Docker incluye el entorno Java y PostgreSQL. Para ejecución nativa se requiere JDK 21 y PostgreSQL.

### Base nueva con Docker

```sh
# Copiar .env.example a .env y completar DB_PASSWORD localmente.
docker compose up --build
```

Abrir http://localhost:8081/login.html y registrarse. Docker crea una base separada y Flyway crea el esquema; no incluye dinero ni usuarios de otras instalaciones.

### NetBeans / PostgreSQL instalado

JDK 21+, PostgreSQL y variable DB_PASSWORD. Para base vacía: crear solo la base y arrancar `mvnw.cmd spring-boot:run`. Flyway administra las tablas. Para conectar una base existente no vacía, seguir **[ACTUALIZAR.md](docs/ACTUALIZAR.md)** para adoptar el esquema con backup y baseline explícito.

### API

Las siguientes URLs corresponden a la ejecución nativa. Con Docker se utiliza el puerto 8081.

- Swagger de consulta: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs
- [Colección Postman y flujo sesión + CSRF](docs/POSTMAN.md).

### Verificación

- `mvnw.cmd test`: unitarias y flujo HTTP con H2, con base H2 aislada.
- `mvnw.cmd verify`: agrega integración PostgreSQL si TEST_DB_PASSWORD está definida y la base exclusiva de prueba está disponible.
- [Configuración de la base de prueba y CI](docs/PRUEBAS.md).
- `npm ci` y `npm run test:frontend`: comportamiento del panel con jsdom.

### Organización del backend

| Paquete | Responsabilidad |
|---|---|
| cuentas | Entidad, repositorio, entrada y endpoints de cuentas |
| transferencias | Validación y procesamiento transaccional de movimientos |
| usuarios | Registro, perfil y persistencia de usuarios |
| seguridad | Autenticación, usuario actual, sesiones, CSRF y límite de solicitudes |
| recuperacion | Tokens y recuperación por SMTP |
| comun | Errores, navegación, estado y OpenAPI |

La clase principal permanece en `com.ignacio.banco` para escanear los subpaquetes.

Node.js es opcional: solo se usa para la prueba automatizada de frontend. Las credenciales SMTP también son opcionales para arrancar; se requieren para enviar recuperación por correo.

## Evidencia y presentación

- **[Validación](docs/VALIDACION.md):** resultados registrados, alcance de las pruebas y verificaciones pendientes.
- **[Demostración y decisiones técnicas](docs/DEMO_Y_ENTREVISTA.md)**.
- **[Arquitectura y endpoints](docs/ARQUITECTURA_Y_API.md)**.
- **[Configuración de Windows](docs/CONFIGURACION_WINDOWS.md)**.
- **[Mapa de clases](docs/MAPA_DE_CLASES.md)**.

## Alcance

Es un simulador educativo con dinero ficticio. La cuenta de destino se identifica por su ID interno, no por CBU o alias real. No integra bancos ni procesa fondos reales. No se presenta como software bancario listo para producción.

La revisión móvil se hizo con emulación de Chrome y capturas, no con un teléfono físico. Las pruebas concurrentes cubren los escenarios documentados; no equivalen a una prueba de carga masiva. El correo SMTP funcionó con Gmail en el entorno local del autor, pero las credenciales no se distribuyen.

Para un despliegue público quedan tareas de operación: HTTPS, secretos, copias de seguridad, observabilidad, paginación, mantenimiento de tokens, límites distribuidos y pruebas de carga. Ver los límites concretos en `docs/VALIDACION.md`.
