# Requisitos del ejercicio

Fuente: *Prueba Técnica - Full Stack Java Spring Boot WebFlux / Angular* (BancoIAS).
Esfuerzo estimado por el enunciado: 2 horas.

## 1. Contexto de negocio

BancoIAS es una entidad financiera con productos transaccionales para personas y empresas.
El **core de transferencias** procesa, valida y da trazabilidad a movimientos de dinero entre
cuentas. Se debe evolucionar esta funcionalidad para registrar transferencias y mantener
trazabilidad de su resultado, considerando que pueden llegar **solicitudes repetidas o
concurrentes**.

## 2. Objetivo

Construir una solución Full Stack mínima y funcional que permita **procesar, persistir y
consultar transferencias**, y presentar desde Angular el resultado de las operaciones y las
transferencias procesadas recientemente.

## 3. Datos iniciales (seed)

No se requiere módulo de administración de cuentas ni integración con sistemas externos.

| Cuenta   | Condición     | Límite diario (COP) |
|----------|---------------|----------------------|
| CTA-1001 | Cuenta válida | 5.000.000            |
| CTA-1002 | Cuenta válida | 5.000.000            |
| CTA-2001 | Cuenta válida | 5.000.000            |

Al iniciar la aplicación se asume que **no existen transferencias autorizadas** durante el día.

### Datos mínimos de una solicitud de transferencia

| Campo (ilustrativo)    | Ejemplo   | Significado                          |
|------------------------|-----------|---------------------------------------|
| `requestReference`     | `REF-001` | Referencia única de la solicitud       |
| `sourceAccountId`      | `CTA-1001`| Cuenta origen                          |
| `destinationAccountId` | `CTA-2001`| Cuenta destino                         |
| `amount`               | `600000`  | Valor de la transferencia en COP       |

> Los nombres técnicos son ilustrativos y pueden adaptarse conservando su significado funcional.
> El consumo diario se calcula **solo con transferencias autorizadas** de la cuenta origen durante
> el día correspondiente. La fecha/hora de procesamiento la registra el sistema.

## 4. Requisitos funcionales (RF01–RF07)

| ID | Requisito | Detalle |
|----|-----------|---------|
| **RF01** | Procesar una transferencia | Permitir solicitar una transferencia con los datos iniciales y registrar fecha/hora de procesamiento. |
| **RF02** | Validar reglas de negocio | Autorizar solo si: valor > 0, cuenta origen ≠ cuenta destino, datos requeridos válidos, y la operación no hace que la cuenta origen supere el límite diario permitido. |
| **RF03** | Conservar resultado de la operación | Toda solicitud procesada conserva: datos principales, si fue autorizada o rechazada, cuándo se procesó, y (si aplica) una razón general del resultado. Una solicitud **rechazada no incrementa** el acumulado diario. |
| **RF04** | Manejar solicitudes simultáneas | El core puede recibir solicitudes casi simultáneas para la misma cuenta origen. Las reglas del límite diario deben preservarse incluso en concurrencia. Estrategia de resolución libre. |
| **RF05** | Manejar referencias repetidas (idempotencia) | Una misma `requestReference` puede llegar más de una vez: no debe generar una segunda transferencia ni incrementar de nuevo el acumulado; debe devolver el resultado de la solicitud original. Si la misma referencia llega con datos distintos, debe manejarse de forma controlada preservando la operación original. Estrategia libre. |
| **RF06** | Consultar transferencias | El backend permite consultar: (a) una transferencia por `requestReference`, y (b) las transferencias recientes procesadas. Sin paginación avanzada. |
| **RF07** | Interfaz Angular | Permitir registrar una nueva solicitud, visualizar claramente el resultado, y consultar las transferencias recientes. Sin exigencia de diseño gráfico avanzado, pero con integración real al backend. |

## 5. Restricciones técnicas y operativas

- Backend obligatorio: **Java con Spring Boot WebFlux**.
- Frontend obligatorio: **Angular**.
- Las transferencias procesadas **deben persistirse**. Modelo, tecnología, estructura y
  estrategia de persistencia quedan a criterio del candidato.
- Las demás decisiones tecnológicas quedan a criterio del candidato.
- El alcance obligatorio debe mantenerse acotado al esfuerzo estimado (2 horas).

## 6. Pruebas automatizadas

