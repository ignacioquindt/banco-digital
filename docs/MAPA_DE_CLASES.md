# Mapa de clases

Responsabilidades de las clases del backend, agrupadas por funcionalidad.

| Clase / paquete | Responsabilidad |
|---|---|
| `BancoDigitalApplication` (banco) | Punto de entrada; inicia Spring Boot y el servidor web. |
| `ApiExceptionHandler` (comun) | Respuestas JSON coherentes para validaciones y errores previstos. |
| `EstadoController` (comun) | Respuesta simple de disponibilidad del endpoint. |
| `NavegacionController` (comun) | Redirecciones a las páginas HTML. |
| `OpenApiConfig` (comun) | Metadatos y esquema de sesión para la documentación de la API. |
| `CrearCuentaRequest` (cuentas) | Datos y validaciones de creación de cuenta. |
| `Cuenta` (cuentas) | Estado y reglas de una cuenta: saldo, dueño, débito, crédito y tieneSaldo(). |
| `CuentaController` (cuentas) | Endpoints y autorización para consultar/crear cuentas propias y movimientos. |
| `CuentaRepository` (cuentas) | Consultas de cuentas y bloqueo pesimista para transferencias. |
| `RecuperacionController` (recuperacion) | Endpoints de solicitud y restablecimiento de contraseña. |
| `RecuperacionRepository` (recuperacion) | Consulta y bloqueo del token durante su consumo. |
| `RecuperacionService` (recuperacion) | Genera secretos aleatorios, envía SMTP y consume tokens. |
| `RecuperacionToken` (recuperacion) | Hash del token, vencimiento, uso y versión de credenciales. |
| `CsrfController` (seguridad) | Entrega el token CSRF asociado al cliente. |
| `LimiteSolicitudesFilter` (seguridad) | Límite local en memoria por IP para rutas sensibles. |
| `SecurityConfig` (seguridad) | Rutas públicas/privadas, login, logout, BCrypt y registro de sesiones. |
| `UsuarioActualService` (seguridad) | Obtiene el usuario autenticado para autorizar una operación. |
| `UsuarioDetailsService` (seguridad) | Carga los datos del usuario para Spring Security. |
| `Transferencia` (transferencias) | Registro persistente del movimiento, fecha y clave de idempotencia. |
| `TransferenciaController` (transferencias) | Entrada HTTP y consulta de transferencias propias. |
| `TransferenciaRepository` (transferencias) | Persistencia y consultas por usuario, cuenta y clave. |
| `TransferenciaRequest` (transferencias) | Datos validados de origen, destino, importe y UUID. |
| `TransferenciaService` (transferencias) | Transacción, bloqueos ordenados, propiedad, idempotencia y saldos. |
| `AuthController` (usuarios) | Endpoint de registro y respuesta sin contraseña. |
| `PerfilController` (usuarios) | Endpoints de consulta de perfil, nombre y cambio de contraseña. |
| `PerfilService` (usuarios) | Reglas del perfil y expiración de sesiones tras confirmar el cambio. |
| `RegistroRequest` (usuarios) | Datos validados del registro. |
| `RegistroService` (usuarios) | Normaliza el email, evita duplicados y codifica la contraseña. |
| `Usuario` (usuarios) | Nombre, email, hash BCrypt, estado y versión de credenciales. |
| `UsuarioRepository` (usuarios) | Consultas de usuario y bloqueo para modificar credenciales. |
