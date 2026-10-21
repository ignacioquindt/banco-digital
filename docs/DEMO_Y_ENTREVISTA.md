# Demostración y decisiones técnicas

## Descripción breve

Banco Digital es un simulador de banca desarrollado con Java, Spring Boot y PostgreSQL, con interfaz HTML/CSS/JavaScript. Permite gestionar cuentas y transferencias ficticias con autenticación, autorización por titular, transacciones e idempotencia. Incluye recuperación por correo y pruebas unitarias y de integración.

## Recorrido de 4 minutos

1. **Acceso:** mostrar login y registro propios. Entrar con un usuario de demostración, sin exponer claves ni configuración SMTP.
2. **Panel:** mostrar saldos y últimas operaciones. Explicar que los datos vienen de PostgreSQL.
3. **Cuenta:** crear una cuenta con saldo cero y mostrar su ID interno.
4. **Transferencia:** enviar un importe pequeño desde una cuenta propia con saldo; revisar antes de confirmar. Mostrar comprobante y actualización de saldos.
5. **Movimientos:** filtrar por cuenta, mostrar una operación y exportar CSV.
6. **Seguridad:** entrar con otro usuario para demostrar que no ve las cuentas ajenas. No publicar credenciales personales.
7. **Perfil y recuperación:** mostrar edición del nombre y explicar los tokens de un uso. Si hacés una recuperación en vivo, no grabes la URL con token ni el contenido privado de Gmail.
8. **Pruebas:** mostrar el resumen de la suite y los resultados documentados de concurrencia, sin decir que es una prueba de carga masiva.

## Decisiones técnicas

| Pregunta | Idea a explicar |
|---|---|
| ¿Por qué BigDecimal? | Los cálculos monetarios del servidor necesitan decimales exactos. |
| ¿Qué garantiza @Transactional? | Débito, crédito y registro confirman juntos o se revierten ante la excepción. |
| ¿Para qué bloquear cuentas? | Dos operaciones no deben decidir sobre el mismo saldo desactualizado. |
| ¿Por qué ordenar los bloqueos? | Todos solicitan las cuentas en el mismo orden de ID para reducir riesgo de deadlock. |
| ¿Qué hace la idempotencia? | La repetición con la misma clave y datos devuelve la operación existente sin volver a debitar. |
| ¿La interfaz es la barrera de seguridad? | No: el backend identifica al usuario autenticado y comprueba la propiedad. |
| ¿Qué guarda de la contraseña? | Su hash BCrypt. El hash del token de recuperación se guarda con SHA-256, porque el token ya es aleatorio y de alta entropía. |
| ¿Cómo sale un correo desde localhost? | La aplicación local se conecta al SMTP de Gmail, que entrega el mensaje por Internet. |
| ¿Qué diferencia hay entre pruebas? | Mockito aísla lógica; integración verifica HTTP/JPA/transacciones; los escenarios de concurrencia se verifican con PostgreSQL real. |
