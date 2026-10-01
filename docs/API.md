# API

> Pendiente de completar a medida que se implementan los endpoints (RF01, RF06). Este documento
> se irá actualizando con el contrato real validado por pruebas de integración.

## Convenciones

- Content-Type: `application/json`.
- Formato de fecha/hora: ISO-8601 UTC (`processedAt`).
- Los nombres de campos son los definidos funcionalmente en
  [REQUIREMENTS.md](./REQUIREMENTS.md#3-datos-iniciales-seed), con posibles adaptaciones técnicas
  documentadas aquí si aplica.

## Endpoints planeados

### `POST /api/transfers` — RF01, RF02, RF03, RF04, RF05

Registra una solicitud de transferencia.

**Request body (borrador):**
```json
{
  "requestReference": "REF-001",
  "sourceAccountId": "CTA-1001",
  "destinationAccountId": "CTA-2001",
  "amount": 600000
}
```

**Response (borrador):** `200 OK` (tanto para autorizada como rechazada — el resultado va en el
cuerpo; se documentará la justificación de este código de estado vs. usar 4xx para rechazos de
negocio en ADR.md cuando se implemente).

```json
{
  "requestReference": "REF-001",
  "sourceAccountId": "CTA-1001",
  "destinationAccountId": "CTA-2001",
  "amount": 600000,
  "status": "AUTHORIZED",
  "reason": null,
  "processedAt": "2026-09-30T20:00:00Z"
}
```

Posibles valores de `status`: `AUTHORIZED`, `REJECTED`.

### `GET /api/transfers/{requestReference}` — RF06

Consulta una transferencia específica por su referencia. `404` si no existe ninguna solicitud con
esa referencia.

### `GET /api/transfers?limit=N` — RF06

Consulta las transferencias recientes procesadas por el sistema (sin paginación avanzada; se
definirá un límite simple por defecto).

## Pendiente de documentar

- [ ] Esquema final de validación y códigos de error (400 por datos inválidos vs. rechazo de
      negocio).
- [ ] Ejemplos reales de request/response capturados de pruebas de integración.
- [ ] Endpoint o evento de RabbitMQ, si se implementa el punto opcional.
