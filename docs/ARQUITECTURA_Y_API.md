# Arquitectura y API

## Responsabilidades

| Componente | Responsabilidad |
|---|---|
| HTML/CSS/JavaScript | Presentación, formularios y solicitudes fetch |
| SecurityConfig | Acceso público/autenticado, login, sesiones y CSRF |
| UsuarioActualService | Identificar al usuario desde la autenticación del servidor |
| Controllers | Recibir datos válidos y devolver respuestas HTTP |
| Services | Reglas de registro, transferencia, perfil y recuperación |
| Repositories | Consultas JPA y bloqueos |
| Entities | Usuarios, cuentas, transferencias y tokens persistidos |
| PostgreSQL | Persistencia, integridad referencial y unicidad |

Las cuentas se vinculan al usuario por usuario_id. Cada transferencia guarda origen_id y destino_id. El token de recuperación vincula usuario_id, vencimiento, estado de uso y versión de credenciales.

## Endpoints

| Método | Ruta | Acceso y propósito |
|---|---|---|
| GET | /api/estado | Público: estado |
| GET | /api/auth/csrf | Público: token vinculado a sesión |
| POST | /api/auth/registro | Público con CSRF: registro |
| POST | /login | Formulario con CSRF: autenticar |
| POST | /logout | CSRF: cerrar sesión |
| POST | /api/auth/recuperar | Público con CSRF: solicitar enlace SMTP |
| POST | /api/auth/restablecer | Público con CSRF: consumir token |
| GET | /api/cuentas | Cuentas del usuario autenticado |
| GET | /api/cuentas/{id} | Cuenta propia; 404 si no está disponible para ese usuario |
| POST | /api/cuentas | Crear cuenta para el usuario autenticado |
| GET | /api/cuentas/{id}/movimientos | Movimientos de cuenta propia |
| GET | /api/transferencias | Operaciones que involucran cuentas propias |
| POST | /api/transferencias | Transferir desde cuenta propia |
| GET | /api/perfil | ID, nombre y correo propios |
| PUT | /api/perfil | Actualizar nombre |
| POST | /api/perfil/password | Cambiar contraseña comprobando la actual |

Todas las operaciones que cambian datos usan CSRF. Las rutas sensibles tienen un límite local por IP. Las credenciales del formulario se llaman username y password; username contiene el correo.

## Transferencia

El controlador obtiene el usuario de la sesión. El servicio valida el importe y los IDs, bloquea las dos cuentas en orden ascendente, comprueba titularidad del origen y consulta la clave de idempotencia. Si ya existe, compara los datos; si coinciden devuelve la anterior y si no responde 409. Si no existe, debita, acredita y registra en la misma transacción.

El frontend conserva el pedido pendiente en sessionStorage de esa pestaña antes de enviarlo. Tras un fallo de red reintenta con la misma clave. Si se cierra esa pestaña puede perderse ese estado: consultar movimientos antes de repetir manualmente una operación de resultado incierto.

## Recuperación

La aplicación genera 32 bytes aleatorios, envía el token en el fragmento del enlace y guarda solamente su hash. Al usarlo, bloquea el token y el usuario, verifica vencimiento/uso/versión, cambia el hash de la contraseña y consume el token. Cambiar la contraseña incrementa la versión de credenciales e invalida enlaces anteriores. Expira las sesiones registradas en esta instancia.
