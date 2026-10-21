# Banco Digital — Inicio rápido

Simulador de banca digital con Java, Spring Boot y PostgreSQL. Todas las operaciones utilizan dinero ficticio.

## Docker

1. Instalar e iniciar Docker Desktop.
2. Copiar `.env.example` a `.env` y definir `DB_PASSWORD`.
3. Ejecutar `docker compose up --build -d` desde la raíz del proyecto.
4. Acceder a http://localhost:8081 y registrar un usuario.

El puerto publicado se define en `compose.yaml`. `APP_BASE_URL` debe coincidir con la URL de acceso. La base se conserva en un volumen; `docker compose down` no elimina sus datos.

## Ejecución sin Docker

Requiere JDK 21 y PostgreSQL. Configurar `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`, crear una base vacía y ejecutar `mvnw.cmd spring-boot:run`. El puerto predeterminado es 8080.

Para adoptar una base existente, consultar [Migraciones](docs/ACTUALIZAR.md).

## Configuración y documentación

Las credenciales se mantienen fuera del repositorio. `.env.example` contiene únicamente valores de referencia. El envío SMTP es opcional.

- [Descripción y arquitectura](README.md).
- [Variables y NetBeans](docs/CONFIGURACION_WINDOWS.md).
- [Pruebas](docs/PRUEBAS.md).
- [API y Postman](docs/POSTMAN.md).
