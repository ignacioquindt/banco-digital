# Configuración en Windows y NetBeans

## Requisitos

JDK 21 y PostgreSQL. Abrir en NetBeans la carpeta que contiene `pom.xml`. La clase principal es `com.ignacio.banco.BancoDigitalApplication`.

## Variables de entorno

| Variable | Uso |
|---|---|
| `DB_URL` | URL JDBC; valor predeterminado `jdbc:postgresql://localhost:5432/banco_digital` |
| `DB_USERNAME` | Usuario PostgreSQL; predeterminado `postgres` |
| `DB_PASSWORD` | Contraseña PostgreSQL; obligatoria |
| `PORT` | Puerto HTTP; predeterminado 8080 en ejecución nativa |
| `MAIL_ENABLED` | Habilita recuperación por correo |
| `MAIL_HOST`, `MAIL_PORT` | Servidor y puerto SMTP |
| `MAIL_AUTH`, `MAIL_STARTTLS` | Autenticación y STARTTLS |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | Credenciales SMTP |
| `MAIL_FROM` | Remitente |
| `APP_BASE_URL` | URL pública utilizada en los enlaces de recuperación |

Definir las variables antes de abrir NetBeans. Si se modifican, reiniciar el IDE para actualizar su entorno. La ejecución nativa no carga `.env` automáticamente; Docker Compose sí utiliza ese archivo.

Para Gmail se utiliza `smtp.gmail.com`, puerto 587, autenticación y STARTTLS activos y una contraseña de aplicación. No almacenar credenciales en el código ni en archivos versionados.

## Ejecución

Crear la base vacía y ejecutar el proyecto desde NetBeans o `mvnw.cmd spring-boot:run`. Flyway administra las tablas. Para una base existente, consultar [Migraciones](ACTUALIZAR.md).

## Diagnóstico

| Mensaje | Comprobación |
|---|---|
| Puerto ocupado | Detener la instancia previa o seleccionar otro puerto |
| Conexión rechazada | Comprobar el servicio PostgreSQL, host y puerto |
| Tabla ausente | Revisar los logs de Flyway y la base seleccionada |
| Correo no configurado | Revisar las variables SMTP recibidas por el proceso |
| CSRF rechazado tras reinicio | Recargar la página e iniciar sesión nuevamente |

Las variables de usuario de Windows no sustituyen un gestor de secretos para un despliegue público.
