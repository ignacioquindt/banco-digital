# Probar la API con Postman

Importar `postman/BancoDigital.postman_collection.json`. Mantener activo el cookie jar.
Las escrituras obtienen un token CSRF automáticamente; no se desactiva la protección.

1. Ejecutar la app y configurar `baseUrl`, `email` y `password` como valores locales/privados en Postman. Contraseña de 12 a 64 caracteres (hasta 72 bytes UTF-8).
2. Con email nuevo ejecutar 01 a 10 en orden. Si el usuario ya existe omitir 02. El login usa formulario y conserva la cookie; no sigue redirecciones para comprobar el 302.
3. El pedido 05 guarda `origenId`. Crear una segunda cuenta (pantalla o API), anotar su ID en `destinoId`. No repetir 05 sin necesidad: crea una cuenta nueva.
4. Las cuentas comienzan en cero. Solo para el simulador local, desde pgAdmin sobre la base de desarrollo, acreditar explícitamente la cuenta de prueba:

```sql
-- Reemplazar 123 por el ID real de la cuenta de prueba; NO ejecutar sobre dinero real.
UPDATE cuentas SET saldo = 100.00 WHERE id = 123;
```

5. Ejecutar 11: debe transferir 1 peso. Ejecutar 12: debe devolver el mismo ID sin nuevo débito. 13: debe responder 409 y no mover dinero. 14: debe responder 400.
6. 15 requiere SMTP configurado. Si está deshabilitado la respuesta prevista es 503; omitirlo en ese caso.
7. 16 cierra sesión y 17 comprueba 401.

No ejecutar toda la colección ciegamente: registro, creación y transferencia producen cambios. Para una NUEVA transferencia borrar la variable `claveIdempotencia`; para reintentar conservarla. No compartir exports con contraseñas o cookies.
Swagger en `/swagger-ui.html` es de consulta. Las llamadas modificadoras se ejercitan con esta colección y con los tests automatizados. Login/logout son filtros de Spring Security y se documentan aquí; no son métodos de un controlador REST.
