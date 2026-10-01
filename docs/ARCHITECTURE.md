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

- **RF04 (concurrencia sobre el límite diario):** estrategia pendiente de implementación —
  candidatos: transacción + relectura del acumulado dentro de la misma transacción R2DBC, o
  constraint/validación atómica a nivel de base de datos. Se documentará la decisión final aquí y
  en ADR.md cuando se implemente.
- **RF05 (idempotencia por `requestReference`):** estrategia pendiente de implementación —
  candidato principal: constraint `UNIQUE` en `request_reference` + manejo del error de
  violación de unicidad para devolver el resultado ya persistido en lugar de fallar.

## Frontend

- Dos vistas principales: formulario de nueva transferencia y listado de transferencias
  recientes, ambas standalone components (ver [ADR-005](./ADR.md#adr-005-frontend-angular-con-standalone-components)).
- `TransferService` centraliza las llamadas HTTP y expone el estado compartido (resultado de la
  última operación + listado reciente) vía signals/RxJS (ver [ADR-006](./ADR.md#adr-006-manejo-de-estado-en-el-frontend-con-services--rxjs--signals)).

## Pendiente de documentar

- [ ] Diagrama de secuencia de una solicitud de transferencia (feliz y rechazada).
- [ ] Detalle final de la estrategia de concurrencia (RF04) una vez implementada.
- [ ] Detalle final de la estrategia de idempotencia (RF05) una vez implementada.
- [ ] Integración RabbitMQ, si se implementa el punto opcional.
