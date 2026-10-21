# Pruebas y validación

## Rápidas (Java 21)

```bat
mvnw.cmd test
```

Ejecuta tests unitarios y flujo HTTP con H2. No usa la base de desarrollo ni envía mails reales: el correo del test es simulado.

## Integración PostgreSQL

Usar una base VACÍA y EXCLUSIVA para pruebas: las pruebas insertan datos, y no deben apuntar a `banco_digital`. Por defecto `jdbc:postgresql://localhost:5433/banco_test`.

Con Docker Desktop, en PowerShell:

```powershell
$env:TEST_DB_PASSWORD = 'elegi-una-clave-solo-para-pruebas'
docker compose -f compose.test.yaml up -d --wait
.\mvnw.cmd verify
docker compose -f compose.test.yaml down
```

En CI esa contraseña es efímera de un servicio aislado, no una credencial real. Si no existe TEST_DB_PASSWORD, la clase PostgreSQL se marca como OMITIDA; eso no cuenta como validación de PostgreSQL.

Los siete casos PostgreSQL comprueban: esquema Flyway y restricción de saldo, mismo pedido concurrente, dos gastos que exceden el saldo, rollback por error al acreditar, propiedad de cuenta y conflicto por reutilización de clave y adopción de un esquema previo preservando sus datos. Los threads llaman al servicio proxificado: cada transferencia tiene su propia transacción. Hay límites de tiempo para detectar bloqueos.

## Frontend

```bat
npm install
npm run test:frontend
```

Los tests usan jsdom: no reemplazan una revisión visual en navegador y celular.

## CI

`.github/workflows/ci.yml` ejecuta Maven `verify` con Java 21 y PostgreSQL, y los tests de frontend en un job separado. Los reportes Java quedan como artifacts. El resultado de cada ejecución está disponible en la pestaña Actions del repositorio.
