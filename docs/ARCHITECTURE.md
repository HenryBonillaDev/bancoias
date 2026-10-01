# Arquitectura

## Visión general

```
                         ┌──────────────────────────┐
                         │        frontend/          │
                         │   Angular (standalone)     │
                         │  TransferFormComponent     │
                         │  TransferListComponent     │
                         │        TransferService     │
                         └─────────────┬──────────────┘
                                       │ HTTP/JSON (REST)
                                       ▼
┌───────────────────────────────── backend/ ──────────────────────────────────┐
│                                                                              │
│  api/            → Controllers (WebFlux), DTOs de request/response,         │
│                    manejo de errores HTTP                                   │
│                        │                                                    │
│                        ▼                                                    │
│  application/    → Casos de uso (ProcessTransferUseCase, GetTransferUseCase)│
│                    orquestan el dominio, definen puertos (interfaces)       │
│                        │                                                    │
│                        ▼                                                    │
│  domain/         → Entidades y reglas de negocio puras (Transfer, Account,  │
│                    DailyLimitPolicy). Sin dependencias de Spring/R2DBC.     │
│                        ▲                                                    │
│                        │ implementa puertos definidos en application/       │
│  infrastructure/ → Adaptadores: TransferR2dbcRepository, AccountRepository  │
│                    (seed en memoria), configuración R2DBC/H2                │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
                         ┌──────────────────────────┐
                         │   H2 (modo memoria,        │
                         │   acceso vía R2DBC)        │
                         └──────────────────────────┘
```

Ver justificación de este layout en [ADR-001](./ADR.md#adr-001-layout-de-módulos-del-backend--single-module-con-paquetes-por-capa).

## Regla de dependencia (Clean Architecture)

- `domain` no depende de nada externo (ni de Spring, ni de R2DBC, ni de `infrastructure`).
- `application` depende de `domain` y define **puertos** (interfaces) que `infrastructure`
  implementa — ej. `TransferRepositoryPort`, `AccountLookupPort`.
- `infrastructure` depende de `application`/`domain` (implementa sus puertos) y de frameworks
  (Spring Data R2DBC).
- `api` depende de `application` (invoca casos de uso) y traduce HTTP ↔ modelo de aplicación.

## Estrategias para los requisitos críticos (RF04 y RF05)

> Se amplía el detalle de validación en [ADR-004](./ADR.md#adr-004-persistencia-con-r2dbc--h2-en-memoria).
> El diseño final de estas estrategias se documentará con el detalle de implementación a medida
> que se construya `application`/`infrastructure`.

- **RF04 (concurrencia sobre el límite diario): pendiente.** La implementación actual
  (`ProcessTransferService.validateDailyLimitAndSave`) hace `SUM` del día + `INSERT` como dos
  pasos no atómicos — correcta en el camino feliz y bajo baja contención, pero no garantiza el
  límite bajo solicitudes verdaderamente concurrentes sobre la misma cuenta origen. Queda como
  siguiente paso explícito (ver estado detallado en
  [REQUIREMENTS.md §12](./REQUIREMENTS.md#nota-sobre-rf04-concurrencia)).
- **RF05 (idempotencia por `requestReference`): implementado.** `TransferPersistenceAdapter`
  primero intenta leer por referencia (camino rápido para referencias ya conocidas) y, si dos
  solicitudes con la misma referencia nueva llegan casi al mismo tiempo, el constraint `UNIQUE`
  de `schema.sql` deja pasar un solo `INSERT`; la otra recibe una `DataIntegrityViolationException`
  que el adaptador traduce en releer y devolver la fila ya persistida (en vez de fallar o
  duplicar). Validado en
  `TransferControllerIntegrationTest.repeatingTheSameRequestReferenceReturnsTheOriginalResult_RF05`.

## Frontend

- Dos vistas principales: formulario de nueva transferencia y listado de transferencias
  recientes, ambas standalone components (ver [ADR-005](./ADR.md#adr-005-frontend-angular-con-standalone-components)).
- `TransferService` centraliza las llamadas HTTP y expone el estado compartido (resultado de la
  última operación + listado reciente) vía signals/RxJS (ver [ADR-006](./ADR.md#adr-006-manejo-de-estado-en-el-frontend-con-services--rxjs--signals)).

## Pendiente de documentar

- [ ] Diagrama de secuencia de una solicitud de transferencia (feliz y rechazada).
- [ ] Detalle final de la estrategia de concurrencia (RF04) una vez implementada.
- [ ] Interfaz Angular (RF07).
- [ ] Integración RabbitMQ, si se implementa el punto opcional.
