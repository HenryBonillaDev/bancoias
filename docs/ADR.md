# Architecture Decision Records (ADR)

Registro de decisiones técnicas relevantes del proyecto BancoIAS - Core de Transferencias.
Cada ADR sigue: contexto, alternativas consideradas, decisión, trade-offs/riesgos, validación.

---

## ADR-001: Layout de módulos del backend — single module con paquetes por capa

**Contexto:** El backend debe seguir Clean Architecture dentro del esfuerzo estimado de 2 horas.

**Alternativas consideradas:**
1. Multi-módulo Gradle real (`domain`, `application`, `infrastructure`, `api` como subproyectos
   independientes, cada uno con su propio `build.gradle.kts`).
2. Single module con separación por paquetes (`domain`, `application`, `infrastructure`, `api`)
   dentro de un único módulo Gradle.

**Decisión:** Opción 2 — single module, paquetes por capa.

**Justificación:** El multi-módulo real aporta un *enforcement* físico de las dependencias entre
capas (el compilador impide que `domain` dependa de `infrastructure`), pero añade configuración de
Gradle (settings.gradle.kts con subproyectos, build.gradle.kts por módulo, gestión de
dependencias entre módulos) que consume tiempo del esfuerzo estimado sin aportar valor
funcional al ejercicio. Con un solo módulo y disciplina de paquetes/visibilidad se preserva la
separación de responsabilidades de Clean Architecture (dominio sin dependencias de framework,
casos de uso orquestando puertos, adaptadores de infraestructura implementando esos puertos) de
forma suficiente para un ejercicio de este alcance.

**Trade-offs / riesgos:** No hay enforcement físico del compilador; un desarrollador podría
importar accidentalmente una clase de `infrastructure` desde `domain`. Se mitiga con revisión
manual y convención de nombres/paquetes clara.

**Validación:** Se revisa que las clases de `domain` y `application` no importen paquetes de
`infrastructure` ni de Spring Web/R2DBC directamente (solo interfaces/puertos definidos en
`application`).

---

## ADR-002: Gradle con Kotlin DSL

**Contexto:** Se requiere elegir el lenguaje de configuración de build (Gradle Groovy DSL vs
Kotlin DSL).

**Alternativas consideradas:**
1. Groovy DSL (`build.gradle`).
2. Kotlin DSL (`build.gradle.kts`).

**Decisión:** Kotlin DSL.

**Justificación:** Es el estándar recomendado actualmente por Gradle para proyectos nuevos,
ofrece tipado estático y autocompletado en IDEs, y es lo que generan por defecto Spring
Initializr y los plugins modernos de Spring Boot/Gradle.

**Trade-offs / riesgos:** Tiempos de configuración (configuration time) ligeramente mayores que
Groovy en proyectos muy grandes; irrelevante para el tamaño de este ejercicio.

**Validación:** El build compila y ejecuta correctamente (`./gradlew build`).

---

## ADR-003: Java 21 LTS

**Contexto:** Selección de versión de JDK para el backend.

**Alternativas consideradas:**
1. Java 17 LTS.
2. Java 21 LTS.

**Decisión:** Java 21 LTS.

**Justificación:** Es el LTS más reciente disponible, con mejor soporte de pattern matching y
records (útiles para modelar el dominio de forma inmutable), y buen encaje con las versiones
actuales de Spring Boot 3.x / WebFlux. El JDK instalado en el entorno de desarrollo es 25, por lo
que 21 es totalmente compatible como `--release` target.

**Trade-offs / riesgos:** Algunos entornos corporativos conservadores aún están en Java 17; si el
evaluador necesita ejecutar en 17, requeriría bajar el `--release`. Se documenta explícitamente
para que sea una decisión reversible y visible.

**Validación:** `java -version` en el entorno de desarrollo y build exitoso con
`sourceCompatibility`/`targetCompatibility` = 21.

---

## ADR-004: Persistencia con R2DBC + H2 en memoria

**Contexto:** El enunciado requiere persistir las transferencias y maneja dos condiciones
especialmente sensibles a la estrategia de persistencia: concurrencia sobre el límite diario
(RF04) e idempotencia por `requestReference` (RF05).

**Alternativas consideradas:**
1. Repositorio en memoria con `ConcurrentHashMap` + locks manuales (`ReentrantLock` por cuenta
   origen).
2. R2DBC (reactivo, no bloqueante) + H2 en modo memoria.

