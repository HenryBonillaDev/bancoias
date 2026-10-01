package com.bancoias.transfers.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Resultado de procesar una solicitud de transferencia (RF01, RF03). Es un
 * hecho inmutable: una vez procesada, una transferencia no cambia de estado.
 *
 * @param reason motivo del resultado, no nulo cuando {@code status} es
 *               {@link TransferStatus#REJECTED}; {@code null} cuando fue
 *               autorizada.
 */
public record Transfer(
		String requestReference,
		String sourceAccountId,
		String destinationAccountId,
		BigDecimal amount,
		TransferStatus status,
		String reason,
		Instant processedAt) {

	public static Transfer authorized(
			String requestReference,
			String sourceAccountId,
			String destinationAccountId,
			BigDecimal amount,
			Instant processedAt) {
		return new Transfer(
				requestReference, sourceAccountId, destinationAccountId, amount,
				TransferStatus.AUTHORIZED, null, processedAt);
	}

	public static Transfer rejected(
			String requestReference,
			String sourceAccountId,
			String destinationAccountId,
			BigDecimal amount,
			String reason,
			Instant processedAt) {
		return new Transfer(
				requestReference, sourceAccountId, destinationAccountId, amount,
				TransferStatus.REJECTED, reason, processedAt);
	}

	public boolean isAuthorized() {
		return status == TransferStatus.AUTHORIZED;
	}
}
