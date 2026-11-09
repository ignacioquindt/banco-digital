# BANCO DIGITAL · Ficha de Producción Audiovisual y Guión Técnico
**Video Demostrativo y Explicativo de Portfolio para Posiciones Backend Java en Banca y Fintech**

---

## 🎬 1. Ficha Técnica de Producción

| Parámetro | Especificación |
|---|---|
| **Resolución & Aspect Ratio** | 1920 x 1080 (Full HD), 16:9 widescreen |
| **Framerate & Codec** | 30 / 60 fps · Video: H.264 (libx264, CRF 19) · Audio: AAC 192 kbps |
| **Duración Total** | 3 minutos exactos (180 segundos) |
| **Dirección de Arte** | Fintech Moderna, Elegante y Corporativa |
| **Paleta Cromática** | `#030712` (Negro Profundo), `#0B1329` (Azul Noche), `#0284C7` (Azul Corporativo), `#38BDF8` (Celeste Neón), `#F8FAFC` (Blanco Nítido) |
| **Tipografía** | Inter / SF Pro Display / System UI (Sans-serif geométrica de alta legibilidad) |
| **Tono de Locución** | Español Neutro Latinoamericano · Profesional, seguro, pausado y técnico |
| **Banda Sonora** | Tech ambient discreto, pads envolventes y pulsos sutiles sin saturación |

---

## ⏱️ 2. Escaleta y Guión de Locución Sincronizado (00:00 – 03:00)

### Bloque 1: Introducción Cinematográfica (00:00 – 00:25 · 25 seg)
* **Visual:** Fondo dark tech con sutil grilla geométrica y haz de luz radial cian. Aparición suave del isotipo y tipografía con degradé metálico: **BANCO DIGITAL**. Badge superior: `JAVA 21 · SPRING BOOT 3 · POSTGRESQL · REACT`. Tres pilares flotantes: Concurrencia, Idempotencia, Transactional Outbox.
* **Locución (Español Latinoamericano):**
  > *"En el ecosistema financiero actual, la integridad de los datos, la disponibilidad y la resiliencia transaccional no son opcionales: son el núcleo del negocio. **Banco Digital** es una plataforma de banca transaccional diseñada y construida bajo estándares reales de la industria fintech, combinando un backend empresarial en Java 21 y Spring Boot con persistencia relacional en PostgreSQL y una arquitectura totalmente desacoplada."*
* **On-Screen Text (Subtítulos):**
  `BANCO DIGITAL · Plataforma Transaccional de Nivel Bancario & Fintech · Java 21 & Spring Boot 3`

---

### Bloque 2: Presentación de la Aplicación en Funcionamiento (00:25 – 00:55 · 30 seg)
* **Visual:** Transición hacia la ventana interactiva del simulador bancario en ejecución (`http://localhost:5173`). Se muestra el acceso mediante autenticación corporativa, el Dashboard consolidado en vivo con saldos en ARS (`$ 500.000,00`), cuentas activas y barra lateral de navegación por módulos.
* **Locución:**
  > *"La solución opera de forma desacoplada: una Single Page Application moderna desarrollada en React con Vite interactúa con la API REST de Spring Boot a través de un proxy seguro. Desde el panel principal, el usuario visualiza en tiempo real la consolidación contable de sus saldos, gestiona múltiples cuentas bancarias y accede a su historial con actualización instantánea mediante DTOs inmutables implementados con Java Records."*
* **On-Screen Text:**
  `[ARQUITECTURA DESACOPLADA] SPA en React + Vite · API RESTful con DTOs Java Records · Seguridad CSRF`

---

### Bloque 3: Stack Tecnológico Real y Verificado (00:55 – 01:20 · 25 seg)
* **Visual:** Cuadrícula tecnológica interactiva de 8 tarjetas estilizadas en glassmorphism con bordes en cian neón:
  1. *Java 21 LTS* (Records, Pattern Matching, Type Inference)
  2. *Spring Boot 3* (Framework empresarial, inyección de dependencias)
  3. *PostgreSQL* (Motor relacional, transacciones ACID, Row-Level Locking)
  4. *Spring Security* (Control RBAC, encriptación BCrypt, protección CSRF)
  5. *Flyway DB* (Migraciones de esquema versionadas e inmutables V1, V2, V3)
  6. *React + Vite* (Frontend modular desacoplado)
  7. *JUnit 5 & Mockito* (Suite de 31 pruebas unitarias y de integración)
  8. *Docker & Compose* (Infraestructura reproducible localmente)
* **Locución:**
  > *"El stack se seleccionó estrictamente bajo criterios de empleabilidad y robustez corporativa. Java 21 como lenguaje base; Spring Boot 3 gestionando la capa de servicios; Spring Data JPA y Hibernate sobre PostgreSQL para una persistencia transaccional sólida; y Flyway administrando migraciones incrementales de base de datos sin depender de herramientas automáticas en tiempo de ejecución."*