- Incluir pruebas automatizadas sobre los comportamientos relevantes para demostrar que las
  reglas de negocio implementadas funcionan correctamente.
- No hay porcentaje de cobertura exigido.

## 7. Decisiones técnicas (trazabilidad obligatoria)

Debe quedar registro (ver [ADR.md](./ADR.md) y [DECISIONS.md](./DECISIONS.md)) de, cuando
corresponda:

- Contexto o problema que motivó la decisión.
- Alternativas consideradas.
- Decisión adoptada y su justificación.
- Trade-offs o riesgos identificados.
- Forma en que se validó que la decisión funciona correctamente.

No existe arquitectura, patrón o estructura de proyecto predeterminada: las decisiones deben
poder explicarse y defenderse técnicamente.

## 8. Uso de inteligencia artificial

Debe documentarse brevemente (ver registro de uso de IA, sección correspondiente en este
repositorio):

- En qué actividades se usó IA.
- Qué resultados fueron aprovechados.
- Cómo fueron validados.
- Qué resultados fueron corregidos o descartados.
- Qué consideraciones se aplicaron para evitar exponer información sensible.

Si no se usa IA en alguna actividad, se indica explícitamente.

## 9. Punto opcional — RabbitMQ

Completamente opcional, no afecta la evaluación del alcance obligatorio.

Cuando una transferencia sea **autorizada**, BancoIAS necesita permitir que otros sistemas
reaccionen posteriormente a ese hecho de negocio. Se puede integrar RabbitMQ para publicar o
procesar información asociada a una transferencia autorizada. Topología, contrato, reintentos,
duplicados y manejo de errores quedan abiertos al criterio del candidato. Si se implementa, debe
documentarse brevemente.

## 10. Criterio de completitud visible

El alcance obligatorio se considera completo cuando la entrega permite verificar RF01–RF07, la
persistencia requerida, las pruebas automatizadas seleccionadas, las instrucciones de ejecución y
la trazabilidad de decisiones técnicas y uso de IA. Cualquier limitación o alcance pendiente debe
quedar identificado claramente. RabbitMQ no es parte de este criterio.

## 11. Entrega

El repositorio Git debe contener como mínimo:

- Código fuente del backend.
- Código fuente del frontend.
- Pruebas automatizadas.
- Instrucciones suficientes para ejecutar la solución.
- Registro de decisiones técnicas.
- Registro del uso de IA.
- Historial de commits realizado durante el desarrollo.

## 12. Estado de cobertura de requisitos (se irá actualizando)

| Requisito | Estado |
|-----------|--------|
| RF01 | Hecho — `POST /api/transfers` procesa y persiste, con `processedAt` registrado |
| RF02 | Hecho — monto > 0, cuentas distintas, cuentas válidas, límite diario |
| RF03 | Hecho — se persiste autorizada/rechazada con razón y timestamp |
| RF04 | Hecho — transacción `SERIALIZABLE` + reintento (ver nota abajo) |
| RF05 | Hecho (básico) — idempotencia por `requestReference` vía lectura previa + constraint `UNIQUE` en BD |
| RF06 | Hecho — `GET /api/transfers/{ref}` y `GET /api/transfers?limit=N` |
| RF07 | Pendiente — interfaz Angular |
| Persistencia | Hecho — R2DBC + H2, `schema.sql` con constraint único |
| Pruebas automatizadas | Hecho (backend) — 18 pruebas: dominio, servicio de aplicación (mocks) e integración HTTP→BD real |
| RabbitMQ (opcional) | No implementado |

### Nota sobre RF04 (concurrencia)

La secuencia "leer acumulado del día + decidir + guardar" (`ProcessTransferService.decideAndSave`)
se ejecuta dentro de una transacción R2DBC con aislamiento `SERIALIZABLE`
(`TransactionalExecutionPort`/`TransactionalExecutionAdapter`), con reintento automático si la base
de datos detecta un conflicto de serialización entre dos solicitudes concurrentes sobre la misma
cuenta origen. Validado con una prueba de integración que dispara 2 solicitudes simultáneas reales
(`ConcurrentTransferIntegrationTest`) y confirma que nunca se autorizan ambas si juntas exceden lo
disponible — y que, revertido el fix temporalmente, la prueba efectivamente falla (ver
[DECISIONS.md](./DECISIONS.md)).
