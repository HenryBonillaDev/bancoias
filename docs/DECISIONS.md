# Decisiones de negocio y trade-offs (bitácora)

Este documento es un **log incremental** de decisiones más puntuales tomadas durante la
implementación — interpretaciones de reglas de negocio, casos borde, y trade-offs menores que no
alcanzan a justificar un ADR formal (ver [ADR.md](./ADR.md) para decisiones de arquitectura).

Formato por entrada: fecha, problema, alternativas, decisión, trade-off/riesgo.

---

## Plantilla de entrada

```
### [YYYY-MM-DD] Título corto de la decisión

**Problema:** ¿Qué ambigüedad o caso borde del enunciado motivó la decisión?

**Alternativas consideradas:** ...

**Decisión:** ...

**Trade-off / riesgo:** ...
```

---

### [2026-09-30] Compilar con JDK 25 instalado usando `--release 21` en vez de toolchain

**Problema:** [ADR-003](./ADR.md#adr-003-java-21-lts) fija Java 21 como versión objetivo, pero el
JDK instalado en el entorno de desarrollo es 25. Configurar un Gradle toolchain
(`languageVersion = JavaLanguageVersion.of(21)`) falló porque no hay un JDK 21 instalado y
Gradle no tiene configurado un repositorio de auto-provisioning de toolchains (requeriría acceso
de red adicional no garantizado para el evaluador).

**Alternativas consideradas:**
1. Configurar auto-provisioning de toolchains (plugin `foojay-resolver`) para que Gradle descargue
   un JDK 21 automáticamente.
2. Compilar con el JDK 25 instalado, fijando `sourceCompatibility`/`targetCompatibility` = 21 y
   `options.release.set(21)` en `JavaCompile` (cross-compilation soportada por `javac` desde
   JDK 9).

**Decisión:** Opción 2.

**Trade-off / riesgo:** La ejecución (`bootRun`) corre sobre JDK 25 en runtime, no sobre un JDK 21
real; si el evaluador tiene un JDK 21 instalado localmente, el proyecto compilará y correrá igual
sin cambios. Se documenta para que quede explícito que el *target* de compatibilidad de código
sigue siendo 21 (API y bytecode), aunque el JDK de build/runtime en este entorno sea 25.

---

### [2026-09-30] Gradle 9.8.0 en vez de 8.x para el wrapper del backend

**Problema:** Gradle 8.14.3 (última de la línea 8.x) no reconoce el JDK 25 instalado como JVM
válida para ejecutar Gradle mismo (falla antes de llegar a compilar).

**Decisión:** Generar el wrapper con Gradle 9.8.0, que sí soporta JDK 25 para ejecutar el propio
Gradle. El plugin de Spring Boot 3.5.16 funciona correctamente sobre Gradle 9 para este proyecto
(validado con `./gradlew build` y `./gradlew bootRun` exitosos), aunque emite advertencias de
*deprecation* de cara a Gradle 10 — no bloquean el build y se dejan pendientes de revisión futura.

---

### [2026-09-30] Bug de Angular CLI 22.2.0 al generar un servicio (`@Service()` inexistente)

**Problema:** `ng generate service core/services/transfer` generó
`import { Service } from '@angular/core'; @Service() export class Transfer {}`. `Service` no
existe en `@angular/core` (se verificó en los `.d.ts` del paquete) — es un defecto de los
schematics de esa versión de Angular CLI, no una decisión de diseño.

**Decisión:** Corregido manualmente a `@Injectable({ providedIn: 'root' })`, que es el decorador
correcto y el patrón estándar de Angular para servicios singleton a nivel de aplicación.

**Validación:** `npx ng build` y `npx ng test` corren sin errores tras la corrección.

---

### [2026-09-30] Angular 22 usa Vitest como test runner por defecto (no Karma)

**Problema/contexto:** Al intentar correr pruebas con `--browsers=ChromeHeadless` (flag típico de
Karma) falló porque Angular CLI 22 usa **Vitest** por defecto para `ng test`, sin necesidad de un
navegador real instalado.

**Decisión:** Usar el comando por defecto `ng test` (Vitest). No se requiere instalar Chrome/
Karma en el entorno. Las 5 pruebas generadas por el scaffold inicial pasan correctamente.

---

### [2026-10-01] Columna `processed_at` en H2 declarada como `TIMESTAMP(9)`

**Problema:** La prueba de integración de idempotencia (RF05) comparaba el `processedAt` de la
respuesta de la primera solicitud (construido en memoria con `Instant.now(clock)`, precisión de
nanosegundos) contra el `processedAt` de la segunda solicitud (leído de vuelta desde H2 vía
`findByRequestReference`). Con la columna declarada como `TIMESTAMP` simple, H2 redondeaba a
microsegundos, y ambos valores dejaban de ser bit-idénticos aunque representaran el mismo instante
real.

**Decisión:** Declarar `processed_at` como `TIMESTAMP(9)` en `schema.sql` para preservar
precisión de nanosegundos, igual que `java.time.Instant`.

**Validación:** `TransferControllerIntegrationTest.repeatingTheSameRequestReferenceReturnsTheOriginalResult_RF05`
pasa comparando el timestamp exacto antes y después del round-trip por base de datos.

---

### [2026-10-01] Swagger UI (springdoc-openapi) para probar la API manualmente

**Problema:** El candidato quiere probar la API con una interfaz interactiva (Swagger/OpenAPI) en
vez de solo `curl`, para verificar visualmente que RF01/RF02/RF05/RF06 funcionan.

**Decisión:** Agregar `springdoc-openapi-starter-webflux-ui:2.8.6` (variante para WebFlux, ya que
la variante no-reactiva de springdoc no funciona sobre un `WebFluxConfigurer`/Netty). Se agregó un
`OpenApiConfig` mínimo solo con título/descripción/versión — sin anotaciones `@Operation`
adicionales en los controllers, dado el alcance acotado del ejercicio.

**Trade-off / riesgo:** Ninguno relevante; es una dependencia de solo documentación/UI, no afecta
la lógica de negocio.

**Validación:** Con la app corriendo, `GET /v3/api-docs` devuelve el contrato OpenAPI con los 3
endpoints (`POST /api/transfers`, `GET /api/transfers/{requestReference}`, `GET /api/transfers`),
y `GET /swagger-ui.html` redirige (302) a `/swagger-ui/index.html`, que responde `200 OK`.

---

### [2026-10-01] Bug reportado por el candidato: espacios en blanco en `requestReference` rompían la búsqueda (RF05/RF06)

**Problema:** Probando desde Swagger UI, el candidato detectó que si el JSON del request traía un
espacio accidental (p. ej. `"requestReference": " REF-001"`), el sistema lo persistía tal cual.
Luego, `GET /api/transfers/REF-001` (sin el espacio) devolvía 404, porque la comparación de
`requestReference` en base de datos es exacta. El mismo problema aplicaba a `sourceAccountId`/
`destinationAccountId`: un espacio accidental hacía que una cuenta válida (`CTA-1001`) pareciera
inválida (` CTA-1001` no está en el mapa de cuentas semilla).

**Alternativas consideradas:**
1. Rechazar el request (400) si algún campo de texto trae espacios al inicio/final.
2. Recortar (`trim`) los espacios silenciosamente antes de procesar la solicitud.

**Decisión:** Opción 2 — recortar espacios. Es el comportamiento más tolerante y esperable para
quien consume la API manualmente (un espacio accidental al copiar/pegar un valor no debería
convertirse en un rechazo ni en una referencia "fantasma" imposible de encontrar luego).

**Dónde se aplicó:** en el límite de la aplicación (`TransferDtoMapper.toCommand`, antes de
construir el `ProcessTransferCommand`) y en `TransferController.getByRequestReference` (recorta
el `@PathVariable` antes de consultar). Así `domain`/`application` siempre trabajan con valores ya
normalizados, y la regla de negocio no se mezcla con la limpieza de input.

**Trade-off / riesgo:** Si alguna vez una referencia o id de cuenta necesitara espacios
intencionales como parte de su valor (no es el caso aquí, según los datos semilla del
enunciado), este trim los eliminaría silenciosamente. Aceptable para el alcance de este ejercicio.

**Validación:** Nueva prueba de integración
`TransferControllerIntegrationTest.trimsWhitespaceAroundTextFieldsSoReferencesAreFoundWithoutTheStraySpace`:
envía `" IT-REF-TRIM "`/`" CTA-1001 "`/`" CTA-2001 "`, verifica que la respuesta y la persistencia
quedan sin espacios, que `GET /api/transfers/IT-REF-TRIM` la encuentra, y que reenviarla sin
espacios sigue tratándose como la misma referencia (RF05).

---

### [2026-10-01] Endpoint de solo lectura `GET /api/accounts/{id}/daily-usage`

**Contexto:** El enunciado dice explícitamente que no se requiere un módulo de administración de
cuentas, y el dominio no modela "saldo" (solo un límite diario que se consume por transferencias
autorizadas). Aun así, el candidato pidió una forma de verificar visualmente — mientras prueba
desde Swagger — si sus transferencias de prueba efectivamente se reflejan en el acumulado diario
de una cuenta, sin tener que sumar manualmente el listado de transferencias recientes.

**Decisión:** Agregar un endpoint puramente de lectura, `GET /api/accounts/{accountId}/daily-usage`,
que devuelve `dailyLimit`, `consumedToday` y `remaining` para una cuenta, reutilizando el mismo
`TransferRepositoryPort.sumAuthorizedAmount` que ya usa `ProcessTransferService` para validar RF02.
Se modeló como un caso de uso nuevo (`GetAccountDailyUsageUseCase` / `AccountQueryService`) y un
value object de dominio no persistido (`AccountDailyUsage`), en vez de mezclar esta lógica dentro
de `ProcessTransferService` o `TransferController`.

**Trade-off / riesgo:** Es scope adicional no pedido por el enunciado (el propio enunciado lo
exime explícitamente), pero acotado a un endpoint de solo lectura sin efectos secundarios — no
compite con el alcance obligatorio ni introduce un módulo de administración de cuentas real.

**Validación:** Dos pruebas de integración nuevas (`AccountControllerIntegrationTest`) comparando
deltas antes/después de una transferencia autorizada y de una rechazada, más verificación manual
con `curl` confirmando que el consumo arranca en 0, sube con una autorizada y no se mueve con una
rechazada.

---

### [2026-10-01] RF04: transacción SERIALIZABLE + reintento (elegido explícitamente por el candidato)

**Contexto:** El chequeo de límite diario hacía `SUM` + `INSERT` como dos pasos no atómicos — bajo
solicitudes concurrentes reales sobre la misma cuenta origen, ambas podían leer el mismo acumulado
"antes" de que la otra confirmara, autorizando juntas más de lo permitido.

**Alternativas presentadas:** (1) serialización en memoria por cuenta (mutex reactivo, sin tocar
la base de datos) vs. (2) transacción R2DBC con aislamiento `SERIALIZABLE` + reintento ante
conflicto. El candidato eligió la opción 2: la garantía vive en la base de datos, no en el
proceso Java — más alineado con la justificación de ADR-004 de usar R2DBC+H2 en vez de una
estructura en memoria.

**Decisión:** Nuevo puerto `TransactionalExecutionPort` (para no filtrar tipos de
`org.springframework.transaction.*` hacia `application`, manteniendo ADR-001), implementado por
`TransactionalExecutionAdapter` con `TransactionalOperator` + aislamiento `SERIALIZABLE` +
`Retry.backoff` filtrando por SQLState `40001` (conflicto de serialización estándar SQL).
`ProcessTransferService.decideAndSave` (sum + decidir + guardar) se ejecuta completo dentro de esa
transacción.

**Validación:** Se escribió `ConcurrentTransferIntegrationTest` (2 solicitudes HTTP concurrentes
reales vía `WebClient`, no mocks) y se verificó el ciclo completo: (a) con la protección activa,
nunca se autorizan ambas si juntas exceden el límite — corrida repetida sin fallos; (b) se quitó
temporalmente la línea que envuelve la transacción y se confirmó que la misma prueba falla de
forma consistente (ambas se autorizaban), demostrando que el test realmente ejercita la condición
de carrera y no pasa "por casualidad".

**Trade-off / riesgo:** El monto de la prueba se calcula como `remaining/2 + 1` en vez de un valor
fijo, precisamente para no depender de cuánto haya consumido otra prueba antes sobre la misma
cuenta (las pruebas de integración comparten una sola base H2 en memoria entre clases). Un primer
diseño más exhaustivo (muchas solicitudes concurrentes intentando agotar el límite exacto) se
descartó por generar una dependencia frágil con otras pruebas que comparten cuenta — se prefirió
una prueba más simple y determinista acotada a 2 solicitudes.
