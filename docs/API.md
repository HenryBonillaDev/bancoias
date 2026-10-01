# API

> Contrato validado por `TransferControllerIntegrationTest` (18 pruebas automatizadas en total,
> ver estado de cobertura en [REQUIREMENTS.md](./REQUIREMENTS.md#12-estado-de-cobertura-de-requisitos-se-irá-actualizando)).

## Convenciones

- Content-Type: `application/json`.
- Formato de fecha/hora: ISO-8601 UTC (`processedAt`).
- Los nombres de campos son los definidos funcionalmente en
  [REQUIREMENTS.md](./REQUIREMENTS.md#3-datos-iniciales-seed), con posibles adaptaciones técnicas
  documentadas aquí si aplica.

## Endpoints

### `POST /api/transfers` — RF01, RF02, RF03, RF05

Registra una solicitud de transferencia. Siempre responde `200 OK`: el resultado de negocio
(autorizada o rechazada) va en el cuerpo, nunca como código de error HTTP — un rechazo de negocio
(RF02) no es un error de la petición. Solo se responde con un código de error HTTP cuando el
request está mal formado (ver más abajo).

**Request body:**
```json
{
  "requestReference": "REF-001",
  "sourceAccountId": "CTA-1001",
  "destinationAccountId": "CTA-2001",
  "amount": 600000
}
```

**Response — autorizada:**
```json
{
  "requestReference": "REF-001",
  "sourceAccountId": "CTA-1001",
  "destinationAccountId": "CTA-2001",
  "amount": 600000,
  "status": "AUTHORIZED",
  "reason": null,
  "processedAt": "2026-10-01T03:25:40.604545014Z"
}
```

**Response — rechazada** (mismo shape, con `reason` explicando el motivo):
```json
{
  "requestReference": "REF-005",
  "sourceAccountId": "CTA-1001",
  "destinationAccountId": "CTA-2001",
  "amount": 4500000,
  "status": "REJECTED",
  "reason": "La operación supera el límite diario permitido de la cuenta origen",
  "processedAt": "2026-10-01T03:25:40.861450027Z"
}
```

Razones de rechazo posibles (RF02):
- `El valor de la transferencia debe ser mayor que cero`
- `La cuenta origen y la cuenta destino deben ser diferentes`
- `La cuenta origen no es una cuenta válida`
- `La cuenta destino no es una cuenta válida`
- `La operación supera el límite diario permitido de la cuenta origen`

**Idempotencia (RF05):** reenviar la misma `requestReference` devuelve `200 OK` con el resultado
ya persistido de la solicitud original (mismo `status`, `reason`, `amount` y `processedAt`),
incluso si el segundo request trae datos distintos (p. ej. otro `amount`).

**400 Bad Request** — solo por datos estructuralmente inválidos (campo requerido vacío/ausente):
```json
{
  "message": "Solicitud inválida",
  "details": ["requestReference: must not be blank"]
}
```

### `GET /api/transfers/{requestReference}` — RF06

Consulta una transferencia específica por su referencia. `200 OK` con el mismo shape de
`TransferResponseDto` anterior, o `404 Not Found` si no existe ninguna solicitud con esa
referencia.

### `GET /api/transfers?limit=N` — RF06

Consulta las transferencias recientes procesadas por el sistema, ordenadas por `processedAt`
descendente. `limit` es opcional (por defecto 20). Sin paginación avanzada (offset/cursor), tal
como permite el enunciado.

### `GET /api/accounts/{accountId}/daily-usage` — visibilidad de RF02 (no exigido por el enunciado)

Endpoint de solo lectura, agregado para verificar visualmente que las transferencias de prueba
afectan el acumulado diario de una cuenta (ver [DECISIONS.md](./DECISIONS.md)). No reemplaza ni
implementa un módulo de administración de cuentas: el enunciado exime explícitamente esa
necesidad.

**Response — `200 OK`:**
```json
{
  "accountId": "CTA-1001",
  "dailyLimit": 5000000,
  "consumedToday": 600000.00,
  "remaining": 4400000.00
}
```

`404 Not Found` si `accountId` no es una de las cuentas semilla (CTA-1001, CTA-1002, CTA-2001).
`consumedToday` solo suma transferencias **autorizadas** del día de la cuenta como **origen**; una
transferencia rechazada no lo afecta.

## Pendiente de documentar

- [ ] Endpoint o evento de RabbitMQ, si se implementa el punto opcional.
- [ ] Impacto en el contrato, si lo hay, al resolver RF04 (concurrencia).