**Decisión:** R2DBC + H2 en memoria.

**Justificación:** R2DBC es coherente con el stack reactivo obligatorio (WebFlux) — no bloquea
threads del event loop, a diferencia de usar JDBC con `Schedulers.boundedElastic()` como
workaround. Usar una base relacional real permite:
- Modelar un **constraint `UNIQUE`** sobre `request_reference` para resolver RF05 (idempotencia)
  apoyándose en la base de datos en lugar de reconstruir esa garantía a mano en memoria.
- Apoyar el control de concurrencia de RF04 en transacciones/consultas atómicas sobre la tabla,
  más representativo de un "core transaccional" real que una estructura en memoria.
- H2 en modo memoria no requiere infraestructura externa (Docker, Postgres), por lo que el
  evaluador puede levantar el proyecto con un solo comando, igual que la alternativa en memoria.

**Trade-offs / riesgos:** H2 no es 100% compatible con la sintaxis de un motor productivo
(Postgres/MySQL); si el proyecto evolucionara a producción se requeriría migrar el dialecto SQL y
validar con el motor real. Los datos se pierden al reiniciar la aplicación (aceptable: el propio
enunciado asume que no hay transferencias autorizadas al iniciar). R2DBC tiene un ecosistema más
joven que JDBC (menos herramientas de migración maduras); se mitiga usando `schema.sql` simple en
vez de una herramienta de migración completa (Flyway/Liquibase), dado el alcance acotado.

**Validación:** Pruebas de integración con `DatabaseClient`/repositorios R2DBC contra H2 en
memoria; pruebas específicas de concurrencia (múltiples solicitudes simultáneas a la misma cuenta
origen) y de idempotencia (misma `requestReference` enviada más de una vez).

---

## ADR-005: Frontend Angular con standalone components

**Contexto:** Selección de estilo de organización de componentes Angular.

**Alternativas consideradas:**
1. NgModules tradicionales (`app.module.ts`, feature modules).
2. Standalone components (sin NgModules).

**Decisión:** Standalone components.

**Justificación:** Es el enfoque por defecto en las versiones actuales de Angular, reduce
boilerplate y encaja mejor con el tamaño del ejercicio (dos vistas: formulario de transferencia y
listado de transferencias recientes).

**Trade-offs / riesgos:** Ninguno relevante para este alcance.

**Validación:** `ng build` / `ng serve` sin errores, navegación funcional entre las vistas.

---

## ADR-006: Manejo de estado en el frontend con Services + RxJS + Signals

**Contexto:** El frontend necesita compartir entre componentes el resultado de la última
operación y el listado de transferencias recientes.

**Alternativas consideradas:**
1. NgRx (store completo con actions/reducers/effects/selectors).
2. Un `TransferService` con `signals`/`RxJS` (sin librería de estado externa).

**Decisión:** Opción 2 — Services + RxJS + Signals.

**Justificación:** NgRx añade boilerplate considerable difícil de justificar para dos pantallas y
un flujo de datos simple (crear transferencia → refrescar listado). Un servicio con signals/RxJS
es el patrón estándar recomendado por el equipo de Angular para este tamaño de aplicación y es
suficiente para demostrar integración real con el backend.

**Trade-offs / riesgos:** Si la aplicación creciera en complejidad de estado compartido, NgRx (u
otra librería) sería más apropiado; se documenta como límite de alcance consciente.

**Validación:** Verificación manual en navegador: al registrar una transferencia, el resultado se
refleja inmediatamente y el listado de recientes se actualiza sin recargar la página.

---

## ADR-007: Estilos — CSS plano / Angular Material mínimo (sin Tailwind)

**Contexto:** El enunciado indica explícitamente que no se evalúa diseño gráfico avanzado.

**Alternativas consideradas:**
1. Tailwind CSS.
2. CSS plano, con posibilidad de usar un par de componentes de Angular Material (inputs, tabla).

**Decisión:** Opción 2.

**Justificación:** Tailwind requiere configuración adicional (PostCSS, content paths) que no
aporta valor funcional frente al criterio de evaluación del ejercicio y consume tiempo del
esfuerzo estimado. CSS plano (con componentes puntuales de Angular Material si se necesitan) es
suficiente para una interfaz funcional y clara.

**Trade-offs / riesgos:** Resultado visual más básico; aceptable según el propio enunciado.

**Validación:** Revisión visual manual de las dos vistas principales.
