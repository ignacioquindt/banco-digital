# Migraciones y adopción de una base existente

## Preparación

Antes de incorporar Flyway a una base existente, realizar y verificar una copia de seguridad. La configuración de base de datos y SMTP se mantiene en variables de entorno.

## Base PostgreSQL ya existente

No borrar ni recrear tablas. La primera ejecución con Flyway sobre una base no vacía sin historial se detiene deliberadamente para evitar adoptar una base equivocada.

1. Comprobar en `application.properties` o `DB_URL` que la conexión apunta a `banco_digital` y verificar el backup.
2. Abrir CMD en la carpeta del proyecto que contiene `pom.xml`. Definir `DB_PASSWORD` con la contraseña local si todavía no está en el entorno. No copiar contraseñas al código.
3. Ejecutar `mvnw.cmd clean test` para compilar desde cero y comprobar las pruebas rápidas. Después ejecutar UNA VEZ:

```bat
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.flyway.baseline-on-migrate=true --spring.flyway.baseline-version=0"
```

La versión 0 registra el punto de partida y V1 aplica de forma idempotente el script que crea/completa el esquema. Hibernate valida el resultado. Si existe ya un historial Flyway, no editarlo ni borrar filas: revisar el error antes de continuar.

4. Detener con Ctrl+C. Las siguientes veces ejecutar normalmente desde NetBeans o `mvnw.cmd spring-boot:run`, sin esas opciones. No dejar baseline-on-migrate habilitado permanentemente.
5. Revisar login, cuentas, movimientos, nombre y recuperación de contraseña. Los datos previos siguen en la misma base.

## Base nueva

Crear solo la base vacía en PostgreSQL; Flyway crea sus tablas al arrancar. No ejecutar antes el SQL manual. `sql/01_actualizar_esquema.sql` se conserva como referencia histórica, no como procedimiento normal nuevo.

## NetBeans

Abrir la carpeta del proyecto con Open Project. La clase principal es `com.ignacio.banco.BancoDigitalApplication`. Los paquetes son `cuentas`, `transferencias`, `usuarios`, `seguridad`, `recuperacion`, `comun`. JDK 21 o superior, objetivo Java 21. Ejecutar Test Project para pruebas rápidas; las pruebas PostgreSQL adicionales usan Maven `verify` y una base aparte (ver PRUEBAS.md).

## Docker opcional

Instalar/iniciar Docker Desktop. Copiar `.env.example` a `.env`, definir una clave local y ejecutar `docker compose up --build`. El puerto publicado se define en `compose.yaml` (8081). Mantener `APP_BASE_URL` alineada con la URL de acceso.

Docker usa una base NUEVA y un volumen propios; no importa automáticamente los datos de PostgreSQL de Windows. Al principio no hay usuarios: registrarse. `docker compose down` conserva los datos del volumen. No usar `down -v` si querés conservarlos. La app se publica solo en loopback y es un simulador local.
