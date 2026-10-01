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