* **On-Screen Text:**
  `[STACK VERIFICADO] Java 21 · Spring Boot 3 · PostgreSQL · Spring Security · Flyway · JUnit 5 · Docker`

---

### Bloque 4: Ciclo Operativo Completo (01:20 – 02:10 · 50 seg)
* **Visual (01:20 – 01:45):** Apertura del modal **Cash-In (Cargar saldo)**. Se ingresa `$ 50.000`. Un callout en pantalla resalta la clave única UUID generada. Se confirma el depósito y se observa la notificación toast y el saldo reflejado al instante.
* **Locución:**
  > *"El ciclo de fondos abarca depósitos, extracciones y transferencias. En la operación de Cash-In, cada solicitud genera una clave de idempotencia UUID única en el cliente. Si la red sufre una intermitencia y la petición se reintenta, el backend valida la clave en base de datos, garantizando que nunca se acredite saldo duplicado."*
* **Visual (01:45 – 02:10):** Apertura del modal **Nueva Transferencia**. Selección de cuenta destino, importe de `$ 15.000`. El sistema procesa la transferencia en vivo, genera el comprobante digital y actualiza la tabla de últimos movimientos.
* **Locución:**
  > *"Para transferir dinero entre cuentas, el servicio adquiere bloqueos pesimistas ordenados en PostgreSQL para evitar condiciones de carrera. El saldo se debita y acredita de forma atómica, validando reglas de negocio como saldos no negativos y emitiendo el comprobante correspondiente."*
* **On-Screen Text:**
  `[IDEMPOTENCIA & CONCURRENCIA] Clave UUID contra reintentos · Bloqueo pesimista SELECT FOR UPDATE en PostgreSQL`

---

### Bloque 5: Arquitectura Backend & Patrones Senior (02:10 – 02:40 · 30 seg)
* **Visual:** Diagrama arquitectónico animado en 3 pilares:
  1. *Bloqueo Pesimista Ordenado:* `minId` y `maxId` mitigando deadlocks concurrentes.
  2. *Transactional Outbox Pattern:* Tabla `outbox_eventos` persistida atómicamente junto al saldo, y job asíncrono `@Scheduled` para despacho de eventos de dominio (`AFTER_COMMIT`).
  3. *Trazabilidad & RBAC:* Filtro `CorrelationIdFilter` inyectando `X-Correlation-ID` en el MDC de SLF4J, roles granulares (`ROLE_CLIENTE`, `ROLE_ADMIN`) y panel corporativo de auditoría inmutable.
* **Locución:**
  > *"A nivel arquitectónico, el sistema implementa patrones avanzados de resiliencia: el patrón Transactional Outbox resuelve el problema de la doble escritura persistiendo los eventos contables en la misma transacción que el saldo; mientras que un filtro de trazabilidad inyecta un Correlation ID en cada solicitud, propagándolo a través del contexto de logs MDC para auditoría distribuida de extremo a extremo."*
* **On-Screen Text:**
  `[PATRONES SENIOR] Transactional Outbox · Eventos AFTER_COMMIT · Correlation ID (MDC) · Seguridad RBAC`

---

### Bloque 6: Cierre de Portfolio (02:40 – 03:00 · 20 seg)
* **Visual:** Plano cinematográfico final. Tarjeta de autor y proyecto:
  * **BANCO DIGITAL**
  * *Proyecto personal de desarrollo backend*
  * Stack: `Java 21 | Spring Boot 3 | PostgreSQL | Spring Security`
  * Autor: **Juan Ignacio Quindt**
  * Repositorio: `github.com/ignacioquindt/banco-digital`
* **Locución:**
  > *"Banco Digital demuestra la capacidad de llevar requerimientos financieros reales a una arquitectura robusta, defendible y lista para producción. Todo el código fuente, pruebas automatizadas y documentación técnica están disponibles en mi repositorio de GitHub."*
* **On-Screen Text:**
  `BANCO DIGITAL · Desarrollado por Juan Ignacio Quindt · github.com/ignacioquindt/banco-digital`

---

## 🎨 3. Especificaciones de Motion Graphics & UI Fintech

* **Banners Flotantes:** Contenedor superior translúcido (`rgba(9, 14, 28, 0.95)`), desenfoque de fondo *glassmorphic* (16px blur), borde con brillo cian (`#38bdf8`) de 2px.
* **Tipografía de Título:** 800 weight, color blanco puro (`#FFFFFF`), tracking positivo `0.3px`, sombra suave `0 2px 8px rgba(0,0,0,0.5)`.
* **Tipografía de Subtítulo:** 400 weight, color gris perla (`#CBD5E1`), interlineado `1.4`.
* **Barra de Progreso:** Línea horizontal inferior continua con gradiente de `#0284C7` a `#38BDF8`.
