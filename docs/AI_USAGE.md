# Registro de uso de Inteligencia Artificial

Requerido por el enunciado (sección 8). Se documenta de forma incremental, actividad por
actividad, a medida que avanza el desarrollo.

Herramienta usada: **Claude Code** (asistente de IA en terminal/IDE).

---

### Documentación inicial (REQUIREMENTS.md, ADR.md, DECISIONS.md, ARCHITECTURE.md, API.md)

- **Actividad:** Transcripción del enunciado en PDF a `docs/REQUIREMENTS.md`, y redacción inicial
  de la estructura de `ADR.md`, `ARCHITECTURE.md`, `API.md` y `DECISIONS.md` a partir de
  decisiones tomadas explícitamente por el candidato en conversación con la IA.
- **Resultado aprovechado:** La transcripción de requisitos (RF01-RF07, datos semilla) y el
  formato/estructura de los documentos. El contenido de las decisiones técnicas (Gradle Kotlin
  DSL, single module, Java 21, R2DBC+H2, Angular standalone, Services+RxJS+Signals, CSS
  plano/Angular Material) fue **decidido explícitamente por el candidato** respondiendo preguntas
  puntuales planteadas antes de escribir código; la IA redactó la justificación formal en
  prosa a partir de esas respuestas.
- **Validación:** Revisión manual del candidato del contenido transcrito contra el PDF original y
  de que las decisiones registradas correspondan a lo efectivamente elegido.
- **Resultados corregidos o descartados:** Ninguno en esta etapa.
- **Información sensible:** El enunciado y los datos semilla (cuentas CTA-1001/1002/2001, límites)
  son datos de ejercicio ficticios, sin información personal ni credenciales reales; no se expuso
  información sensible.

---

---

### Scaffolding inicial de backend y frontend

- **Actividad:** Generación de la estructura de carpetas Clean Architecture del backend
  (`domain`/`application`/`infrastructure`/`api`), el wrapper de Gradle (Kotlin DSL), el
  `build.gradle.kts` con dependencias WebFlux/R2DBC/H2, y el scaffold inicial de Angular
  (`ng new`, componentes `transfer-form`/`transfer-list`, servicio `transfer`), todo sobre
  decisiones ya tomadas explícitamente por el candidato (ver ADR.md).
- **Resultado aprovechado:** Los comandos de generación (`gradle wrapper`, `ng new`,
  `ng generate component/service`) y los archivos resultantes, tal cual los produce cada
  herramienta oficial.
- **Validación:** Se ejecutó `./gradlew build` y `./gradlew bootRun` (arranque correcto del
  servidor Netty en el puerto 8080) para el backend, y `npx ng build` + `npx ng test` (5 pruebas
  generadas, todas en verde) para el frontend.
- **Resultados corregidos o descartados:** Se detectó y corrigió manualmente un defecto de los
  schematics de Angular CLI 22.2.0: `ng generate service` generó un decorador `@Service()`
  inexistente en `@angular/core`; se reemplazó por `@Injectable({ providedIn: 'root' })` (detalle
  completo en `docs/DECISIONS.md`). También se ajustó la configuración de Java del backend
  (compilar con `--release 21` en lugar de un toolchain Gradle) por no haber un JDK 21 instalado
  en el entorno, y se usó Gradle 9.8.0 en vez de 8.x por incompatibilidad del JDK 25 instalado con
  Gradle 8.x.
- **Información sensible:** No se expuso información sensible; solo nombres de paquete, estructura
  de carpetas y configuración de build, sin datos de negocio reales.

---

---

### Implementación de RF01/RF02/RF03/RF05 (dominio, puertos, adaptadores, API, pruebas)

- **Actividad:** Escritura de las clases de dominio (`Transfer`, `Account`, `DailyLimitPolicy`),
  los puertos de entrada/salida de `application`, el caso de uso `ProcessTransferService` y
  `TransferQueryService`, los adaptadores de infraestructura (R2DBC + mapper + seed de cuentas en
  memoria), el controller/DTOs/manejo de errores de `api`, y las 18 pruebas automatizadas
  (dominio, servicio con Mockito/StepVerifier, integración HTTP→H2 real) — todo sobre las
  decisiones de diseño ya tomadas explícitamente por el candidato (records inmutables,
  BigDecimal, entidad R2DBC separada con mapper, seed en memoria).
- **Resultado aprovechado:** La implementación completa de código y pruebas, generada por la IA
  siguiendo el diseño acordado.
- **Validación:** Se ejecutó el flujo real end-to-end con `curl` contra el servidor levantado
  (10 escenarios: autorizada, repetida/idempotente, monto ≤0, cuentas iguales, cuenta inválida,
  límite diario excedido, consulta por referencia, listado reciente, validación 400, 404), y
  luego se corrió la suite completa de pruebas automatizadas (`./gradlew test`, 18/18 en verde).
- **Resultados corregidos o descartados:** Un primer intento de `ProcessTransferService` no
  compilaba por un conflicto de inferencia de tipos genéricos de Reactor
  (`Mono<Account>` vs `Mono<Transfer>` mezclados en una cadena de `switchIfEmpty`); se corrigió
  rediseñando el flujo para que toda la cadena de validación trabaje sobre `Mono<Optional<String>>`
  (razón de rechazo) y solo al final se decide `authorized`/`rejected` y se persiste. También se
  detectó, mediante una prueba de integración que falló, que H2 redondeaba `processed_at` a
  microsegundos con `TIMESTAMP` simple, rompiendo la comparación exacta de idempotencia (RF05); se
  corrigió declarando la columna como `TIMESTAMP(9)` (detalle completo en `docs/DECISIONS.md`).
- **Información sensible:** No se expuso información sensible; solo datos de ejercicio (cuentas
  ficticias CTA-1001/1002/2001) y código/configuración.

---

*(Próximas entradas se agregarán conforme avance la implementación de frontend y RF04.)*
