package com.bancoias.transfers.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Cuerpo de la solicitud de {@code POST /api/transfers} (RF01).
 *
 * <p>Solo valida presencia/forma de los campos (400 si faltan). Las reglas de
 * negocio de RF02 — valor &gt; 0, cuentas distintas, cuentas válidas, límite
 * diario — se evalúan en el caso de uso y resultan en una transferencia
 * {@code REJECTED} (200 OK), no en un error HTTP.
 */
public record TransferRequestDto(
		@NotBlank String requestReference,
		@NotBlank String sourceAccountId,
		@NotBlank String destinationAccountId,
		@NotNull BigDecimal amount) {
}
